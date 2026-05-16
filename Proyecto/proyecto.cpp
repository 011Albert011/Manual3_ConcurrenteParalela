#include <chrono>
#include <condition_variable>
#include <iostream>
#include <mutex>
#include <random>
#include <string>
#include <thread>
#include <vector>

using namespace std;

#define NUM_TELS_TOTALES 10  // Definimos el número total de teléfonos como constante
#define NUM_EST_TOTALES 7    // Definimos el número de todos los estados posibles

/*
    Creamos las constantes de estados para cada uno de los casos en los que se pueda encontrar el teléfono ya sea ante
    el usuario o ante la central
*/
enum Estado { COLG_ESPERA, COLG_LLAMADA, DESC_SENIAL, DESC_MARCANDO, DESC_LLAMANDO, DESC_OCUPADO, DESC_HABLANDO };

/*
    Se crea un struct que nos indica el estado del telefono y permite asignar su correspondiente mensaje de estado
    (este es el medio de comunicación (paquete de datos como estructura genérica) entre el monitor y el teléfono)
*/
struct Estado_Mensaje {
    Estado estadoActual;
    string mensajeMonitorTel;
};

// (Monitor: Central Telefónica)
class Central {
   public:
    // Catalogo de todos los estados y mensajes posibles
    Estado_Mensaje catalogo[NUM_EST_TOTALES];

    // Generamos las estructuras para el monitoreo de los teléfonos conectados (Recursos compartidos)
    Estado estadosTel[NUM_TELS_TOTALES];
    int conectadoCon[NUM_TELS_TOTALES];

    // Variables para la exclusión mutua
    mutex candado;
    condition_variable vc_cambioEstado;
    condition_variable vc_esperaComunicacion;

   public:
    Central() {
        // Definimos todos los estados iniciales posibles
        catalogo[0] = {COLG_ESPERA, "Colgado y en espera"};
        catalogo[1] = {COLG_LLAMADA, "Ring-Ring"};
        catalogo[2] = {DESC_SENIAL, "Piiiii..."};
        catalogo[3] = {DESC_MARCANDO, ""};
        catalogo[4] = {DESC_LLAMANDO, "Piii-Piii-Piii..."};
        catalogo[5] = {DESC_OCUPADO, "Tuuu-Tuu-Tuu..."};
        catalogo[6] = {DESC_HABLANDO, ""};

        // Inicializamos los estados de los telefonos
        for (int i = 0; i < NUM_TELS_TOTALES; i++) {
            estadosTel[i] = COLG_ESPERA;
            conectadoCon[i] = -1;
        }
    }

    // Función que nos envía el estado del teléfono
    Estado_Mensaje getEstado(int numTel) {
        Estado_Mensaje paquete;

        // Debido a que estamos accediendo a un recurso compartido generamos un candado para obtener un valor de lectura
        // real
        unique_lock<mutex> lk(candado);

        paquete.estadoActual = estadosTel[numTel];

        // Inicializamos el mensaje correpondiente al estado del teléfono que la central esté gestionando en el momento
        switch (paquete.estadoActual) {
            case DESC_MARCANDO:
                paquete.mensajeMonitorTel = "Teléfono marcando a " + to_string(conectadoCon[numTel]);
                break;

            case DESC_HABLANDO:
                paquete.mensajeMonitorTel = "Teléfono hablando con " + to_string(conectadoCon[numTel]);
                break;

            default:
                paquete.mensajeMonitorTel = catalogo[paquete.estadoActual].mensajeMonitorTel;
        }
        lk.unlock();
        return paquete;
    }

    // Funciones que reflejan las posibles acciones del usuario sobre el teléfono, que el teléfono comunica a su vez con
    // la central
    void marcar(int numTel, int numAMarcar) {
        /*
            Solo establecemos la conexión del lado del teléfono que marca porque no sabemos si el otro teléfono está
            disponible y lo que estamos registrando son las teclas que pulsa el usuario y detecta el telefono
        */
        unique_lock<mutex> lk(candado);

        conectadoCon[numTel] = numAMarcar;
        estadosTel[numTel] = DESC_MARCANDO;

        lk.unlock();
        vc_cambioEstado.notify_all();
    }

    void colgar(int numTel) {
        unique_lock<mutex> lk(candado);

        int otroTelefono = conectadoCon[numTel];
        if (otroTelefono != -1) {
            if (estadosTel[otroTelefono] == COLG_LLAMADA)
                estadosTel[otroTelefono] = COLG_ESPERA;
            else
                estadosTel[otroTelefono] = DESC_OCUPADO;

            conectadoCon[otroTelefono] = -1;
        }

        estadosTel[numTel] = COLG_ESPERA;
        conectadoCon[numTel] = -1;

        lk.unlock();
        vc_cambioEstado.notify_all();
        vc_esperaComunicacion.notify_all();
    }

    void descolgar(int numTel) {
        unique_lock<mutex> lk(candado);

        switch (estadosTel[numTel]) {
            case COLG_ESPERA:
                estadosTel[numTel] = DESC_SENIAL;
                break;

            case COLG_LLAMADA:
                estadosTel[numTel] = DESC_HABLANDO;

                int otroTelefono = conectadoCon[numTel];
                if (otroTelefono != -1) estadosTel[otroTelefono] = DESC_HABLANDO;
                break;
        }
        lk.unlock();
        vc_esperaComunicacion.notify_all();
        vc_cambioEstado.notify_all();
        return;
    }

    // Funciones que realiza internamente la central para gestionar la transición de estados al intentar comunicarse un
    // teléfono con otro
    void conectar(int numTel) {
        unique_lock<mutex> lk(candado);

        int otroTelefono = conectadoCon[numTel];
        if ((otroTelefono != -1) && (estadosTel[otroTelefono] == COLG_ESPERA)) {
            estadosTel[numTel] = DESC_LLAMANDO;
            estadosTel[otroTelefono] = COLG_LLAMADA;

            conectadoCon[otroTelefono] = numTel;
        } else
            estadosTel[numTel] = DESC_OCUPADO;

        lk.unlock();
        vc_esperaComunicacion.notify_all();
        vc_cambioEstado.notify_all();
    }

    void esperarContestacion(int numTel) {
        unique_lock<mutex> lk(candado);

        while (estadosTel[numTel] == DESC_LLAMANDO) vc_esperaComunicacion.wait(lk);
        lk.unlock();
    }
};

/*
    Las funciones "central->function()" representan la interacción del teléfono con la central, mientras que las
        varibles aleatorias (como "decision" o "pensarNumero") representan decisiones del usuario simuladas
*/
class Telefono {
   public:
    int numTel;
    Central* central;  // Pasamos una sola central a todos los telefonos por medio de apuntadores

    Estado_Mensaje estadoTel;  // Paquete de datos que indica estado del telefono
    string mensajeTelUser;     // Mensaje que el teléfono muestra al usuario

   public:
    Telefono(int numTel, Central* central) {
        this->numTel = numTel;
        this->central = central;
    }

    void operator()() {
        // Variables para generar los números aleatorios que representan la decisión del usuario
        random_device rd;
        // Se indica como "static thread_local" para que exista un generador distinto por hilo
        static thread_local mt19937 gen(rd());
        uniform_int_distribution<> decision(0, 1);

        uniform_int_distribution<> pensarNumero(0, 9);

        // Variable de tiempo de respuesta del usuario
        uniform_int_distribution<> tiempoRespUser(800, 1200);

        // Generamos la variable que reporte los cambios e imprimimos el primer mensaje por defecto del
        // telefono
        Estado ultimoEstado = COLG_ESPERA;

        estadoTel = central->getEstado(numTel);
        mensajeTelUser = "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";
        cout << mensajeTelUser;

        int numAMarcar;

        // Utilizamos un bucle while(true) para que los teléfonos estén siempre activos hasta que se
        // interrumpa la ejecución (CNTRL-C)
        while (true) {
            // Obtenemos el paquete de datos del estado que haya generado la central
            estadoTel = central->getEstado(numTel);

            // Si el estado del telefono cambia se imprime y actualizamos "ultimoEstado"
            if (estadoTel.estadoActual != ultimoEstado) {
                mensajeTelUser = "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";
                cout << mensajeTelUser;

                ultimoEstado = estadoTel.estadoActual;
            }

            // Marca si es que el usuario decide tomar cierta decisión dependiendo del estado del teléfono
            bool decisionUsuario = (bool)decision(gen);

            switch (estadoTel.estadoActual) {
                case (COLG_ESPERA):
                case (COLG_LLAMADA):
                    // Si es que el telefono está colgado, decide si descolgarlo o no
                    if (decisionUsuario) central->descolgar(numTel);
                    break;

                case (DESC_SENIAL):
                    // En este caso "decisionUsuario" indica si es que se tiene la decisión de marcar una vez que el
                    // teléfono está descolgado y dando señal
                    if (decisionUsuario) {
                        do numAMarcar = pensarNumero(gen);
                        while (numAMarcar == numTel);
                        central->marcar(numTel, numAMarcar);
                    } else
                        // De no ser así, decide colgar
                        central->colgar(numTel);
                    break;

                case (DESC_MARCANDO):
                    // Una vez que se encuentra marcando el número la central se pone a trabajar para registrar los
                    // datos necesarios
                    central->conectar(numTel);
                    break;

                case (DESC_LLAMANDO):
                    // Mientras se llama, la central gestiona la espera de nuestro teléfono y la conectividad con el
                    // teléfono de destino
                    central->esperarContestacion(numTel);
                    break;
                case (DESC_OCUPADO):
                case (DESC_HABLANDO):
                    // Independientemente de si el otro teléfono le respondió o no, el usuario siempre tiene la elección
                    // de colgar en cualquier momento
                    if (decisionUsuario) central->colgar(numTel);
                    break;
            }

            // Añadimos un pequeño retraso simulando el tiempo en el que una persona realiza cierta acción
            this_thread::sleep_for(chrono::milliseconds(tiempoRespUser(gen)));
        }
    }
};

int main() {
    vector<thread> hilos;
    // Generamos la instancia de la central telefónica
    Central* central = new Central();

    // Inicializamos los hilos
    for (int i = 0; i < NUM_TELS_TOTALES; i++) {
        Telefono registrandoTel(i, central);

        hilos.push_back(thread(registrandoTel));
    }

    for (int i = 0; i < NUM_TELS_TOTALES; i++) hilos[i].join();

    return 0;
}

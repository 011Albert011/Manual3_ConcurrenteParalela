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
#define NUM_EST_TOTALES 8    // Definimos el número de todos los estados posibles

mutex mutex_pantalla;

/*
    Creamos las constantes de estados para cada uno de los casos en los que se pueda encontrar el teléfono ya sea ante
    el usuario o ante la central
*/
enum Estado {
    COLG_ESPERA,
    COLG_LLAMADA,
    DESC_SENIAL,
    DESC_MARCANDO,
    DESC_LLAMANDO,
    DESC_OCUPADO,
    DESC_HABLANDO,
    CREADO
};

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
        catalogo[1] = {COLG_LLAMADA, "Ring-Ring (Colgado y recibiendo llamada)"};
        catalogo[2] = {DESC_SENIAL, "Piiiii... (Descolgado y dando señal)"};
        catalogo[3] = {DESC_MARCANDO, ""};
        catalogo[4] = {DESC_LLAMANDO, "Piii-Piii-Piii... (Descolgado y llamando)"};
        catalogo[5] = {DESC_OCUPADO, "Tuuu-Tuu-Tuu... (El teléfono al que se llamó está ocupado)"};
        catalogo[6] = {DESC_HABLANDO, ""};

        catalogo[7] = {CREADO, "Inicializando línea..."};

        // Inicializamos los estados de los telefonos
        for (int i = 0; i < NUM_TELS_TOTALES; i++) {
            estadosTel[i] = CREADO;  // Utilizamos el estado creado para que se detecte que esté colgado el teléfono y
                                     // también lo imprima de manera paralela sin que los primeros letreros sean todos
                                     // los teléfonos de manera secuencial en "colgado_espera"
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
        // Solo cambiamos al otro si el otro está enlazado exactamente con nosotros
        if (otroTelefono != -1 && conectadoCon[otroTelefono] == numTel) {
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
                if (otroTelefono != -1) {
                    estadosTel[otroTelefono] = DESC_HABLANDO;
                    conectadoCon[otroTelefono] = numTel;
                }
                break;
        }
        lk.unlock();
        vc_esperaComunicacion.notify_all();
        vc_cambioEstado.notify_all();
    }

    // Funciones que realiza internamente la central para gestionar la transición de estados al intentar comunicarse un
    // teléfono con otro
    void conectar(int numTel) {
        unique_lock<mutex> lk(candado);

        int otroTelefono = conectadoCon[numTel];

        // Siempre pasamos al emisor a DESC_LLAMANDO para que pinte su "Piii-Piii-Piii..."
        estadosTel[numTel] = DESC_LLAMANDO;

        // Al receptor solo le mandamos el Ring-Ring si está realmente libre
        if ((otroTelefono != -1) && (estadosTel[otroTelefono] == COLG_ESPERA) && (conectadoCon[otroTelefono] == -1)) {
            estadosTel[otroTelefono] = COLG_LLAMADA;
            conectadoCon[otroTelefono] = numTel;  // El receptor sabe quién le llama
        }
        // Si no estaba libre, no hacemos nada, dejando que el emisor "llame" a la "nada" por un rato

        lk.unlock();
        vc_esperaComunicacion.notify_all();
        vc_cambioEstado.notify_all();
    }

    void verificarEnlace(int numTel) {
        unique_lock<mutex> lk(candado);

        int otroTelefono = conectadoCon[numTel];
        // Si el destino no está conectado con nosotros (porque estaba ocupado y no aceptó el Ring-Ring)
        if (otroTelefono != -1 && conectadoCon[otroTelefono] != numTel) {
            estadosTel[numTel] = DESC_OCUPADO;
            conectadoCon[numTel] = -1;  // Limpiamos el intento fallido
        }

        lk.unlock();
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
        Estado ultimoEstado = CREADO;

        int numAMarcar;

        // Utilizamos un bucle while(true) para que los teléfonos estén siempre activos hasta que se
        // interrumpa la ejecución (CNTRL-C)
        while (true) {
            // Obtenemos el paquete de datos del estado que haya generado la central
            estadoTel = central->getEstado(numTel);

            // Marca si es que el usuario decide tomar cierta decisión dependiendo del estado del teléfono
            bool decisionUsuario = (bool)decision(gen);

            switch (estadoTel.estadoActual) {
                case (COLG_ESPERA):
                    // Si es que el telefono está colgado, decide si descolgarlo o no
                    if (decisionUsuario) central->descolgar(numTel);
                    break;

                case (COLG_LLAMADA):
                    if (decisionUsuario) {
                        // El usuario decide contestar, pero le toma tiempo reaccionar (Simulación humana)
                        // Primero dejamos que el "if" imprima el Ring-Ring original de la central
                        estadoTel = central->getEstado(numTel);
                        if (estadoTel.estadoActual != ultimoEstado) {
                            mensajeTelUser =
                                "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";

                            unique_lock<mutex> lk_pantalla(mutex_pantalla);
                            cout << mensajeTelUser;
                            lk_pantalla.unlock();

                            ultimoEstado = estadoTel.estadoActual;
                        }

                        // Dejamos que el teléfono suene en la vida real durante un instante antes de levantar el
                        // auricular
                        this_thread::sleep_for(chrono::milliseconds(600));

                        central->descolgar(numTel);
                    }
                    break;

                case (DESC_SENIAL):
                    // En este caso "decisionUsuario" indica si es que se tiene la decisión de marcar una vez que el
                    // teléfono está descolgado y dando señal
                    if (decisionUsuario) {
                        do numAMarcar = pensarNumero(gen);
                        while (numAMarcar == numTel);

                        central->marcar(numTel, numAMarcar);
                    } else
                        central->colgar(numTel);
                    break;

                case (DESC_MARCANDO):
                    // La central registra el intento y nos pasa a DESC_LLAMANDO obligatoriamente
                    central->conectar(numTel);

                    // Obtenemos el estado e imprimimos inmediatamente el "Piii-Piii-Piii..."
                    estadoTel = central->getEstado(numTel);

                    if (estadoTel.estadoActual != ultimoEstado) {
                        mensajeTelUser = "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";

                        unique_lock<mutex> lk_pantalla(mutex_pantalla);
                        cout << mensajeTelUser;
                        lk_pantalla.unlock();

                        ultimoEstado = estadoTel.estadoActual;
                    }

                    // Simulamos que el teléfono pita durante 500ms en la vida real
                    this_thread::sleep_for(chrono::milliseconds(500));

                    // Después de ese retraso, la central verificamos si el destino estaba realmente ocupado o no
                    central->verificarEnlace(numTel);
                    break;

                case (DESC_LLAMANDO):
                    // Mientras se llama, la central gestiona la espera de nuestro teléfono y la conectividad con el
                    // teléfono de destino
                    if (decisionUsuario)
                        central->colgar(numTel);
                    else
                        central->esperarContestacion(numTel);
                    break;

                case (DESC_OCUPADO):
                case (DESC_HABLANDO):
                    // Independientemente de si el otro teléfono le respondió o no, el usuario siempre tiene la elección
                    // de colgar en cualquier momento
                    if (decisionUsuario) central->colgar(numTel);
                    break;

                case (CREADO):
                    // Haremos la trancisión entre CREADO y COLG_ESPERA para que posteriormente se pueda imprimir el
                    // mensaje de COLG_ESPERA
                    central->colgar(numTel);
                    break;
            }

            // Debido a que el switch pudo haber modificado el comportamiento del teléfono, es buena idea verificar que
            // su estado y conexión sean válidos y estén actualizados, por eso llamamos a "getEstado" nuevamente
            estadoTel = central->getEstado(numTel);

            // Si el estado del telefono cambia se imprime y actualizamos "ultimoEstado"
            if (estadoTel.estadoActual != ultimoEstado) {
                mensajeTelUser = "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";

                // El teléfono toma el recurso de la pantalla y ocupa su respectivo candado para imprimir todos los
                // mensajes de todos los teléfonos
                unique_lock<mutex> lk_pantalla(mutex_pantalla);
                cout << mensajeTelUser;
                lk_pantalla.unlock();

                ultimoEstado = estadoTel.estadoActual;
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
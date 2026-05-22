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

mutex mutex_pantalla;  // Creado para que los mensajes en pantalla no se sobrepongan o ignoren al manipularse una gran
                       // cantidad de hilos, se bloque el candado de nuestro recurso llamado "pantalla", para que no
                       // existan interrupciones

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
    CREADO  // Estado comodín, para que los hilos impriman posteriormente de forma paralela su mensaje de "colgado y en
            // espera"
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
        catalogo[1] = {COLG_LLAMADA, "Ring-Ring                (Colgado y recibiendo llamada)"};
        catalogo[2] = {DESC_SENIAL, "Piiiii...                (Descolgado y dando señal)"};
        catalogo[3] = {DESC_MARCANDO, ""};
        catalogo[4] = {DESC_LLAMANDO, "Piii-Piii-Piii...        (Descolgado y llamando)"};
        catalogo[5] = {DESC_OCUPADO, "Tuuu-Tuu-Tuu...          (El teléfono al que se llamó está ocupado)"};
        catalogo[6] = {DESC_HABLANDO, ""};

        // Asignamos también un letrero para el estado comodín, aunque posteriormente no se imprima
        catalogo[7] = {CREADO, "Inicializando línea..."};

        // Inicializamos los estados de los telefonos
        for (int i = 0; i < NUM_TELS_TOTALES; i++) {
            estadosTel[i] = CREADO;
            conectadoCon[i] = -1;
        }
    }

    // Función que nos envía el estado del teléfono
    Estado_Mensaje getEstado(int numTel) {
        Estado_Mensaje paquete;
        unique_lock<mutex> lk(candado);

        paquete.estadoActual = estadosTel[numTel];

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

    /*
        Funciones que representan las acciones que puede realizar el usuario sore el teléfono, estas nos ayudan a
        representar que el teléfono se comunica con la central telefónica
    */
    // La función no crea ninguna conexión real, solo le indica a la central que el usuario del telefono tiene la
    // ¡intención! de contactar con otro
    void marcar(int numTel, int numAMarcar) {
        unique_lock<mutex> lk(candado);

        unique_lock<mutex> lk_p(mutex_pantalla);
        cout << "\n(Central registra: Teléfono " << numTel << " pulsó teclas para llamar al Teléfono " << numAMarcar
             << ")\n\n";
        lk_p.unlock();

        conectadoCon[numTel] = numAMarcar;
        estadosTel[numTel] = DESC_MARCANDO;

        lk.unlock();
        vc_cambioEstado.notify_all();
    }

    void colgar(int numTel) {
        unique_lock<mutex> lk(candado);

        int otroTelefono = conectadoCon[numTel];

        if (otroTelefono != -1 && conectadoCon[otroTelefono] == numTel) {
            unique_lock<mutex> lk_p(mutex_pantalla);
            cout << "\n(Central interrumpe: Teléfono " << numTel << " cortó la línea. Notificando a Teléfono "
                 << otroTelefono << ")\n\n";
            lk_p.unlock();

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
                unique_lock<mutex> lk_p(mutex_pantalla);
                cout << "\n(Central consolida enlace: Teléfono " << numTel << " atendió llamada entrante.)\n\n";
                lk_p.unlock();

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

    /*
        Funciones propias de la central que se encargan del procesamiento de los estados intermedios (generalmente al
        intentar conectar un teléfono con otro) de los teléfonos involucrados
    */
    void conectar(int numTel) {
        unique_lock<mutex> lk(candado);
        int otroTelefono = conectadoCon[numTel];

        estadosTel[numTel] = DESC_LLAMANDO;

        if ((otroTelefono != -1) && (estadosTel[otroTelefono] == COLG_ESPERA) && (conectadoCon[otroTelefono] == -1)) {
            unique_lock<mutex> lk_p(mutex_pantalla);
            cout << "\n(Central ruteando: Línea libre. Enviando impulsos de Ring-Ring al Teléfono " << otroTelefono
                 << " [Origen: Tel " << numTel << "])\n\n";
            lk_p.unlock();

            estadosTel[otroTelefono] = COLG_LLAMADA;
            conectadoCon[otroTelefono] = numTel;
        } else {
            unique_lock<mutex> lk_p(mutex_pantalla);
            cout << "\n(Central detecta colisión/línea ocupada: Destino " << otroTelefono
                 << " no disponible de inmediato. [Origen: Tel " << numTel << "])\n\n";
            lk_p.unlock();
        }

        lk.unlock();
        vc_esperaComunicacion.notify_all();
        vc_cambioEstado.notify_all();
    }

    void verificarEnlace(int numTel) {
        unique_lock<mutex> lk(candado);
        int otroTelefono = conectadoCon[numTel];

        if (otroTelefono != -1) {
            if (conectadoCon[otroTelefono] != numTel ||
                (estadosTel[otroTelefono] == DESC_LLAMANDO && estadosTel[numTel] == DESC_LLAMANDO)) {
                unique_lock<mutex> lk_p(mutex_pantalla);
                cout << "\n(Central deniega enlace: Cancelando intento del Teléfono " << numTel
                     << " por rechazo/ocupación. [Destino: Tel " << otroTelefono << "])\n\n";
                lk_p.unlock();

                estadosTel[numTel] = DESC_OCUPADO;
                conectadoCon[numTel] = -1;
            } else if (estadosTel[otroTelefono] == DESC_HABLANDO && estadosTel[numTel] == DESC_HABLANDO) {
                unique_lock<mutex> lk_p(mutex_pantalla);
                cout << "\n(Central verifica: Enlace exitoso establecido entre " << numTel << " y " << otroTelefono
                     << ")\n\n";
                lk_p.unlock();
            }
        }

        lk.unlock();
        vc_cambioEstado.notify_all();
        vc_esperaComunicacion.notify_all();
    }

    void esperarContestacion(int numTel) {
        unique_lock<mutex> lk(candado);
        while (estadosTel[numTel] == DESC_LLAMANDO) vc_esperaComunicacion.wait(lk);
        lk.unlock();
    }
};

class Telefono {
   public:
    int numTel;
    Central* central;

    // Paquete de datos que se recibe de la central
    Estado_Mensaje estadoTel;

    // Variable que respeta la condicional de que el teléfono sea el que se comunique con el usuario
    string mensajeTelUser;

   public:
    Telefono(int numTel, Central* central) {
        this->numTel = numTel;
        this->central = central;
    }

    void operator()() {
        random_device rd;
        // Lo asignamos como "static thread_local" para que cada hilo tenga su propio generador de números independiente
        static thread_local mt19937 gen(rd());

        // Variables que simulan decisiones/acciones del usuario del teléfono
        uniform_int_distribution<> decision(0, 1);
        uniform_int_distribution<> pensarNumero(0, 9);
        uniform_int_distribution<> tiempoRespUser(800, 1200);

        // Varible que nos ayuda en la impresión que detecta cambios de estado
        Estado ultimoEstado = CREADO;
        int numAMarcar;

        // Utilizamos un bucle while(true) para que los teléfonos estén siempre activos hasta que se
        // interrumpa la ejecución (CNTRL-C)
        while (true) {
            // Obtenemos el paquete de datos del estado que haya generado la central y generamos la decisión del usuario
            // con respecto al estado del teléfono
            estadoTel = central->getEstado(numTel);
            bool decisionUsuario = (bool)decision(gen);

            switch (estadoTel.estadoActual) {
                case (COLG_ESPERA):
                    // Si decisionUsuario es TRUE, significa que decide descolgar el teléfono
                    if (decisionUsuario) {
                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " lo descuelga.\n";
                        lk_p.unlock();

                        central->descolgar(numTel);
                    }
                    break;

                case (COLG_LLAMADA):
                    // Si el teléfono recibe una llamada y decisiónUsuario es TRUE, se imprime "Ring-Ring" por parte del
                    // teléfono existe cierto descanso de 600 ms simulando tiempo de reacción humano y entonces
                    // descuelga, si decisiónUsuario es FALSE se interpreta que solo ignora la llamada
                    if (decisionUsuario) {
                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " escucha el Ring-Ring y decide contestar.\n";
                        lk_p.unlock();

                        estadoTel = central->getEstado(numTel);
                        if (estadoTel.estadoActual != ultimoEstado) {
                            mensajeTelUser =
                                "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";

                            unique_lock<mutex> lk_p2(mutex_pantalla);
                            cout << mensajeTelUser;
                            lk_p2.unlock();

                            ultimoEstado = estadoTel.estadoActual;
                        }

                        this_thread::sleep_for(chrono::milliseconds(600));
                        central->descolgar(numTel);
                    }
                    break;

                case (DESC_SENIAL):
                    // Si decisionUsuario es igual a TRUE, se interpreta que el usuario desea marcar un número, en caso
                    // contrario se interpreta que el usuario desea colgar
                    if (decisionUsuario) {
                        do numAMarcar = pensarNumero(gen);
                        while (numAMarcar == numTel);

                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " empieza a marcar los dígitos del número " << numAMarcar << ".\n";
                        lk_p.unlock();

                        central->marcar(numTel, numAMarcar);
                    } else {
                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " vuelve a colgar tras haber descolgado.\n";
                        lk_p.unlock();

                        central->colgar(numTel);
                    }
                    break;

                case (DESC_MARCANDO):
                    // La central registra el intento y nos pasa a DESC_LLAMANDO, por lo que obtenemos el estado del
                    // teléfono e imprimimos el "Piii-Piii-Piii..."(Llamando)
                    central->conectar(numTel);
                    estadoTel = central->getEstado(numTel);

                    if (estadoTel.estadoActual != ultimoEstado) {
                        mensajeTelUser = "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";

                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << mensajeTelUser;
                        lk_p.unlock();

                        ultimoEstado = estadoTel.estadoActual;
                    }
                    // Simulamos el pitido del teléfono durante un momento
                    this_thread::sleep_for(chrono::milliseconds(800));

                    // Tras ello verificamos nuevamente si el destino estaba ocupado o no
                    central->verificarEnlace(numTel);
                    break;

                case (DESC_LLAMANDO):
                    // Si el usuario así lo desea mientras hace la llamada se puede cansar y decidir colgar o en caso
                    // contrario seguir esperando
                    if (decisionUsuario) {
                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " se cansó de esperar el tono de llamada y colgó.\n";
                        lk_p.unlock();

                        central->colgar(numTel);
                    } else {
                        central->esperarContestacion(numTel);
                    }
                    break;

                // Independientemente de si fue respondido o no, el usuario siempre tiene la opción de colgar el
                // teléfono
                case (DESC_OCUPADO):
                    this_thread::sleep_for(chrono::milliseconds(1000));
                    if (decisionUsuario) {
                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " lo cuelga tras saber que el destinatario está ocupado.\n";
                        lk_p.unlock();

                        central->colgar(numTel);
                    }
                    break;

                case (DESC_HABLANDO):
                    this_thread::sleep_for(chrono::milliseconds(1000));
                    if (decisionUsuario) {
                        unique_lock<mutex> lk_p(mutex_pantalla);
                        cout << "                                       --> El usuario del Teléfono " << numTel
                             << " termina la charla colgando nuevamente el teléfono.\n";
                        lk_p.unlock();

                        central->colgar(numTel);
                    }
                    break;

                case (CREADO):
                    // Haremos la trancisión entre CREADO y COLG_ESPERA para que posteriormente se pueda imprimir el
                    // mensaje de COLG_ESPERA
                    central->colgar(numTel);
                    break;
            }

            // Debido a que el switch o algún otro teléfono pudo haber modificado el comportamiento del teléfono, es
            // buena idea verificar que su estado y conexión sean válidos y estén actualizados, por eso llamamos a
            // "getEstado" nuevamente
            estadoTel = central->getEstado(numTel);

            // Si el estado del telefono cambia se imprime y actualizamos "ultimoEstado"
            if (estadoTel.estadoActual != ultimoEstado) {
                mensajeTelUser = "[Teléfono " + to_string(numTel) + "]: " + estadoTel.mensajeMonitorTel + "\n";

                unique_lock<mutex> lk_p(mutex_pantalla);
                cout << mensajeTelUser;
                lk_p.unlock();

                ultimoEstado = estadoTel.estadoActual;
            }

            // Añadimos un pequeño retraso simulando el tiempo en el que una persona realiza cierta acción
            this_thread::sleep_for(chrono::milliseconds(tiempoRespUser(gen)));
        }
    }
};

int main() {
    vector<thread> hilos;
    Central* central = new Central();  // Generamos la instancia compartida de central telefónica

    // Inicializamos los hilos
    for (int i = 0; i < NUM_TELS_TOTALES; i++) {
        Telefono registrandoTel(i, central);
        hilos.push_back(thread(registrandoTel));
    }

    for (int i = 0; i < NUM_TELS_TOTALES; i++) hilos[i].join();

    return 0;
}
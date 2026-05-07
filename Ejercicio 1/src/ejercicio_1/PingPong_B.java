package ejercicio_1;

import Jpmi.*;

// Clase que representa el "jugador" B en el juego de Ping Pong
public class PingPong_B implements Proceso {
	// Canales para comunicarse con A
	private CanalSimple canalIn, canalOut;

	public PingPong_B(CanalSimple canalIn, CanalSimple canalOut) {
		this.canalIn = canalIn;
		this.canalOut = canalOut;
	}

	public void run() {
		// Bucle infinito para recibir mensajes de A y enviar de vuelta el mismo mensaje a A
		while (true) {
			// Recibe e imprime el mensaje recibido desde A 
				String mensajeRecibido = canalIn.receive().toString();

				System.out.println("[PONG] | Recibido el mensaje " + mensajeRecibido + " en B desde A");

			// Envia e imprime el mismo mensaje recibido desde A hacia A
				System.out.println("[PING] | Enviando de nuevo el mensaje " + mensajeRecibido + " desde B hacia A");

				canalOut.send(mensajeRecibido);
		}
	}
	
}

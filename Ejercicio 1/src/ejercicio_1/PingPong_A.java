package ejercicio_1;

import Jpmi.*;
import java.util.Random;

// Clase que representa el "jugador" A en el juego de Ping Pong
public class PingPong_A implements Proceso {
	// Canales para comunicarse con B
	private CanalSimple canalIn, canalOut;

	// Caracteres para generar cadenas aleatorias
	private String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private Random randomGen = new Random();

	public PingPong_A(CanalSimple canalOut, CanalSimple canalIn) {
		this.canalIn = canalIn;
		this.canalOut = canalOut;
	}

	// Método para generar una cadena aleatoria de una longitud establecida
	public String generarCadenaAleatoria(int longitudCadena) {
		StringBuilder cadenaAleatoria = new StringBuilder();
		for (int i = 0; i < longitudCadena; i++) {
			// Selecciona un índice aleatorio para obtener un carácter de la cadena de caracteres y lo agrega a la cadena aleatoria
			int indiceCaracter = randomGen.nextInt(caracteres.length());
			cadenaAleatoria.append(caracteres.charAt(indiceCaracter));
		}
		return cadenaAleatoria.toString();
	}

	public void run() {
		// Bucle infinito para enviar y recibir mensajes con B
		while (true) {
			// Genera una cadena aleatoria de 10 caracteres para enviar a B
			String mensajeCadAleatoria = generarCadenaAleatoria(10);

			// Envia e imprime el mensaje generado desde A hacia B
				System.out.println("[PING] | Enviando el mensaje _"  + mensajeCadAleatoria + "_ desde A hacia B");

				canalOut.send(mensajeCadAleatoria);
			
			// Recibe e imprime el mensaje enviado desde B hacia A (que es el mismo mensaje que A envió previamente)
				String mensajeRecibido = canalIn.receive().toString();

				System.out.println("[PONG] | Recibido de nuevo el mensaje _" + mensajeRecibido + "_ en A desde B" + "\n-------------------------------------------------------------");
		}
	}
}

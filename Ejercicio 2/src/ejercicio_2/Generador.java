package ejercicio_2;

import java.util.Random;

import Jpmi.*;

// Proceso para generar caracteres aleatorios y enviarlos a dos canales simples
public class Generador implements Proceso {
	// Canales de salida para enviar los caracteres generados
	CanalSimple canalOut1, canalOut2;

	/* Cadena con los caracteres posibles para generar. No se incluyen demasiados 
	caracteres para facilitar que se generen dos veces el caracter "*" (se incluye
	además dos veces para aumentar las probabilidades) y poder ver la salida distinta */
	private String caracteres = "*AB*E14";
	private Random randomGen = new Random();

	public Generador(CanalSimple canalOut1, CanalSimple canalOut2) {
		this.canalOut1 = canalOut1;
		this.canalOut2 = canalOut2;
	}

	// Método para generar un carácter aleatorio a partir de la cadena de caracteres
	public char generarCharAleatorio() {
		char charAleatorio;

		// Genera un índice aleatorio para seleccionar un carácter de la cadena
		int indiceCaracter = randomGen.nextInt(caracteres.length());
		charAleatorio = caracteres.charAt(indiceCaracter);

		return charAleatorio;
	}

	public void run() {
		// Bucle infinito para generar caracteres aleatorios y enviarlos a los canales
		while (true) {
			char charAleatorio1 = generarCharAleatorio();
			char charAleatorio2 = generarCharAleatorio();

			// Se envian los caracteres generados a los canales de salida utilizando procesos Escribe en paralelo
			Paralelo paralelo = new Paralelo(new Proceso[]{
				new Escribe(canalOut1, charAleatorio1),
				new Escribe(canalOut2, charAleatorio2)
			});
			
			paralelo.run();
		}
	}
}
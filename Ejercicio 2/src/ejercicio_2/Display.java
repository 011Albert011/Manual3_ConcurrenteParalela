package ejercicio_2;

import Jpmi.*;

// Proceso para recibir caracteres de un canal simple y mostrarlos por pantalla
public class Display implements Proceso {
	CanalSimple canalIn;

	public Display(CanalSimple canalIn) {
		this.canalIn = canalIn;
	}

	public void run() {
		// Bucle infinito para recibir caracteres del canal de entrada y mostrarlos por pantalla
		while (true) {
			char charRecibido = (char) canalIn.receive();
			System.out.println("Char recibido por Display: " + charRecibido);
		}
	}
}

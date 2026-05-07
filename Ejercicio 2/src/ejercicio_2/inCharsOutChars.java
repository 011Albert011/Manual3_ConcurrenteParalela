package ejercicio_2;

import Jpmi.*;

// Proceso para leer caracteres de dos canales y enviar un resultado a un canal de salida según los caracteres recibidos
public class inCharsOutChars implements Proceso {
	// Canales de entrada y salida
	CanalSimple canalIn1, canalIn2, canalOut;

	public inCharsOutChars(CanalSimple canalIn1, CanalSimple canalIn2, CanalSimple canalOut) {
		this.canalIn1 = canalIn1;
		this.canalIn2 = canalIn2;
		this.canalOut = canalOut;
	}

	public void run() {
		// Bucle infinito para leer caracteres de los canales de entrada y enviar resultados al canal de salida
		while (true) {
			// Se crean procesos Lee para leer caracteres de los canales de entrada
			Lee lee1 = new Lee(canalIn1);
			Lee lee2 = new Lee(canalIn2);

			// Se ejecutan los procesos Lee en paralelo para leer de ambos canales al mismo tiempo
			Paralelo paralelo = new Paralelo(new Proceso[]{
				lee1,
				lee2,
			});
			paralelo.run();

			// Se obtienen los caracteres leídos de los procesos Lee para impimirlos
			char charRecibido1 = (char) lee1.getValor();
			char charRecibido2 = (char) lee2.getValor();

			System.out.println("Chars recibidos por inCharsOutChars: " + charRecibido1 + ", " + charRecibido2);

			// Se envía un carácter al canal de salida según los caracteres recibidos. Si ambos caracteres son '*', se envía '#'; de lo contrario se envía '$'
			if (charRecibido1 == '*' && charRecibido2 == '*') {
				canalOut.send('#');
			} else {
				canalOut.send('$');
			}
		}
	}
}
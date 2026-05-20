package Examen_3;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import Jpmi.*;

// Etapa 3 del Pipeline: Escribir las transacciones válidas e inválidas en archivos de texto separados, incluyendo los motivos de invalidez o sospechosidad, y luego enviar la transacción al siguiente proceso
public class E3_Archivos_Texto implements Proceso {
	// Nombres de los archivos de texto para almacenar las transacciones válidas e inválidas
	private static final String ARCHIVO_VALIDAS = "transacciones_validas.txt";
	private static final String ARCHIVO_INVALIDAS = "transacciones_invalidas.txt";

	// Canales de entrada y salida para recibir las transacciones con la confirmación de validez y sospechosidad desde la etapa 2 y enviar las transacciones al siguiente proceso a través del canal de salida
	private CanalSimple canalIn, canalOut;

	// Constructor 
	public E3_Archivos_Texto(CanalSimple canalIn, CanalSimple canalOut) {
		this.canalIn = canalIn;
		this.canalOut = canalOut;
	}

	// Método run
	public void run() {
		// Se definen los encabezados para los archivos de transacciones válidas e inválidas
		String headerValidas = "===== TRANSACCIONES VALIDAS | " + " =====";
		String headerInvalidas = "===== TRANSACCIONES INVALIDAS | " + " =====";

		// Se utilizan BufferedWriter para escribir en los archivos de texto, asegurando que se cierren correctamente después de su uso
		try (BufferedWriter escritorValidas = new BufferedWriter(new FileWriter(ARCHIVO_VALIDAS));

		BufferedWriter escritorInvalidas = new BufferedWriter(new FileWriter(ARCHIVO_INVALIDAS))) {

			// Se escriben los encabezados en los archivos de texto para las transacciones válidas e inválidas
			escritorValidas.write(headerValidas);
			escritorValidas.newLine();
			escritorValidas.write("----------------------------------------");
			escritorValidas.newLine();

			escritorInvalidas.write(headerInvalidas);
			escritorInvalidas.newLine();
			escritorInvalidas.write("----------------------------------------");
			escritorInvalidas.newLine();

			// Se recorren las 10 transacciones recibidas a través del canal de entrada
			for (int i = 0; i < 10; i++) {
				Transaccion_Bancaria transaccion = (Transaccion_Bancaria) canalIn.receive();

				// Si la transacción es válida
				if (transaccion.is_valida()) {
					// Se escribe la transacción en el archivo de transacciones válidas
					escritorValidas.write(transaccion.toString());
					escritorValidas.newLine();

					// Si además es sospechosa (y por seguridad se verifica que la cadena con el motivo no es vacía), se escribe el motivo de sospechosidad en el archivo de transacciones válidas
					if (transaccion.is_sospechosa() && 
						transaccion.getMotivo_sospechosa() != null && 
						!transaccion.getMotivo_sospechosa().isEmpty()) {

							escritorValidas.write("\n\tMotivo: " + transaccion.getMotivo_sospechosa());
							escritorValidas.newLine();
					}
					escritorValidas.write("----------------------------------------");
					escritorValidas.newLine();

				// Si la transacción no es válida, se escribe en el archivo de transacciones inválidas
				} else {
					escritorInvalidas.write(transaccion.toString());
					escritorInvalidas.newLine();

					// Por seguridad, se verifica que la cadena con el motivo de invalidez no es vacía antes de escribirla 
					if (transaccion.getMotivo_invalida() != null 
						&& !transaccion.getMotivo_invalida().isEmpty()) {

							escritorInvalidas.write("\n\tMotivo: " + transaccion.getMotivo_invalida());
							escritorInvalidas.newLine();
					}
					escritorInvalidas.write("----------------------------------------");
					escritorInvalidas.newLine();
				}

				// Se envía la transacción al siguiente proceso a través del canal de salida
				canalOut.send(transaccion);
			}

		// Si ocurre alguna excepción de E/S al escribir en los archivos de texto, se lanza una RuntimeException 
		} catch (IOException e) {
			throw new RuntimeException("No se pudieron escribir los archivos de transacciones", e);
		}
	}
}
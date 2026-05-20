package Examen_3;

import Jpmi.*;
import java.util.List;

// Etapa 4 del Pipeline: Contabilizar el número de transacciones válidas, inválidas y sospechosas, y mostrar un resumen en pantalla, incluyendo los detalles de cada transacción
public class E4_Contabilizador_Display implements Proceso {
	// Canal de entrada para recibir las transacciones con la confirmación de validez y sospechosidad desde la etapa 3
	CanalSimple canalIn;

	// Variables para contabilizar el número de transacciones válidas, inválidas y sospechosas
	int num_validas, num_invalidas, num_sospechosas;

	// Constructor 
	public E4_Contabilizador_Display(CanalSimple canalIn) {
		this.canalIn = canalIn;
		this.num_validas = 0;
		this.num_invalidas = 0;
		this.num_sospechosas = 0;
	}

	// Método run 
	public void run() {
		// Se definen listas para almacenar las transacciones válidas, válidas sospechosas e inválidas, para luego mostrar los detalles de cada transacción en pantalla
		List<Transaccion_Bancaria> validas = new java.util.ArrayList<>();
		List<Transaccion_Bancaria> validasSospechosas = new java.util.ArrayList<>();
		List<Transaccion_Bancaria> invalidas = new java.util.ArrayList<>();

		// Se recorren las 10 transacciones recibidas 
		for (int i = 0; i < 10; i++) {
			Transaccion_Bancaria transaccion = (Transaccion_Bancaria) canalIn.receive();

			// Si la transacción es válida
			if (transaccion.is_valida()) {
				num_validas++;	// Se incrementa el contador de transacciones válidas

				// Si además es sospechosa 
				if (transaccion.is_sospechosa()) {
					num_sospechosas++;	// Se incrementa el contador de transacciones sospechosas
					validasSospechosas.add(transaccion);	// Se agrega la transacción a la lista 
															// de transacciones válidas sospechosas 
															// para mostrar sus detalles en pantalla

				} else {
					validas.add(transaccion);	// Se agrega la transacción a la lista de 
												// transacciones válidas no sospechosas para mostrar 
												// sus detalles en pantalla
				}

			// Si la no es válida, entonces es inválida
			} else {
				num_invalidas++;	// Se incrementa el contador de transacciones inválidas
				invalidas.add(transaccion);	// Se agrega la transacción a la lista de transacciones inválidas para mostrar sus detalles en pantalla
			}
		}

		// Se muestra en pantalla el número total de transacciones válidas, inválidas y sospechosas
		System.out.println("Número de transacciones válidas: " + num_validas);
		System.out.println("Número de transacciones inválidas: " + num_invalidas);
		System.out.println("Número de transacciones sospechosas: " + num_sospechosas);

		// Se recorre cada lista de transacciones válidas no sospechosas, válidas sospechosas e inválidas para mostrar los detalles de cada transacción en pantalla, incluyendo los motivos de invalidez o sospechosidad cuando corresponda

		// Se imprimen las validas no sospechosas
			System.out.println("\n--- TRANSACCIONES VALIDAS (no sospechosas) ---");

			if (validas.isEmpty()) {
				System.out.println("\tNo hay transacciones válidas no sospechosas.");
			} else {
				for (Transaccion_Bancaria transaccion_valida : validas) {
					System.out.println(transaccion_valida);
					System.out.println("----------------------------------------");
				}
			}

		// Se imprimen las válidas sospechosas (si es que hay alguna) 
			System.out.println("\n--- TRANSACCIONES VALIDAS SOSPECHOSAS ---");

			if (validasSospechosas.isEmpty()) {
				System.out.println("\tNo hay transacciones válidas sospechosas.");

			} else {
				for (Transaccion_Bancaria transaccion_sospechosa : validasSospechosas) {
					System.out.println(transaccion_sospechosa);

					// Se verifica que la cadena con el motivo de sospechosidad no es nula ni vacía
					if (transaccion_sospechosa.getMotivo_sospechosa() != null && !transaccion_sospechosa.getMotivo_sospechosa().isEmpty()) {
						System.out.println("\n\tMotivo: " + transaccion_sospechosa.getMotivo_sospechosa());
					}
					System.out.println("----------------------------------------");
				}
			}

		// Se imprimen las inválidas
			System.out.println("\n--- TRANSACCIONES INVALIDAS ---");

			if (invalidas.isEmpty()) {
				System.out.println("\tNo hay transacciones inválidas.");

			} else {
				for (Transaccion_Bancaria transaccion_invalida : invalidas) {
					System.out.println(transaccion_invalida);

					// Se verifica que la cadena con el motivo de invalidez no es nula ni vacía
					if (transaccion_invalida.getMotivo_invalida() != null && !transaccion_invalida.getMotivo_invalida().isEmpty()) {
						System.out.println("\n\tMotivo: " + transaccion_invalida.getMotivo_invalida());
					}
					System.out.println("----------------------------------------");
				}
			}
	}
}

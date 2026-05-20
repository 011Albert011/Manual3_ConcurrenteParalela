package Examen_3;

import java.time.LocalTime;
import java.util.Arrays;

import Jpmi.*;

// Etapa 1 del Pipeline: Verificar si las transacciones son válidas o no, y enviar la información al siguiente proceso
public class E1_Is_Valida implements Proceso {
	// Lista de países registrados para validar las transacciones, donde solo se aceptan transacciones de estos países, el resto se marca como inválida
	String[] PaisesRegistrados = {"México", "Japón", "Canadá", "Colombia", "Venezuela"};

	// Canales de entrada y salida para recibir las transacciones generadas en la etapa 0 y enviar las transacciones con la confirmación de validez al siguiente proceso
	private CanalSimple canalIn;
	private CanalSimple canalOut;

	// Constructor
	public E1_Is_Valida(CanalSimple canalIn, CanalSimple canalOut) {
		this.canalIn = canalIn;
		this.canalOut = canalOut;
	}

	// Método run 
	public void run() {
		// Se recorren las 10 transacciones recibidas a través del canal de entrada
		for (int i = 0; i < 10; i++) {
			Transaccion_Bancaria transaccion = (Transaccion_Bancaria) canalIn.receive();

			// Se inicializa la transacción como no válida y se limpia el motivo de invalidez para llenarlo
			transaccion.setEs_valida(false);
			transaccion.setMotivo_invalida("");

			// Se verifican las condiciones para determinar si la transacción es válida o no

				// Condición 1: El monto de la transacción debe ser mayor a 0
				boolean montoOk = transaccion.getMonto_transaccion() > 0;


				// Condición 2: La hora de la transacción debe estar entre las 8:00 y las 13:00 (se incluyen ambos extremos - INCLUSIVO)
				boolean horaOk = !transaccion.getHora_transaccion().isBefore(LocalTime.of(7, 59)) && !transaccion.getHora_transaccion().isAfter(LocalTime.of(13, 1));

				// Condición 3: El país de origen debe estar registrado en la lista de países aceptados
				boolean paisOk = Arrays.asList(PaisesRegistrados).contains(transaccion.getPais_origen());

			// Si la transacción cumple con todas las condiciones, se marca como válida, de lo contrario se deja como inválida y se llenan los motivos correspondientes
			if (montoOk && horaOk && paisOk) {
				transaccion.setEs_valida(true);
			} else {
				StringBuilder motivos = new StringBuilder();
				if (!montoOk) motivos.append("Monto <= 0");
				if (!horaOk) {
					if (motivos.length() == 0) motivos.append("Fuera de horario"); else motivos.append("; Fuera de horario");
				}
				if (!paisOk) {
					if (motivos.length() == 0) motivos.append("Pais no registrado"); else motivos.append("; Pais no registrado");
				}
				transaccion.setMotivo_invalida(motivos.toString());
			}

			// Se envía la transacción con la confirmación de validez al siguiente proceso a través del canal de salida
			canalOut.send(transaccion);
		}
	}

}

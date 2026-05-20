package Examen_3;

import Jpmi.*;

// Etapa 2 del Pipeline: Verificar si las transacciones válidas son sospechosas según el monto y el país de origen, y luego enviar la transacción con la confirmación de sospechosidad al siguiente proceso
public class E2_Is_Sospechosa implements Proceso {
	// Canales de entrada y salida para recibir las transacciones con la confirmación de validez desde la etapa 1 y enviar las transacciones a la siguiente etapa del pipeline
	private CanalSimple canalIn;
	private CanalSimple canalOut;

	// Constructor
	public E2_Is_Sospechosa(CanalSimple canalIn, CanalSimple canalOut) {
		this.canalIn = canalIn;
		this.canalOut = canalOut;
	}

	// Método run 
	public void run() {
		// Se recorren las 10 transacciones recibidas a través del canal de entrada
		for (int i = 0; i < 10; i++) {
			Transaccion_Bancaria transaccion = (Transaccion_Bancaria) canalIn.receive();

			// Si la transacción no es válida, se envía directamente al siguiente proceso sin verificar si es sospechosa, ya que solo las transacciones válidas pueden ser sospechosas
			if (!transaccion.is_valida()) {
				canalOut.send(transaccion);
				continue;
			}

			// Se limpia el motivo de sospechosidad para llenarlo 
			transaccion.setMotivo_sospechosa("");

			// Se verifican las condiciones para determinar si la transacción válida es sospechosa o no

				// Condición 1: El monto de la transacción es mayor a 50,000
				boolean montoAlto = transaccion.getMonto_transaccion() > 50000;

				// Condición 2: El país de origen es de alto riesgo 
				boolean paisRiesgoso = transaccion.is_altoRiesgo();

			// Si la transacción cumple con alguna de las condiciones, se marca como sospechosa y se llenan los motivos correspondientes
			if (montoAlto || paisRiesgoso) {
				transaccion.setEs_sospechosa(true);

				StringBuilder motivos = new StringBuilder();
				
				if (montoAlto) motivos.append("Monto > 50000");
				if (paisRiesgoso) {
					if (motivos.length() == 0) motivos.append("Pais de alto riesgo"); else motivos.append("; Pais de alto riesgo");
				}
				transaccion.setMotivo_sospechosa(motivos.toString());
			}

			// Se envía la transacción con la confirmación de sospechosidad al siguiente proceso a través del canal de salida
			canalOut.send(transaccion);
		}
	}
}

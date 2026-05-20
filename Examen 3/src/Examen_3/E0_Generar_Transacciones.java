package Examen_3;

import Jpmi.*;

// Etapa 0 del Pipeline: Generar transacciones bancarias aleatorias y enviarlas al siguiente proceso
public class E0_Generar_Transacciones implements Proceso {
	// Canal de salida para enviar las transacciones generadas al siguiente proceso
	private CanalSimple canalOut;
	// Arreglo para almacenar las transacciones generadas, se generan 10 transacciones para el pipeline
	private Transaccion_Bancaria [] transacciones = new Transaccion_Bancaria[10];

	// Constructor que recibe el canal de salida y genera las transacciones aleatorias al instanciar la clase
	public E0_Generar_Transacciones(CanalSimple canalOut) {
		this.canalOut = canalOut;
		for (int i = 0; i < transacciones.length; i++) {
			transacciones[i] = Transaccion_Bancaria.generarAleatoria();
		}
	}

	// Método run que envía las transacciones generadas al siguiente proceso a través del canal de salida
	public void run() {
		for(Transaccion_Bancaria transaccion : transacciones) {
			canalOut.send(transaccion);
		}	
	}
}
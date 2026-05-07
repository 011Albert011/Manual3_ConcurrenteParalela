package ejercicio_2;

import Jpmi.*;

// Proceso para leer un valor de un canal simple
public class Lee implements Proceso {
	CanalSimple in;	// Canal de entrada
	Object valor;

	public Lee(CanalSimple in) {
		this.in = in;
		valor = null;
	}

	public void run() {
		// Lee un valor del canal de entrada
		valor = in.receive();
	}

	// Método para obtener el valor leído
	public Object getValor() {
		return valor;
	}
}

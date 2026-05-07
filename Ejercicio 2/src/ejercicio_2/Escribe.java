package ejercicio_2;

import Jpmi.*;

// Proceso para escribir un valor en un canal simple
public class Escribe implements Proceso {
	CanalSimple out; // Canal de salida
	Object valor;

	public Escribe(CanalSimple out, Object valor) {
		this.out = out;
		this.valor = valor;
	}

	public void run() {
		// Escribe el valor en el canal de salida
		out.send(valor);
	}
}

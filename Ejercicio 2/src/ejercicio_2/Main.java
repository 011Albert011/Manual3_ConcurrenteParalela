package ejercicio_2;

import Jpmi.*;

// Proceso principal para ejecutar el programa con los procesos Generador, inCharsOutChars y Display
public class Main {
	public static void main(String[] args) {
		// Creación de los canales simples para la comunicación entre los procesos
		CanalSimple canalChar1 = new CanalSimple();
		CanalSimple canalChar2 = new CanalSimple();
		CanalSimple canalDisplay = new CanalSimple();

		// Creación de un proceso paralelo que ejecuta los procesos Generador, inCharsOutChars y Display
		Paralelo paralelo = new Paralelo(new Proceso[] {
			new Generador(canalChar1, canalChar2),
			new inCharsOutChars(canalChar1, canalChar2, canalDisplay),
			new Display(canalDisplay)
		});

		paralelo.run();
	}
}

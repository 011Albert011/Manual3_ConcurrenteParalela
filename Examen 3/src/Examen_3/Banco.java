package Examen_3;

import Jpmi.*;

// Clase principal que ejecuta el pipeline completo, creando los canales de comunicación entre las etapas y ejecutando cada proceso en paralelo
public class Banco {
	public static void main(String[] args) {
		// Se crean los canales de comunicación entre las etapas del pipeline
		
			// Canal 0: Entre E0_Generar_Transacciones y E1_Is_Valida
			CanalSimple canal0 = new CanalSimple();
			// Canal 1: Entre E1_Is_Valida y E2_Is_Sospechosa
			CanalSimple canal1 = new CanalSimple();
			// Canal 2: Entre E2_Is_Sospechosa y E3_Archivos_Texto
			CanalSimple canal2 = new CanalSimple();
			// Canal 3: Entre E3_Archivos_Texto y E4_Contabilizador_Display
			CanalSimple canal3 = new CanalSimple();

		// Se ejecuta cada proceso del pipeline en paralelo, pasando los canales de comunicación correspondientes a cada proceso
		Paralelo paralelo = new Paralelo(new Proceso[] {

			// Se crean las instancias de cada proceso del pipeline, y se puede ver cómo sigue el flujo concreto de las transacciones a través de los canales de comunicación entre cada etapa del pipeline
			new E0_Generar_Transacciones(canal0),
			new E1_Is_Valida(canal0, canal1),
			new E2_Is_Sospechosa(canal1, canal2),
			new E3_Archivos_Texto(canal2, canal3),
			new E4_Contabilizador_Display(canal3)
		});

		// Se ejecuta el pipeline completo en paralelo
		paralelo.run();
	}
}

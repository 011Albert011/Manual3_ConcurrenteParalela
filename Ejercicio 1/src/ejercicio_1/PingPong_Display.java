package ejercicio_1;

import Jpmi.*;

// Clase principal para ejecutar el "juego" de Ping Pong entre A y B
public class PingPong_Display {
	public static void main(String[] args) {
		// Creación de canales para la comunicación entre A y B
		CanalSimple canalAtoB = new CanalSimple();
		CanalSimple canalBtoA = new CanalSimple();

		// Creación de un proceso paralelo que ejecuta tanto a A como a B
		Paralelo paralelo = new Paralelo(new Proceso[]{
			new PingPong_A(canalAtoB, canalBtoA),
			new PingPong_B(canalAtoB, canalBtoA)
		});

		paralelo.run();
	}
}

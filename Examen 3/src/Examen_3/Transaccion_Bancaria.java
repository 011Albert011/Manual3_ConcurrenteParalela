package Examen_3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Clase que representa una transacción bancaria
public class Transaccion_Bancaria {
	// Mapa de países y su nivel de riesgo asociado
	private static final Map<String, String> PAISES_RIESGOS = new HashMap<>();

	// Inicialización estática del mapa de países y riesgos. Notar que no todos los paises estan "registrados" ante el banco (solo lo estan México, Japón, Canadá, Colombia y Venezuela), lo que se valida en la etapa 1 del pipeline
	static {
		PAISES_RIESGOS.put("México", "NORMAL");
		PAISES_RIESGOS.put("Japón", "NORMAL");
		PAISES_RIESGOS.put("Canadá", "NORMAL");
		PAISES_RIESGOS.put("Brasil", "NORMAL");
		PAISES_RIESGOS.put("Islandia", "NORMAL");
		PAISES_RIESGOS.put("Siria", "ALTO_RIESGO");
		PAISES_RIESGOS.put("Colombia", "ALTO_RIESGO");
		PAISES_RIESGOS.put("Venezuela", "ALTO_RIESGO");
	}

	// Atributos de la transacción
	private String id_transaccion;
	private String id_cliente;
	private double monto_transaccion;
	private String pais_origen;
	private String nivel_riesgo;
	private LocalTime hora_transaccion;

	// Estados de la transacción (estos se actualizan a medida que se procesa la transacción)
	private boolean es_valida;
	private boolean es_sospechosa;

	// Motivos para marcar la transacción como inválida o sospechosa (se llenan durante el procesamiento)
	private String motivo_invalida = "";
	private String motivo_sospechosa = "";

	// Constructor para crear una transacción bancaria con todos los atributos
	public Transaccion_Bancaria(String id_transaccion, String id_cliente, double monto_transaccion, String pais_origen, String nivel_riesgo, LocalTime hora_transaccion) {
			this.id_transaccion = id_transaccion;
			this.id_cliente = id_cliente;
			this.monto_transaccion = monto_transaccion;
			this.pais_origen = pais_origen;
			this.nivel_riesgo = nivel_riesgo;
			this.hora_transaccion = hora_transaccion;

			// Inicialmente, la transacción no se ha validado ni marcado como sospechosa, esto se actualiza durante el procesamiento en las etapas del pipeline
			this.es_valida = false;
			this.es_sospechosa = false;
	}

	// Método estático para generar una transacción aleatoria, y así cambie con cada ejecución del programa
	public static Transaccion_Bancaria generarAleatoria() {
		Random random = new Random();

		// Se genera una lista de países a partir de las claves del mapa de países y riesgos, para luego seleccionar uno aleatoriamente y obtener su nivel de riesgo asociado
		List<String> paises = new ArrayList<>(PAISES_RIESGOS.keySet());
		String pais_aleatorio = paises.get(random.nextInt(paises.size()));
		String riesgo = PAISES_RIESGOS.get(pais_aleatorio);

		// Se generan los IDs aleatorios para la transacción
		String id_transaccion = "TRN" + (10000 + random.nextInt(90000));
		String id_cliente = "CLT" + (100 + random.nextInt(9000));

		// Se genera un monto aleatorio entre 0 y 60000, para que haya casos válidos, inválidos (monto = 0) y sospechosos (monto > 50000)
		double monto_transaccion = random.nextInt(60001);

		// Se genera una hora aleatoria entre las 7:00 y las 14:00, para que haya casos válidos (entre 8:00 y 13:00) e inválidos (fuera de ese rango)
		LocalTime hora_transaccion = LocalTime.of(7 + random.nextInt(8), 0, 0);

		// Se retorna la nueva transacción con los datos aleatorios generados
		return new Transaccion_Bancaria(
			id_transaccion, 
			id_cliente, 
			monto_transaccion, 
			pais_aleatorio, 
			riesgo, 
			hora_transaccion);
	}

	// Getters
	public String getId_transaccion() {
		return id_transaccion;
	}

	public String getId_cliente() {
		return id_cliente;
	}

	public double getMonto_transaccion() {
		return monto_transaccion;
	}

	public String getPais_origen() {
		return pais_origen;
	}

	public String getNivel_riesgo() {
		return nivel_riesgo;
	}

	public LocalTime getHora_transaccion() {
		return hora_transaccion;
	}

	// Métodos para verificar si la transacción es válida o sospechosa, que se llenan durante el procesamiento de la transacción
	public boolean is_valida() {
		return es_valida;
	}

	public boolean is_sospechosa() {
		return es_sospechosa;
	}

	
	public boolean is_altoRiesgo() {
		return "ALTO_RIESGO".equals(nivel_riesgo);
	}
	
	// Setters y getters para actualizar los motivos de invalidez o sospechosidad, que se llenan durante el procesamiento de la transacción
	public String getMotivo_invalida() {
		return motivo_invalida;
	}

	public void setMotivo_invalida(String motivo_invalida) {
		this.motivo_invalida = motivo_invalida;
	}

	public String getMotivo_sospechosa() {
		return motivo_sospechosa;
	}

	public void setMotivo_sospechosa(String motivo_sospechosa) {
		this.motivo_sospechosa = motivo_sospechosa;
	}
	
	// Setter para marcar la transacción como validada
	public void setEs_valida(boolean valida) {
		this.es_valida = valida;
	}

	// Setter para marcar la transacción como sospechosa
	public void setEs_sospechosa(boolean sospechosa) {
		this.es_sospechosa = sospechosa;
	}
	
	// Método toString para mostrar la información de la transacción de forma legible, incluyendo los motivos de invalidez o sospechosidad si aplican
	@Override
	public String toString() {
		DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm:ss");
		String validaTexto = es_valida ? "VERDADERO" : "FALSO";
		String sospechosaTexto = es_sospechosa ? "VERDADERO" : "FALSO";
		return String.format(
			"\tID transaccion: %s%n" +
			"\tID cliente: %s%n" +
			"\tMonto transaccion: %.2f%n" +
			"\tPais origen: %s%n" +
			"\tNivel de riesgo: %s%n" +
			"\tHora transaccion: %s%n" +
			"\tValida: %s%n" +
			"\tSospechosa: %s",
			id_transaccion,
			id_cliente,
			monto_transaccion,
			pais_origen,
			nivel_riesgo,
			hora_transaccion.format(tf),
			validaTexto,
			sospechosaTexto
		);
	}
}
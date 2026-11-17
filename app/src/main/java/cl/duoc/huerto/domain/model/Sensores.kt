package cl.duoc.huerto.domain.model

/** De dónde viene la lectura. Las simuladas se marcan como "Dato de prueba". */
enum class OrigenLectura { SIMULADA, MANUAL }

enum class EstadoLectura { BAJO, NORMAL, ALTO }

/**
 * Cada variable con su unidad, el rango válido para registrar y los umbrales
 * que se consideran normales. Es solo informativo: la app no actúa sobre el riego.
 */
enum class Variable(
    val etiqueta: String,
    val unidad: String,
    val min: Double,
    val max: Double,
    val umbralBajo: Double,
    val umbralAlto: Double
) {
    HUMEDAD_SUELO("Humedad del suelo", "%", 0.0, 100.0, 30.0, 80.0),
    TEMPERATURA("Temperatura", "°C", -10.0, 50.0, 5.0, 32.0),
    HUMEDAD_AMBIENTE("Humedad ambiente", "%", 0.0, 100.0, 30.0, 85.0);

    fun evaluar(valor: Double): EstadoLectura = when {
        valor < umbralBajo -> EstadoLectura.BAJO
        valor > umbralAlto -> EstadoLectura.ALTO
        else -> EstadoLectura.NORMAL
    }
}

package cl.duoc.huerto.domain

import cl.duoc.huerto.data.entities.LecturaSensor
import kotlin.math.round
import kotlin.random.Random

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

fun EstadoLectura.etiqueta() = when (this) {
    EstadoLectura.BAJO -> "Bajo"
    EstadoLectura.NORMAL -> "Normal"
    EstadoLectura.ALTO -> "Alto"
}

fun OrigenLectura.etiqueta() = when (this) {
    OrigenLectura.SIMULADA -> "Dato de prueba"
    OrigenLectura.MANUAL -> "Registro manual"
}

private fun redondear(v: Double) = round(v * 10) / 10

/** Acepta coma o punto decimal ("22,5" o "22.5"). */
fun parseNumero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

/** Genera una lectura de prueba con valores realistas (a veces fuera de lo normal, para ver los estados). */
fun simularLectura(sectorId: Int, ahora: Long, random: Random = Random.Default) = LecturaSensor(
    sectorId = sectorId,
    fecha = ahora,
    humedadSuelo = redondear(random.nextDouble(15.0, 90.0)),
    temperatura = redondear(random.nextDouble(3.0, 36.0)),
    humedadAmbiente = redondear(random.nextDouble(25.0, 95.0)),
    origen = OrigenLectura.SIMULADA
)

// ---------------- Validación del registro manual ----------------

data class ErroresLectura(
    val humedadSuelo: String? = null,
    val temperatura: String? = null,
    val humedadAmbiente: String? = null
) {
    val hayErrores: Boolean get() = listOf(humedadSuelo, temperatura, humedadAmbiente).any { it != null }
}

private fun formatoRango(v: Double) = if (v % 1.0 == 0.0) v.toInt().toString() else v.toString()

private fun validarValor(texto: String, variable: Variable): String? {
    if (texto.isBlank()) return "Ingresa un valor"
    val n = parseNumero(texto) ?: return "Debe ser un número (ej: 22,5)"
    return if (n < variable.min || n > variable.max)
        "Debe estar entre ${formatoRango(variable.min)} y ${formatoRango(variable.max)} ${variable.unidad}"
    else null
}

fun validarLectura(humedadSuelo: String, temperatura: String, humedadAmbiente: String) = ErroresLectura(
    humedadSuelo = validarValor(humedadSuelo, Variable.HUMEDAD_SUELO),
    temperatura = validarValor(temperatura, Variable.TEMPERATURA),
    humedadAmbiente = validarValor(humedadAmbiente, Variable.HUMEDAD_AMBIENTE)
)

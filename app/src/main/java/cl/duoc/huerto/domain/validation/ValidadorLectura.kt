package cl.duoc.huerto.domain.validation

import cl.duoc.huerto.domain.model.Variable

/** Acepta coma o punto decimal ("22,5" o "22.5"). */
fun parseNumero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

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

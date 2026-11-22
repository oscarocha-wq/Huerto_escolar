package cl.duoc.huerto.domain.model

/** Clima actual del huerto, obtenido de la API externa Open-Meteo. */
data class Clima(
    val temperatura: Double,
    val humedad: Int,
    val lluviaHoyMm: Double,
    val maxima: Double?,
    val minima: Double?,
    val codigo: Int
)

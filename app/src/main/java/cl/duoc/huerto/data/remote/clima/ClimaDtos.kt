package cl.duoc.huerto.data.remote.clima

import cl.duoc.huerto.domain.model.Clima

/*
 * Forma del JSON de Open-Meteo. Los nombres van tal cual vienen en el JSON
 * (temperature_2m, relative_humidity_2m...) para que Gson los lea sin anotaciones.
 */

data class ClimaRespuestaDto(
    val current: ClimaActualDto?,
    val daily: ClimaDiarioDto?
) {
    /** null si la respuesta no trae el clima actual. */
    fun toDomain(): Clima? {
        val actual = current ?: return null
        return Clima(
            temperatura = actual.temperature_2m ?: return null,
            humedad = actual.relative_humidity_2m?.toInt() ?: 0,
            lluviaHoyMm = daily?.precipitation_sum?.firstOrNull() ?: actual.precipitation ?: 0.0,
            maxima = daily?.temperature_2m_max?.firstOrNull(),
            minima = daily?.temperature_2m_min?.firstOrNull(),
            codigo = actual.weather_code ?: -1
        )
    }
}

data class ClimaActualDto(
    val temperature_2m: Double?,
    val relative_humidity_2m: Double?,
    val precipitation: Double?,
    val weather_code: Int?
)

data class ClimaDiarioDto(
    val precipitation_sum: List<Double>?,
    val temperature_2m_max: List<Double>?,
    val temperature_2m_min: List<Double>?
)

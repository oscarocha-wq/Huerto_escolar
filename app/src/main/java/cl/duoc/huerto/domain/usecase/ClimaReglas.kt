package cl.duoc.huerto.domain.usecase

import cl.duoc.huerto.domain.model.Clima

/** Lluvia del día (mm) desde la cual no hace falta regar. */
const val LLUVIA_SUFICIENTE_MM = 5.0

/** Temperatura (°C) desde la cual conviene regar temprano o al atardecer. */
const val CALOR_FUERTE_C = 30.0

/** Traduce el código del clima (WMO) que entrega Open-Meteo a un texto en español. */
fun descripcionClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1, 2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Neblina"
    in 51..57 -> "Llovizna"
    in 61..67 -> "Lluvia"
    in 71..77 -> "Nieve"
    in 80..82 -> "Chubascos"
    in 95..99 -> "Tormenta"
    else -> "Sin información"
}

/** Consejo de riego para los estudiantes según el clima del día. */
fun consejoRiego(clima: Clima): String = when {
    clima.lluviaHoyMm >= LLUVIA_SUFICIENTE_MM -> "Hoy llueve lo suficiente: no es necesario regar."
    clima.temperatura >= CALOR_FUERTE_C -> "Hace mucho calor: rieguen temprano o al atardecer."
    clima.humedad < 30 -> "Ambiente muy seco: revisen la humedad del suelo."
    else -> "Rieguen según la frecuencia de cada cultivo."
}

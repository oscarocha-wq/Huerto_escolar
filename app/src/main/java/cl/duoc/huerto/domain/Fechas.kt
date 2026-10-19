package cl.duoc.huerto.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.time.LocalDate
import java.time.ZoneOffset

/** Formatea fechas elegidas con el DatePicker (guardadas como medianoche UTC). */
fun formatearFechaUtc(millis: Long?): String {
    if (millis == null) return ""
    val formato = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL"))
    formato.timeZone = TimeZone.getTimeZone("UTC")
    return formato.format(Date(millis))
}

/** Hoy a medianoche UTC, con la misma convención que el DatePicker. */
fun inicioDeHoyUtc(hoy: LocalDate = LocalDate.now()): Long =
    hoy.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** Para fecha y hora reales (realización, validación), en hora local. */
fun formatearFechaHora(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(millis))
package cl.duoc.huerto.domain

data class ErroresCultivo(
    val nombre: String? = null,
    val sector: String? = null,
    val frecuencia: String? = null,
    val trasplante: String? = null,
    val cosecha: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, sector, frecuencia, trasplante, cosecha).any { it != null }
}

fun validarCultivo(
    nombre: String,
    sectorId: Int?,
    frecuenciaTexto: String,
    fechaSiembra: Long?,
    fechaTrasplante: Long?,
    fechaCosecha: Long?
): ErroresCultivo {
    val frecuencia = frecuenciaTexto.toIntOrNull()
    return ErroresCultivo(
        nombre = if (nombre.isBlank()) "El nombre es obligatorio" else null,
        sector = if (sectorId == null) "Selecciona un sector" else null,
        frecuencia = if (frecuencia == null || frecuencia !in 1..60)
            "Ingresa los días entre riegos (1 a 60)" else null,
        trasplante = if (fechaSiembra != null && fechaTrasplante != null && fechaTrasplante < fechaSiembra)
            "No puede ser anterior a la siembra" else null,
        cosecha = if (fechaSiembra != null && fechaCosecha != null && fechaCosecha < fechaSiembra)
            "No puede ser anterior a la siembra" else null
    )
}
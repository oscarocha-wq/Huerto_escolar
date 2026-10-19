package cl.duoc.huerto.domain

data class ErroresCultivo(
    val nombre: String? = null,
    val sector: String? = null,
    val frecuencia: String? = null,
    val trasplante: String? = null,
    val cosecha: String? = null,
    val descripcion: String? = null,
    val observaciones: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, sector, frecuencia, trasplante, cosecha, descripcion, observaciones)
            .any { it != null }
}

fun validarCultivo(
    nombre: String,
    sectorId: Int?,
    frecuenciaTexto: String,
    fechaSiembra: Long?,
    fechaTrasplante: Long?,
    fechaCosecha: Long?,
    descripcion: String = "",
    observaciones: String = ""
) = ErroresCultivo(
    nombre = Reglas.requeridoYLargo(nombre, "el nombre del cultivo", LimitesCampo.CULTIVO_NOMBRE),
    sector = if (sectorId == null) "Selecciona un sector" else null,
    frecuencia = Reglas.enteroPositivo(
        frecuenciaTexto, "cada cuántos días se riega", LimitesCampo.RIEGO_MAX_DIAS
    ),
    trasplante = if (fechaSiembra != null && fechaTrasplante != null && fechaTrasplante < fechaSiembra)
        "No puede ser anterior a la siembra" else null,
    cosecha = if (fechaSiembra != null && fechaCosecha != null && fechaCosecha < fechaSiembra)
        "No puede ser anterior a la siembra" else null,
    descripcion = Reglas.largo(descripcion, "la descripción", LimitesCampo.CULTIVO_DESCRIPCION),
    observaciones = Reglas.largo(observaciones, "las observaciones", LimitesCampo.COMENTARIO)
)
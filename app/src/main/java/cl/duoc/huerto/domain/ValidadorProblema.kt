package cl.duoc.huerto.domain
const val MIN_DESCRIPCION_PROBLEMA = 10
const val MAX_DESCRIPCION_PROBLEMA = 300

data class ErroresProblema(
    val tipo: String? = null,
    val descripcion: String? = null,
    val sector: String? = null
) {
    val hayErrores: Boolean get() = tipo != null || descripcion != null || sector != null
}

fun validarProblema(tipo: TipoProblema?, descripcion: String, sectorId: Int?): ErroresProblema {
    val largo = descripcion.trim().length
    return ErroresProblema(
        tipo = if (tipo == null) "Selecciona el tipo de problema" else null,
        descripcion = when {
            largo == 0 -> "Describe brevemente el problema"
            largo < MIN_DESCRIPCION_PROBLEMA -> "Escribe al menos $MIN_DESCRIPCION_PROBLEMA caracteres"
            largo > MAX_DESCRIPCION_PROBLEMA -> "Máximo $MAX_DESCRIPCION_PROBLEMA caracteres"
            else -> null
        },
        sector = if (sectorId == null) "Selecciona un sector" else null
    )
}
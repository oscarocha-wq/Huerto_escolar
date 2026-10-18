package cl.duoc.huerto.domain

data class ErroresTarea(
    val tipo: String? = null,
    val descripcion: String? = null,
    val sector: String? = null,
    val fecha: String? = null,
    val asignado: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(tipo, descripcion, sector, fecha, asignado).any { it != null }
}

fun validarTarea(
    tipo: TipoTarea?,
    descripcion: String,
    sectorId: Int?,
    fechaProgramada: Long?,
    tieneResponsable: Boolean
) = ErroresTarea(
    tipo = if (tipo == null) "Selecciona el tipo de tarea" else null,
    descripcion = if (descripcion.isBlank()) "Escribe las instrucciones" else null,
    sector = if (sectorId == null) "Selecciona un sector" else null,
    fecha = if (fechaProgramada == null) "Selecciona la fecha programada" else null,
    asignado = if (!tieneResponsable) "Selecciona un estudiante o un grupo" else null
)
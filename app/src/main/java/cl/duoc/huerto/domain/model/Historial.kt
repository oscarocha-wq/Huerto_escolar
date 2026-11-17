package cl.duoc.huerto.domain.model

enum class TipoEvento {
    TAREA_ASIGNADA, TAREA_ENVIADA, TAREA_VALIDADA, TAREA_RECHAZADA,
    PROBLEMA_REPORTADO, PROBLEMA_ATENDIDO
}

data class EventoHistorial(
    val clave: String,               // única: sirve como key en la lista
    val tipo: TipoEvento,
    val fecha: Long,
    val titulo: String,
    val detalle: String,
    val tareaId: Int? = null,
    val problemaId: Int? = null
)

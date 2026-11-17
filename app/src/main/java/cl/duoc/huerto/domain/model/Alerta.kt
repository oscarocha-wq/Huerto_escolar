package cl.duoc.huerto.domain.model

enum class SeveridadAlerta { URGENTE, AVISO, INFO }

enum class TipoAlerta {
    TAREA_ATRASADA, TAREA_RECHAZADA, RIEGO_ATRASADO, RIEGO_PROXIMO,
    COSECHA_PROXIMA, MANTENIMIENTO, POR_VALIDAR, PROBLEMA_PENDIENTE
}

data class Alerta(
    val clave: String,              // identificador único para la lista
    val tipo: TipoAlerta,
    val severidad: SeveridadAlerta,
    val titulo: String,
    val detalle: String,
    val fecha: Long?,               // solo para ordenar
    val tareaId: Int? = null,
    val problemaId: Int? = null
)

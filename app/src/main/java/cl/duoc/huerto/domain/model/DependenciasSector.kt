package cl.duoc.huerto.domain.model

/** Cuántos registros dependen de un sector. */
data class DependenciasSector(
    val cultivos: Int = 0,
    val tareas: Int = 0,
    val problemas: Int = 0
)

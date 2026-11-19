package cl.duoc.huerto.domain.model

/** Lo que se trajo del servidor en una sincronización. */
data class ResultadoSincronizacion(
    val usuarios: Int,
    val sectores: Int,
    val cultivos: Int,
    val tareas: Int,
    val problemas: Int
) {
    val total: Int get() = usuarios + sectores + cultivos + tareas + problemas
}

package cl.duoc.huerto.domain

/** Cuántos registros dependen de un sector. */
data class DependenciasSector(
    val cultivos: Int = 0,
    val tareas: Int = 0,
    val problemas: Int = 0
)

private fun cantidad(n: Int, singular: String, plural: String) = "$n ${if (n == 1) singular else plural}"

/**
 * Un sector solo se elimina si no tiene nada asociado.
 * Así no se pierden cultivos, tareas ni el historial de problemas.
 */
fun motivoNoEliminarSector(d: DependenciasSector): String? {
    val partes = buildList {
        if (d.cultivos > 0) add(cantidad(d.cultivos, "cultivo", "cultivos"))
        if (d.tareas > 0) add(cantidad(d.tareas, "tarea", "tareas"))
        if (d.problemas > 0) add(cantidad(d.problemas, "problema reportado", "problemas reportados"))
    }
    return if (partes.isEmpty()) null
    else "No se puede eliminar: el sector tiene ${partes.joinToString(", ")}. " +
        "Puedes marcarlo como \"Fuera de uso\"."
}

/** Un cultivo solo se elimina si ninguna tarea lo usa. */
fun motivoNoEliminarCultivo(tareas: Int): String? =
    if (tareas == 0) null
    else "No se puede eliminar: el cultivo tiene ${cantidad(tareas, "tarea", "tareas")}. " +
        "Puedes marcarlo como \"Finalizado\"."

/** Solo el administrador, solo si nadie la ha iniciado ni entregado (se conserva el historial). */
fun puedeEliminarTarea(sesion: Sesion?, estado: EstadoTarea, entregas: Int): Boolean =
    sesion?.esAdministrador == true && estado == EstadoTarea.PENDIENTE && entregas == 0

package cl.duoc.huerto.domain

data class ResumenAvisos(
    val atrasadas: Int = 0,
    val porValidar: Int = 0,
    val problemasNuevos: Int = 0
)

/** Texto del aviso según el perfil. Devuelve null si no hay nada que avisar. */
fun ResumenAvisos.mensaje(esAdmin: Boolean): String? {
    val partes = buildList {
        if (atrasadas > 0)
            add(if (atrasadas == 1) "1 tarea atrasada" else "$atrasadas tareas atrasadas")
        if (esAdmin && porValidar > 0)
            add(if (porValidar == 1) "1 tarea por validar" else "$porValidar tareas por validar")
        if (esAdmin && problemasNuevos > 0)
            add(
                if (problemasNuevos == 1) "1 problema sin revisar"
                else "$problemasNuevos problemas sin revisar"
            )
    }
    return if (partes.isEmpty()) null else partes.joinToString(" · ")
}
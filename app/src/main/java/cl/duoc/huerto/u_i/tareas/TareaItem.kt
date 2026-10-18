package cl.duoc.huerto.u_i.tareas

import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.entities.Tarea
import cl.duoc.huerto.data.entities.Usuario
import cl.duoc.huerto.domain.EstadoTarea
import cl.duoc.huerto.domain.estaAtrasada
import cl.duoc.huerto.domain.inicioDeHoyUtc

data class TareaItem(
    val tarea: Tarea,
    val sectorNombre: String,
    val cultivoNombre: String?,
    val asignadoTexto: String,
    val atrasada: Boolean
)

enum class FiltroTarea(val titulo: String) {
    TODAS("Todas"),
    PENDIENTES("Pendientes"),
    POR_VALIDAR("Por validar"),
    ATRASADAS("Atrasadas"),
    VALIDADAS("Validadas");

    fun acepta(item: TareaItem): Boolean = when (this) {
        TODAS -> true
        PENDIENTES -> item.tarea.estado in setOf(
            EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO, EstadoTarea.RECHAZADA
        )
        POR_VALIDAR -> item.tarea.estado == EstadoTarea.PENDIENTE_DE_VALIDACION
        ATRASADAS -> item.atrasada
        VALIDADAS -> item.tarea.estado == EstadoTarea.VALIDADA
    }
}

fun construirItems(
    tareas: List<Tarea>,
    sectores: List<Sector>,
    cultivos: List<Cultivo>,
    estudiantes: List<Usuario>
): List<TareaItem> {
    val hoy = inicioDeHoyUtc()
    val nombresSector = sectores.associate { it.id to "${it.codigo} · ${it.nombre}" }
    val nombresCultivo = cultivos.associate { it.id to it.nombre }
    val nombresEstudiante = estudiantes.associate { it.id to it.nombre }

    return tareas.map { t ->
        TareaItem(
            tarea = t,
            sectorNombre = nombresSector[t.sectorId] ?: "Sector desconocido",
            cultivoNombre = t.cultivoId?.let { nombresCultivo[it] },
            asignadoTexto = t.asignadoId?.let { nombresEstudiante[it] ?: it }
                ?: t.grupo?.let { "Grupo $it" }
                ?: "Sin asignar",
            atrasada = estaAtrasada(t.estado, t.fechaProgramada, hoy)
        )
    }
}
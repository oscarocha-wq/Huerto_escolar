package cl.duoc.huerto.domain

import cl.duoc.huerto.data.entities.Problema
import cl.duoc.huerto.data.entities.Tarea

private val ABIERTAS_PANEL = setOf(
    EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO, EstadoTarea.RECHAZADA
)

data class ResumenPanel(
    val pendientes: Int = 0,
    val atrasadas: Int = 0,
    val porValidar: Int = 0,
    val validadas: Int = 0,
    val problemasAbiertos: Int = 0,
    val alertas: Int = 0
)

fun construirResumen(
    sesion: Sesion?,
    tareas: List<Tarea>,
    problemas: List<Problema>,
    totalAlertas: Int,
    hoy: Long
): ResumenPanel {
    if (sesion == null) return ResumenPanel()
    val visibles = tareas.filter { puedeVerTarea(sesion, it.asignadoId, it.grupo) }
    return ResumenPanel(
        pendientes = visibles.count { it.estado in ABIERTAS_PANEL },
        atrasadas = visibles.count { estaAtrasada(it.estado, it.fechaProgramada, hoy) },
        porValidar = visibles.count { it.estado == EstadoTarea.PENDIENTE_DE_VALIDACION },
        validadas = visibles.count { it.estado == EstadoTarea.VALIDADA },
        problemasAbiertos = problemas.count {
            puedeVerProblema(sesion, it.reportadoPorId) && it.estado != EstadoProblema.CERRADO
        },
        alertas = totalAlertas
    )
}

// ---------------- Historial ----------------

enum class TipoEvento {
    TAREA_ASIGNADA, TAREA_ENVIADA, TAREA_VALIDADA, TAREA_RECHAZADA,
    PROBLEMA_REPORTADO, PROBLEMA_ATENDIDO
}

data class EventoHistorial(
    val tipo: TipoEvento,
    val fecha: Long,
    val titulo: String,
    val detalle: String,
    val tareaId: Int? = null,
    val problemaId: Int? = null
)

/** Historial más reciente primero. Cada persona ve solo lo que le corresponde. */
fun construirHistorial(
    sesion: Sesion?,
    tareas: List<Tarea>,
    problemas: List<Problema>,
    nombres: Map<String, String>
): List<EventoHistorial> {
    if (sesion == null) return emptyList()
    fun quien(id: String?) = when {
        id == null -> "—"
        id == sesion.usuarioId -> "ti"
        else -> nombres[id] ?: id
    }
    val eventos = mutableListOf<EventoHistorial>()

    tareas.filter { puedeVerTarea(sesion, it.asignadoId, it.grupo) }.forEach { t ->
        val nombre = t.tipo.etiqueta()
        val para = t.asignadoId?.let { quien(it) } ?: t.grupo?.let { "el grupo $it" } ?: "—"

        eventos += EventoHistorial(
            TipoEvento.TAREA_ASIGNADA, t.fechaCreacion,
            "Tarea asignada: $nombre", "Para $para", tareaId = t.id
        )
        t.fechaRealizacion?.let {
            eventos += EventoHistorial(
                TipoEvento.TAREA_ENVIADA, it,
                "Tarea enviada: $nombre", "Enviada por ${quien(t.realizadaPorId)}", tareaId = t.id
            )
        }
        val fechaRevision = t.fechaValidacion
        if (fechaRevision != null &&
            (t.estado == EstadoTarea.VALIDADA || t.estado == EstadoTarea.RECHAZADA)
        ) {
            val validada = t.estado == EstadoTarea.VALIDADA
            eventos += EventoHistorial(
                if (validada) TipoEvento.TAREA_VALIDADA else TipoEvento.TAREA_RECHAZADA,
                fechaRevision,
                (if (validada) "Tarea validada: " else "Tarea rechazada: ") + nombre,
                "Revisada por ${quien(t.validadorId)}" +
                        (t.comentarioDocente?.let { " · $it" } ?: ""),
                tareaId = t.id
            )
        }
    }

    problemas.filter { puedeVerProblema(sesion, it.reportadoPorId) }.forEach { p ->
        val tipo = p.tipo.etiqueta()
        eventos += EventoHistorial(
            TipoEvento.PROBLEMA_REPORTADO, p.fechaReporte,
            "Problema reportado: $tipo", "Reportado por ${quien(p.reportadoPorId)}",
            problemaId = p.id
        )
        val fechaAtencion = p.fechaAtencion
        if (fechaAtencion != null &&
            (p.estado == EstadoProblema.ATENDIDO || p.estado == EstadoProblema.CERRADO)
        ) {
            eventos += EventoHistorial(
                TipoEvento.PROBLEMA_ATENDIDO, fechaAtencion,
                "Problema ${p.estado.etiqueta().lowercase()}: $tipo",
                p.comentarioDocente ?: "Sin comentario", problemaId = p.id
            )
        }
    }

    return eventos.sortedByDescending { it.fecha }
}
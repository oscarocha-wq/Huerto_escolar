package cl.duoc.huerto.domain

import cl.duoc.huerto.data.entities.Evidencia
import cl.duoc.huerto.data.entities.Problema
import cl.duoc.huerto.data.entities.Tarea
import cl.duoc.huerto.data.entities.Validacion

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
    val clave: String,               // única: sirve como key en la lista
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
    nombres: Map<String, String>,
    evidencias: List<Evidencia> = emptyList(),
    validaciones: List<Validacion> = emptyList()
): List<EventoHistorial> {
    if (sesion == null) return emptyList()
    fun quien(id: String?) = when {
        id == null -> "—"
        id == sesion.usuarioId -> "ti"
        else -> nombres[id] ?: id
    }
    fun conComentario(base: String, comentario: String?) =
        base + (comentario?.takeIf { it.isNotBlank() }?.let { " · $it" } ?: "")

    val eventos = mutableListOf<EventoHistorial>()
    val visibles = tareas.filter { puedeVerTarea(sesion, it.asignadoId, it.grupo) }
    val tareaPorId = visibles.associateBy { it.id }

    // Asignación de cada tarea
    visibles.forEach { t ->
        val para = t.asignadoId?.let { quien(it) } ?: t.grupo?.let { "el grupo $it" } ?: "—"
        eventos += EventoHistorial(
            clave = "asignada-${t.id}",
            tipo = TipoEvento.TAREA_ASIGNADA,
            fecha = t.fechaCreacion,
            titulo = "Tarea asignada: ${t.tipo.etiqueta()}",
            detalle = "Para $para",
            tareaId = t.id
        )
    }

    // Cada entrega (puede haber varias si la tarea fue rechazada y reenviada)
    evidencias.forEach { e ->
        val t = tareaPorId[e.tareaId] ?: return@forEach
        eventos += EventoHistorial(
            clave = "entrega-${e.id}",
            tipo = TipoEvento.TAREA_ENVIADA,
            fecha = e.fecha,
            titulo = "Tarea enviada: ${t.tipo.etiqueta()}",
            detalle = conComentario("Enviada por ${quien(e.autorId)}", e.comentario),
            tareaId = t.id
        )
    }

    // Cada revisión del docente
    validaciones.forEach { v ->
        val t = tareaPorId[v.tareaId] ?: return@forEach
        val aprobada = v.resultado == ResultadoValidacion.APROBADA
        eventos += EventoHistorial(
            clave = "revision-${v.id}",
            tipo = if (aprobada) TipoEvento.TAREA_VALIDADA else TipoEvento.TAREA_RECHAZADA,
            fecha = v.fecha,
            titulo = (if (aprobada) "Tarea validada: " else "Tarea rechazada: ") + t.tipo.etiqueta(),
            detalle = conComentario("Revisada por ${quien(v.validadorId)}", v.comentario),
            tareaId = t.id
        )
    }

    problemas.filter { puedeVerProblema(sesion, it.reportadoPorId) }.forEach { p ->
        val tipo = p.tipo.etiqueta()
        eventos += EventoHistorial(
            clave = "problema-${p.id}",
            tipo = TipoEvento.PROBLEMA_REPORTADO,
            fecha = p.fechaReporte,
            titulo = "Problema reportado: $tipo",
            detalle = "Reportado por ${quien(p.reportadoPorId)}",
            problemaId = p.id
        )
        val fechaAtencion = p.fechaAtencion
        if (fechaAtencion != null &&
            (p.estado == EstadoProblema.ATENDIDO || p.estado == EstadoProblema.CERRADO)
        ) {
            eventos += EventoHistorial(
                clave = "atencion-${p.id}",
                tipo = TipoEvento.PROBLEMA_ATENDIDO,
                fecha = fechaAtencion,
                titulo = "Problema ${p.estado.etiqueta().lowercase()}: $tipo",
                detalle = p.comentarioDocente ?: "Sin comentario",
                problemaId = p.id
            )
        }
    }

    return eventos.sortedByDescending { it.fecha }
}

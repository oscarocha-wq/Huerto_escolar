package cl.duoc.huerto.domain

import cl.duoc.huerto.data.entities.Cultivo
import cl.duoc.huerto.data.entities.Problema
import cl.duoc.huerto.data.entities.Sector
import cl.duoc.huerto.data.entities.Tarea

const val DIA_MS = 86_400_000L
const val DIAS_AVISO_RIEGO = 2
const val DIAS_AVISO_COSECHA = 7
const val DIAS_AVISO_MANTENIMIENTO = 7

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

private val ABIERTAS = setOf(
    EstadoTarea.PENDIENTE, EstadoTarea.EN_DESARROLLO, EstadoTarea.RECHAZADA
)
private val CULTIVOS_CON_RIEGO = setOf(EstadoCultivo.SEMBRADO, EstadoCultivo.EN_CRECIMIENTO)
private val CULTIVOS_CON_COSECHA = setOf(
    EstadoCultivo.SEMBRADO, EstadoCultivo.EN_CRECIMIENTO, EstadoCultivo.LISTO_PARA_COSECHAR
)

/** Fecha del próximo riego: último riego (o siembra) más la frecuencia. */
fun proximoRiego(ultimaFecha: Long?, frecuenciaDias: Int): Long? =
    if (ultimaFecha == null || frecuenciaDias <= 0) null
    else ultimaFecha + frecuenciaDias * DIA_MS

/**
 * Genera los avisos de la app. Es informativo: no activa ningún dispositivo ni decide nada.
 * Las tareas y problemas se filtran según quién tiene la sesión.
 */
fun generarAlertas(
    sesion: Sesion?,
    tareas: List<Tarea>,
    cultivos: List<Cultivo>,
    sectores: List<Sector>,
    problemas: List<Problema>,
    hoy: Long
): List<Alerta> {
    if (sesion == null) return emptyList()
    val sectorDe = sectores.associate { it.id to "${it.codigo} · ${it.nombre}" }
    val cultivoDe = cultivos.associate { it.id to it.nombre }
    val alertas = mutableListOf<Alerta>()

    // --- Tareas (solo las que esta persona puede ver) ---
    tareas.filter { puedeVerTarea(sesion, it.asignadoId, it.grupo) }.forEach { t ->
        val donde = t.cultivoId?.let { cultivoDe[it] } ?: sectorDe[t.sectorId] ?: "Sector desconocido"
        val nombre = t.tipo.etiqueta()
        val atrasada = estaAtrasada(t.estado, t.fechaProgramada, hoy)

        if (atrasada) {
            alertas += Alerta(
                "tarea-atrasada-${t.id}", TipoAlerta.TAREA_ATRASADA, SeveridadAlerta.URGENTE,
                "Tarea atrasada: $nombre",
                "$donde · programada para el ${formatearFechaUtc(t.fechaProgramada)}",
                t.fechaProgramada, tareaId = t.id
            )
        }
        if (!sesion.esAdministrador && t.estado == EstadoTarea.RECHAZADA && !atrasada) {
            alertas += Alerta(
                "tarea-rechazada-${t.id}", TipoAlerta.TAREA_RECHAZADA, SeveridadAlerta.AVISO,
                "Tarea rechazada: $nombre",
                "$donde · el docente pidió repetirla",
                t.fechaProgramada, tareaId = t.id
            )
        }
        if (t.tipo == TipoTarea.MANTENIMIENTO && t.estado in ABIERTAS && !atrasada &&
            t.fechaProgramada <= hoy + DIAS_AVISO_MANTENIMIENTO * DIA_MS
        ) {
            alertas += Alerta(
                "mantenimiento-${t.id}", TipoAlerta.MANTENIMIENTO, SeveridadAlerta.INFO,
                "Mantenimiento programado",
                "$donde · ${formatearFechaUtc(t.fechaProgramada)}",
                t.fechaProgramada, tareaId = t.id
            )
        }
        if (sesion.esAdministrador && t.estado == EstadoTarea.PENDIENTE_DE_VALIDACION) {
            alertas += Alerta(
                "por-validar-${t.id}", TipoAlerta.POR_VALIDAR, SeveridadAlerta.AVISO,
                "Tarea por validar: $nombre",
                "$donde · esperando tu revisión",
                t.fechaProgramada, tareaId = t.id
            )
        }
    }

    // --- Problemas sin revisar (solo administrador) ---
    if (sesion.esAdministrador) {
        problemas.filter { it.estado == EstadoProblema.INFORMADO }.forEach { p ->
            alertas += Alerta(
                "problema-${p.id}", TipoAlerta.PROBLEMA_PENDIENTE, SeveridadAlerta.AVISO,
                "Problema sin revisar: ${p.tipo.etiqueta()}",
                sectorDe[p.sectorId] ?: "Sector desconocido",
                p.fechaReporte, problemaId = p.id
            )
        }
    }

    // --- Cultivos: riegos y cosechas (información general, sin datos personales) ---
    cultivos.forEach { c ->
        val sector = sectorDe[c.sectorId] ?: "Sector desconocido"

        if (c.estado in CULTIVOS_CON_RIEGO) {
            val ultimoRiego = tareas
                .filter { it.cultivoId == c.id && it.tipo == TipoTarea.RIEGO && it.estado == EstadoTarea.VALIDADA }
                .maxOfOrNull { it.fechaProgramada }
            val base = listOfNotNull(ultimoRiego, c.fechaSiembra).maxOrNull()
            val proximo = proximoRiego(base, c.frecuenciaRiegoDias)
            if (proximo != null) {
                if (proximo < hoy) {
                    alertas += Alerta(
                        "riego-${c.id}", TipoAlerta.RIEGO_ATRASADO, SeveridadAlerta.URGENTE,
                        "Riego atrasado: ${c.nombre}",
                        "$sector · tocaba el ${formatearFechaUtc(proximo)}", proximo
                    )
                } else if (proximo <= hoy + DIAS_AVISO_RIEGO * DIA_MS) {
                    alertas += Alerta(
                        "riego-${c.id}", TipoAlerta.RIEGO_PROXIMO, SeveridadAlerta.AVISO,
                        "Próximo riego: ${c.nombre}",
                        "$sector · ${formatearFechaUtc(proximo)}", proximo
                    )
                }
            }
        }

        val cosecha = c.fechaCosechaEstimada
        if (c.estado in CULTIVOS_CON_COSECHA && cosecha != null &&
            cosecha <= hoy + DIAS_AVISO_COSECHA * DIA_MS
        ) {
            alertas += Alerta(
                "cosecha-${c.id}", TipoAlerta.COSECHA_PROXIMA,
                if (cosecha < hoy) SeveridadAlerta.AVISO else SeveridadAlerta.INFO,
                if (cosecha < hoy) "Cosecha pasada de fecha: ${c.nombre}" else "Próxima cosecha: ${c.nombre}",
                "$sector · estimada para el ${formatearFechaUtc(cosecha)}", cosecha
            )
        }
    }

    return alertas.sortedWith(
        compareBy({ it.severidad.ordinal }, { it.fecha ?: Long.MAX_VALUE })
    )
}
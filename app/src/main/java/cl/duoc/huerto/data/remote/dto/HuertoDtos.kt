package cl.duoc.huerto.data.remote.dto

import cl.duoc.huerto.domain.model.*

/*
 * DTO = forma del JSON que envía el backend.
 * Todos los campos son anulables: si el servidor omite uno, la app no se cae.
 * toEntity() traduce cada DTO a la entidad de Room que ya usa la app
 * (el backend usa ids Long y la app Int; los textos nulos pasan a "").
 */

data class LoginRequestDto(val id: String, val clave: String)

data class UsuarioDto(
    val id: String?,
    val nombre: String?,
    val perfil: Perfil?,
    val grupo: String?,
    val activo: Boolean?
) {
    /** El backend no devuelve la clave (es WRITE_ONLY): se recibe aparte. */
    fun toEntity(clave: String = ""): Usuario? {
        val codigo = id ?: return null
        return Usuario(
            id = codigo,
            nombre = nombre.orEmpty(),
            clave = clave,
            perfil = perfil ?: Perfil.ESTUDIANTE,
            grupo = grupo,
            activo = activo ?: true
        )
    }
}

data class SectorDto(
    val id: Long?,
    val codigo: String?,
    val nombre: String?,
    val descripcion: String?,
    val ubicacion: String?,
    val estado: EstadoSector?,
    val responsableId: String?,
    val fechaCreacion: Long?,
    val fechaActualizacion: Long?
) {
    fun toEntity(): Sector? {
        val ahora = System.currentTimeMillis()
        return Sector(
            id = id?.toInt() ?: return null,
            codigo = codigo.orEmpty(),
            nombre = nombre.orEmpty(),
            descripcion = descripcion.orEmpty(),
            ubicacion = ubicacion.orEmpty(),
            estado = estado ?: EstadoSector.DISPONIBLE,
            responsableId = responsableId,
            fechaCreacion = fechaCreacion ?: ahora,
            fechaActualizacion = fechaActualizacion ?: ahora
        )
    }
}

data class CultivoDto(
    val id: Long?,
    val nombre: String?,
    val descripcion: String?,
    val sectorId: Long?,
    val fechaSiembra: Long?,
    val fechaTrasplante: Long?,
    val fechaCosechaEstimada: Long?,
    val frecuenciaRiegoDias: Int?,
    val estado: EstadoCultivo?,
    val observaciones: String?,
    val fotoUri: String?
) {
    fun toEntity(): Cultivo? {
        val idLocal = id?.toInt() ?: return null
        val sector = sectorId?.toInt() ?: return null
        return Cultivo(
            id = idLocal,
            nombre = nombre.orEmpty(),
            descripcion = descripcion.orEmpty(),
            sectorId = sector,
            fechaSiembra = fechaSiembra,
            fechaTrasplante = fechaTrasplante,
            fechaCosechaEstimada = fechaCosechaEstimada,
            frecuenciaRiegoDias = frecuenciaRiegoDias ?: 1,
            estado = estado ?: EstadoCultivo.PLANIFICADO,
            observaciones = observaciones.orEmpty(),
            fotoUri = fotoUri
        )
    }
}

data class TareaDto(
    val id: Long?,
    val tipo: TipoTarea?,
    val descripcion: String?,
    val sectorId: Long?,
    val cultivoId: Long?,
    val fechaCreacion: Long?,
    val fechaProgramada: Long?,
    val asignadoId: String?,
    val grupo: String?,
    val creadaPorId: String?,
    val estado: EstadoTarea?
) {
    fun toEntity(): Tarea? {
        val ahora = System.currentTimeMillis()
        return Tarea(
            id = id?.toInt() ?: return null,
            tipo = tipo ?: TipoTarea.OTRA,
            descripcion = descripcion.orEmpty(),
            sectorId = sectorId?.toInt() ?: return null,
            cultivoId = cultivoId?.toInt(),
            fechaCreacion = fechaCreacion ?: ahora,
            fechaProgramada = fechaProgramada ?: ahora,
            asignadoId = asignadoId,
            grupo = grupo,
            creadaPorId = creadaPorId ?: return null,
            estado = estado ?: EstadoTarea.PENDIENTE
        )
    }
}

data class ProblemaDto(
    val id: Long?,
    val tipo: TipoProblema?,
    val descripcion: String?,
    val sectorId: Long?,
    val cultivoId: Long?,
    val reportadoPorId: String?,
    val fechaReporte: Long?,
    val fotoRuta: String?,
    val estado: EstadoProblema?,
    val comentarioDocente: String?,
    val atendidoPorId: String?,
    val fechaAtencion: Long?
) {
    fun toEntity(): Problema? {
        val idLocal = id?.toInt() ?: return null
        val sector = sectorId?.toInt() ?: return null
        val reportante = reportadoPorId ?: return null
        return Problema(
            id = idLocal,
            tipo = tipo ?: TipoProblema.OTRO,
            descripcion = descripcion.orEmpty(),
            sectorId = sector,
            cultivoId = cultivoId?.toInt(),
            reportadoPorId = reportante,
            fechaReporte = fechaReporte ?: System.currentTimeMillis(),
            fotoRuta = fotoRuta,
            estado = estado ?: EstadoProblema.INFORMADO,
            comentarioDocente = comentarioDocente,
            atendidoPorId = atendidoPorId,
            fechaAtencion = fechaAtencion
        )
    }
}
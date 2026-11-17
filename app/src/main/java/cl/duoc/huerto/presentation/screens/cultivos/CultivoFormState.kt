package cl.duoc.huerto.presentation.screens.cultivos

import cl.duoc.huerto.domain.model.EstadoCultivo
import cl.duoc.huerto.domain.model.Sector
import cl.duoc.huerto.domain.validation.ErroresCultivo

data class CultivoFormState(
    val nombre: String = "",
    val descripcion: String = "",
    val sectorId: Int? = null,
    val fechaSiembra: Long? = null,
    val fechaTrasplante: Long? = null,
    val fechaCosecha: Long? = null,
    val frecuencia: String = "",
    val estado: EstadoCultivo = EstadoCultivo.PLANIFICADO,
    val observaciones: String = "",
    val fotoRuta: String? = null,
    val sectores: List<Sector> = emptyList(),
    val errores: ErroresCultivo = ErroresCultivo(),
    val errorGeneral: String? = null,
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardado: Boolean = false,
    val eliminado: Boolean = false
)

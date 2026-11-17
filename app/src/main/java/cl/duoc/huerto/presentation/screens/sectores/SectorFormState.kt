package cl.duoc.huerto.presentation.screens.sectores

import cl.duoc.huerto.domain.model.EstadoSector
import cl.duoc.huerto.domain.validation.ErroresSector

data class SectorFormState(
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val estado: EstadoSector = EstadoSector.DISPONIBLE,
    val errores: ErroresSector = ErroresSector(),
    val errorGeneral: String? = null,
    val esEdicion: Boolean = false,
    val cargando: Boolean = false,
    val guardado: Boolean = false,
    val eliminado: Boolean = false
)

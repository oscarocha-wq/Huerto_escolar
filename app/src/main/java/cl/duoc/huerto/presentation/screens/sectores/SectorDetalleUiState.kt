package cl.duoc.huerto.presentation.screens.sectores

import cl.duoc.huerto.domain.model.Cultivo
import cl.duoc.huerto.domain.model.Sector

data class SectorDetalleUiState(
    val cargando: Boolean = true,
    val sector: Sector? = null,
    val cultivos: List<Cultivo> = emptyList(),
    val tareasAbiertas: Int = 0,
    val problemasAbiertos: Int = 0
)

typealias SectorDetalleUi = SectorDetalleUiState

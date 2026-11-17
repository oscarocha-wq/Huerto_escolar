package cl.duoc.huerto.presentation.screens.panel

import cl.duoc.huerto.domain.model.Alerta
import cl.duoc.huerto.domain.model.EventoHistorial
import cl.duoc.huerto.domain.model.ResumenPanel
import cl.duoc.huerto.domain.model.Sesion

data class PanelUiState(
    val sesion: Sesion? = null,
    val nombre: String = "",
    val esAdmin: Boolean = false,
    val resumen: ResumenPanel = ResumenPanel(),
    val alertas: List<Alerta> = emptyList(),
    val historial: List<EventoHistorial> = emptyList()
)

typealias PanelUi = PanelUiState

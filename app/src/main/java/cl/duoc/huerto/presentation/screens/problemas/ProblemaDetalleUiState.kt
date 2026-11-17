package cl.duoc.huerto.presentation.screens.problemas

data class ProblemaDetalleUiState(
    val comentario: String = "",
    val error: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false
)

typealias DetalleProblemaUi = ProblemaDetalleUiState

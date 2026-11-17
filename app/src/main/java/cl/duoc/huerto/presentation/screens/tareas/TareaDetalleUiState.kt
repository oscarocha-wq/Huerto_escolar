package cl.duoc.huerto.presentation.screens.tareas

data class TareaDetalleUiState(
    val comentario: String = "",
    val fotoRuta: String? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false,
    val eliminada: Boolean = false
)

typealias DetalleUi = TareaDetalleUiState

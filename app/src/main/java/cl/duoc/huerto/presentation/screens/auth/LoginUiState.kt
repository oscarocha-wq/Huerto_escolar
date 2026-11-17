package cl.duoc.huerto.presentation.screens.auth

data class LoginUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

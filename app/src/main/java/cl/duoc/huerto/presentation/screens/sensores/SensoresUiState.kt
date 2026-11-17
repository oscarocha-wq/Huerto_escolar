package cl.duoc.huerto.presentation.screens.sensores

import cl.duoc.huerto.domain.validation.ErroresLectura

data class SensoresUiState(
    val humedadSuelo: String = "",
    val temperatura: String = "",
    val humedadAmbiente: String = "",
    val errores: ErroresLectura = ErroresLectura(),
    val error: String? = null,
    val mensaje: String? = null,
    val procesando: Boolean = false
)

typealias FormLecturaUi = SensoresUiState

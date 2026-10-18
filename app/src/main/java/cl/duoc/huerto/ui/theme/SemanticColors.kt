package cl.duoc.huerto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Único lugar del proyecto con hex de éxito / advertencia / información.
// El error se toma de MaterialTheme.colorScheme.error.

private val Green40 = Color(0xFF2E7D32)
private val Green90 = Color(0xFFC8E6C9)
private val Green10 = Color(0xFF0B3D0E)
private val Green80 = Color(0xFF81C784)
private val Green30 = Color(0xFF1B5E20)

private val Amber40 = Color(0xFF8A5A00)
private val Amber90 = Color(0xFFFFE0A3)
private val Amber10 = Color(0xFF2B1A00)
private val Amber80 = Color(0xFFFFC94D)
private val Amber30 = Color(0xFF5E3D00)

private val Blue40 = Color(0xFF1565C0)
private val Blue90 = Color(0xFFD3E4FF)
private val Blue10 = Color(0xFF001C3A)
private val Blue80 = Color(0xFF9CC7FF)
private val Blue30 = Color(0xFF00468C)

@Immutable
data class SemanticColors(
    val success: Color, val onSuccess: Color,
    val successContainer: Color, val onSuccessContainer: Color,
    val warning: Color, val onWarning: Color,
    val warningContainer: Color, val onWarningContainer: Color,
    val info: Color, val onInfo: Color,
    val infoContainer: Color, val onInfoContainer: Color,
)

val LightSemanticColors = SemanticColors(
    success = Green40, onSuccess = Color.White,
    successContainer = Green90, onSuccessContainer = Green10,
    warning = Amber40, onWarning = Color.White,
    warningContainer = Amber90, onWarningContainer = Amber10,
    info = Blue40, onInfo = Color.White,
    infoContainer = Blue90, onInfoContainer = Blue10,
)

val DarkSemanticColors = SemanticColors(
    success = Green80, onSuccess = Green10,
    successContainer = Green30, onSuccessContainer = Green90,
    warning = Amber80, onWarning = Amber10,
    warningContainer = Amber30, onWarningContainer = Amber90,
    info = Blue80, onInfo = Blue10,
    infoContainer = Blue30, onInfoContainer = Blue90,
)

val LocalSemanticColors = staticCompositionLocalOf { LightSemanticColors }

/** Uso: MaterialTheme.semanticColors.success */
val MaterialTheme.semanticColors: SemanticColors
    @Composable @ReadOnlyComposable
    get() = LocalSemanticColors.current
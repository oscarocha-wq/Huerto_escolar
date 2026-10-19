package cl.duoc.huerto.u_i

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.formatearFechaUtc
import cl.duoc.huerto.ui.theme.semanticColors

/** Texto bajo el campo: error animado, ayuda y contador de caracteres. */
@Composable
private fun ApoyoCampo(
    error: String?,
    ayuda: String? = null,
    contador: String? = null,
    contadorAlLimite: Boolean = false
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            androidx.compose.animation.AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            if (error == null && ayuda != null) Text(ayuda)
        }
        if (contador != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                contador,
                color = if (contadorAlLimite) MaterialTheme.semanticColors.warning
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Ícono al final del campo: el normal, o el de error si hay error. */
@Composable
private fun IconoCampo(normal: ImageVector, error: String?) {
    if (error != null) {
        Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
    } else {
        Icon(normal, contentDescription = null)
    }
}

/**
 * Campo de texto estándar de los formularios. NO valida: recibe `error` ya calculado
 * por el ViewModel. Muestra ícono de error, mensaje animado, contador y corta en maxLength.
 */
@Composable
fun CampoValidado(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    marcarError: Boolean = false,               // borde rojo sin mensaje propio (ej: login)
    maxLength: Int? = null,
    icono: ImageVector? = null,
    ayuda: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingPropio: (@Composable () -> Unit)? = null,  // ej: ojo de la contraseña
    enabled: Boolean = true
) {
    val hayError = error != null || marcarError

    OutlinedTextField(
        value = value,
        onValueChange = { nuevo ->
            if (maxLength == null || nuevo.length <= maxLength) onValueChange(nuevo)
        },
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (error != null) error(error) },
        isError = hayError,
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = when {
            trailingPropio != null -> trailingPropio
            error != null -> {
                { Icon(Icons.Filled.Error, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
            }
            else -> null
        },
        supportingText = if (error != null || ayuda != null || maxLength != null) {
            {
                ApoyoCampo(
                    error = error,
                    ayuda = ayuda,
                    contador = maxLength?.let { "${value.length}/$it" },
                    contadorAlLimite = maxLength != null && value.length >= maxLength
                )
            }
        } else null
    )
}

/** Lista desplegable simple: muestra la opción elegida y abre un menú al tocar. */
@Composable
fun <T> SelectorDesplegable(
    etiqueta: String,
    opciones: List<T>,
    seleccion: T?,
    textoDe: (T) -> String,
    onSeleccion: (T) -> Unit,
    error: String? = null,
    enabled: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = seleccion?.let(textoDe) ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text("Seleccionar") },
            trailingIcon = { IconoCampo(Icons.Filled.ArrowDropDown, error) },
            isError = error != null,
            supportingText = if (error != null) { { ApoyoCampo(error) } } else null,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        // Capa transparente que recibe el toque
        Box(
            Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { expandido = true }
        )
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(textoDe(opcion)) },
                    onClick = {
                        onSeleccion(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

/** Campo de fecha: al tocarlo abre el calendario de Material 3. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoFecha(
    etiqueta: String,
    valor: Long?,
    onCambio: (Long?) -> Unit,
    error: String? = null,
    enabled: Boolean = true
) {
    var mostrar by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatearFechaUtc(valor),
            onValueChange = {},
            readOnly = true,
            label = { Text(etiqueta) },
            placeholder = { Text("Sin fecha") },
            trailingIcon = { IconoCampo(Icons.Filled.CalendarMonth, error) },
            isError = error != null,
            supportingText = if (error != null) { { ApoyoCampo(error) } } else null,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { mostrar = true }
        )
    }

    if (mostrar) {
        val estadoCalendario = rememberDatePickerState(initialSelectedDateMillis = valor)
        DatePickerDialog(
            onDismissRequest = { mostrar = false },
            confirmButton = {
                TextButton(onClick = {
                    onCambio(estadoCalendario.selectedDateMillis)
                    mostrar = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (valor != null) {
                        TextButton(onClick = {
                            onCambio(null)
                            mostrar = false
                        }) { Text("Quitar fecha") }
                    }
                    TextButton(onClick = { mostrar = false }) { Text("Cancelar") }
                }
            }
        ) {
            DatePicker(state = estadoCalendario)
        }
    }
}
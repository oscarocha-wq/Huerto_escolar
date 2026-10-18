package cl.duoc.huerto.u_i

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.huerto.domain.formatearFechaUtc

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
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
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
            trailingIcon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
            isError = error != null,
            supportingText = { error?.let { Text(it) } },
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
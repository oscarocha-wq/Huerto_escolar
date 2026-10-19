package cl.duoc.huerto.u_i

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón rojo de "Eliminar" que siempre pide confirmación antes de actuar.
 * No decide si se puede eliminar: eso lo valida el ViewModel.
 */
@Composable
fun BotonEliminar(
    texto: String,
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var preguntar by remember { mutableStateOf(false) }

    OutlinedButton(
        onClick = { preguntar = true },
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        modifier = modifier.fillMaxWidth()
    ) {
        Icon(Icons.Filled.Delete, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(texto)
    }

    if (preguntar) {
        AlertDialog(
            onDismissRequest = { preguntar = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(titulo) },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(
                    onClick = {
                        preguntar = false
                        onConfirmar()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { preguntar = false }) { Text("Cancelar") } }
        )
    }
}

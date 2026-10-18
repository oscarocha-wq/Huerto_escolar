package cl.duoc.huerto.u_i

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Botón principal: el texto se funde con un indicador de carga mientras se procesa. */
@Composable
fun BotonAccion(
    texto: String,
    cargando: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !cargando,
        modifier = modifier
            .height(52.dp)
            .animateContentSize()
    ) {
        AnimatedContent(
            targetState = cargando,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "boton_accion"
        ) { enCarga ->
            if (enCarga) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
            } else {
                Text(texto, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Mensaje de error general: aparece y desaparece con animación, con ícono. */
@Composable
fun MensajeError(mensaje: String?, modifier: Modifier = Modifier) {
    // Recordamos el último texto para que no se vacíe mientras se anima la salida
    var ultimo by remember { mutableStateOf(mensaje ?: "") }
    if (mensaje != null && mensaje != ultimo) ultimo = mensaje

    AnimatedVisibility(
        visible = mensaje != null,
        enter = fadeIn(tween(250)) + expandVertically(tween(250)),
        exit = fadeOut(tween(200)) + shrinkVertically(tween(200)),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                ultimo,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
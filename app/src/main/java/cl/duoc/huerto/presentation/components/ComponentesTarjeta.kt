package cl.duoc.huerto.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.presentation.theme.Tono
import cl.duoc.huerto.presentation.theme.coloresTono

/** Un dato corto de la tarjeta, con su ícono (fecha, persona, riego, ubicación…). */
data class DatoTarjeta(val icono: ImageVector, val texto: String)

/**
 * Tarjeta de lista estándar de la app.
 *
 *  [avatar]  Título                           >
 *            Subtítulo
 *            [chip estado] [chip extra]
 *            Descripción (máx. 2 líneas)
 *            ◷ dato   👤 dato   💧 dato
 *
 * Solo usa recursos del tema: colorScheme, typography, shapes y los tonos de estado.
 * El avatar toma el color del tono, así el estado se reconoce de un vistazo.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaElemento(
    titulo: String,
    icono: ImageVector,
    tono: Tono,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitulo: String? = null,
    descripcion: String? = null,
    fotoRuta: String? = null,
    datos: List<DatoTarjeta> = emptyList(),
    etiquetas: (@Composable RowScope.() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AvatarTarjeta(icono = icono, tono = tono, fotoRuta = fotoRuta)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    subtitulo?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (etiquetas != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = etiquetas
                    )
                }

                descripcion?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (datos.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        datos.forEach { DatoConIcono(it) }
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

/** Ícono (o foto) dentro de un cuadrado redondeado con el color del estado. */
@Composable
private fun AvatarTarjeta(icono: ImageVector, tono: Tono, fotoRuta: String?) {
    val (fondo, contenido) = coloresTono(tono)
    val iconoEstado: @Composable () -> Unit = {
        Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(28.dp))
    }
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        if (fotoRuta != null) {
            ImagenLocal(fotoRuta, Modifier.fillMaxSize(), alternativa = iconoEstado)
        } else {
            iconoEstado()
        }
    }
}

@Composable
private fun DatoConIcono(dato: DatoTarjeta) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = dato.icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = dato.texto,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

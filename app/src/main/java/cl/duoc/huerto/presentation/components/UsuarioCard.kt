package cl.duoc.huerto.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.model.Perfil
import cl.duoc.huerto.domain.model.Sesion
import cl.duoc.huerto.domain.model.Usuario
import cl.duoc.huerto.presentation.theme.HuertoTheme

/**
 * Tarjeta de un usuario de la app (equivalente al ClienteCard del proyecto de referencia:
 * Cliente ↔ Usuario y tipo VIP/regular ↔ perfil Administrador/Estudiante).
 *
 *  (DP)  Docente Prueba          [★ ADMINISTRADOR]
 *        ID ADM-01
 *        [Perfil Docente] [Grupo Todos] [Estado Activo]
 *
 * Componente sin estado: recibe los datos y los dibuja.
 * Solo usa recursos del tema: colorScheme, typography y shapes.
 */
@Composable
fun UsuarioCard(
    usuario: Usuario,
    modifier: Modifier = Modifier,
    saludo: String? = null
) = UsuarioCardContenido(
    id = usuario.id,
    nombre = usuario.nombre,
    perfil = usuario.perfil,
    grupo = usuario.grupo,
    activo = usuario.activo,
    saludo = saludo,
    modifier = modifier
)

/** Misma tarjeta para el usuario que inició sesión. */
@Composable
fun UsuarioCard(
    sesion: Sesion,
    modifier: Modifier = Modifier,
    saludo: String? = null
) = UsuarioCardContenido(
    id = sesion.usuarioId,
    nombre = sesion.nombre,
    perfil = sesion.perfil,
    grupo = sesion.grupo,
    activo = true,
    saludo = saludo,
    modifier = modifier
)

/** Colores del perfil, todos tomados del tema. El administrador se destaca en dorado (tertiary). */
private data class ColoresPerfil(val fondo: Color, val contenido: Color, val borde: Color)

@Composable
private fun coloresPerfil(perfil: Perfil): ColoresPerfil {
    val c = MaterialTheme.colorScheme
    return if (perfil == Perfil.ADMINISTRADOR) {
        ColoresPerfil(c.tertiaryContainer, c.onTertiaryContainer, c.tertiary)
    } else {
        ColoresPerfil(c.primaryContainer, c.onPrimaryContainer, c.primary)
    }
}

/** "Docente Prueba" -> "DP". */
private fun iniciales(nombre: String): String =
    nombre.trim().split(Regex("\\s+"))
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

@Composable
private fun UsuarioCardContenido(
    id: String,
    nombre: String,
    perfil: Perfil,
    grupo: String?,
    activo: Boolean,
    saludo: String?,
    modifier: Modifier
) {
    val colores = coloresPerfil(perfil)
    val esAdmin = perfil == Perfil.ADMINISTRADOR

    Card(
        modifier = modifier
            .fillMaxWidth()
            // TalkBack lee la tarjeta completa como un solo elemento
            .semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, if (esAdmin) colores.borde else MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarUsuario(nombre = nombre, colores = colores)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        saludo?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "ID $id",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    InsigniaPerfil(esAdmin = esAdmin, colores = colores)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatoUsuario("Perfil", if (esAdmin) "Docente" else "Estudiante")
                    DatoUsuario("Grupo", grupo ?: if (esAdmin) "Todos" else "Sin grupo")
                    DatoUsuario("Estado", if (activo) "Activo" else "Inactivo")
                }
            }
        }
    }
}

@Composable
private fun AvatarUsuario(nombre: String, colores: ColoresPerfil) {
    Surface(
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = colores.fondo,
        border = BorderStroke(1.dp, colores.borde)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = iniciales(nombre),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colores.contenido
            )
        }
    }
}

@Composable
private fun InsigniaPerfil(esAdmin: Boolean, colores: ColoresPerfil) {
    Surface(
        shape = CircleShape,
        color = if (esAdmin) colores.fondo else MaterialTheme.colorScheme.secondaryContainer,
        contentColor = if (esAdmin) colores.contenido else MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Text(
            text = if (esAdmin) "★ ADMIN" else "ESTUDIANTE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DatoUsuario(etiqueta: String, valor: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UsuarioCardAdminPreview() {
    HuertoTheme {
        UsuarioCard(
            usuario = Usuario("ADM-01", "Docente Prueba", "1234", Perfil.ADMINISTRADOR, null),
            saludo = "Hola,",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UsuarioCardEstudiantePreview() {
    HuertoTheme {
        UsuarioCard(
            usuario = Usuario("EST-01", "Estudiante Uno", "1234", Perfil.ESTUDIANTE, "Taller A"),
            modifier = Modifier.padding(16.dp)
        )
    }
}

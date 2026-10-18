package cl.duoc.huerto.u_i

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.domain.Perfil
import cl.duoc.huerto.domain.puedeAcceder

@Composable
fun PantallaProvisoria(titulo: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
fun AccesoDenegado() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Lock, contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text("Acceso denegado", style = MaterialTheme.typography.titleLarge)
        Text(
            "Tu perfil no tiene permiso para ver esta pantalla.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Muestra el contenido solo si la sesión actual tiene el perfil requerido. */
@Composable
fun RutaProtegida(
    perfilRequerido: Perfil?,
    contenido: @Composable () -> Unit
) {
    val sesion by SesionManager.sesion.collectAsStateWithLifecycle()
    if (sesion.puedeAcceder(perfilRequerido)) contenido() else AccesoDenegado()
}
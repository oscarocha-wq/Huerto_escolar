package cl.duoc.huerto.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.presentation.navigation.ItemMenu

@Composable
fun BarraNavegacionInferior(
    menu: List<ItemMenu>,
    estaSeleccionado: (String) -> Boolean,
    onIrASeccion: (String) -> Unit
) {
    NavigationBar {
        menu.forEach { item ->
            NavigationBarItem(
                selected = estaSeleccionado(item.ruta),
                onClick = { onIrASeccion(item.ruta) },
                icon = { Icon(item.icono, contentDescription = item.titulo) },
                label = { Text(item.titulo) }
            )
        }
    }
}

@Composable
fun RielNavegacionLateral(
    menu: List<ItemMenu>,
    estaSeleccionado: (String) -> Boolean,
    onIrASeccion: (String) -> Unit
) {
    NavigationRail {
        Spacer(Modifier.height(8.dp))
        menu.forEach { item ->
            NavigationRailItem(
                selected = estaSeleccionado(item.ruta),
                onClick = { onIrASeccion(item.ruta) },
                icon = { Icon(item.icono, contentDescription = item.titulo) },
                label = { Text(item.titulo) }
            )
        }
    }
}

@Composable
fun AccionesBarraSuperior(
    alertasCount: Int,
    onVerAlertas: () -> Unit,
    onVerHistorial: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    IconButton(onClick = onVerAlertas) {
        BadgedBox(badge = {
            if (alertasCount > 0) Badge { Text(if (alertasCount > 9) "9+" else "$alertasCount") }
        }) {
            Icon(Icons.Filled.Notifications, contentDescription = "Alertas")
        }
    }
    IconButton(onClick = onVerHistorial) {
        Icon(Icons.Filled.History, contentDescription = "Historial")
    }
    IconButton(onClick = onCerrarSesion) {
        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
    }
}

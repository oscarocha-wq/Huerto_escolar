package cl.duoc.huerto.u_i.panel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.Alerta

@Composable
fun AlertasScreen(
    alertas: List<Alerta>,
    onAbrirAlerta: (Alerta) -> Unit,
    onVolver: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text("Alertas y recordatorios", style = MaterialTheme.typography.titleLarge)
        }

        if (alertas.isEmpty()) {
            EstadoVacio(Icons.Filled.CheckCircle, "Todo al día", "No hay avisos pendientes.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alertas, key = { it.clave }) { alerta ->
                    Box(Modifier.animateItem()) {
                        Card(Modifier.fillMaxWidth()) {
                            AlertaContenido(
                                alerta,
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = alerta.tareaId != null || alerta.problemaId != null) {
                                        onAbrirAlerta(alerta)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
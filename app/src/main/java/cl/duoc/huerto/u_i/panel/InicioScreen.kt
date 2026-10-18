package cl.duoc.huerto.u_i.panel

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.huerto.domain.Alerta
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.ui.theme.coloresTono
import cl.duoc.huerto.ui.theme.semanticColors

@Composable
fun InicioScreen(
    ui: PanelUi,
    onVerAlertas: () -> Unit,
    onVerHistorial: () -> Unit,
    onAbrirAlerta: (Alerta) -> Unit
) {
    val r = ui.resumen

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Hola, ${ui.nombre}", style = MaterialTheme.typography.headlineSmall)
            Text(
                if (ui.esAdmin) "Resumen del huerto" else "Resumen de tus tareas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaContador("Pendientes", r.pendientes, Icons.Filled.Schedule, Tono.INFO, Modifier.weight(1f))
            TarjetaContador("Atrasadas", r.atrasadas, Icons.Filled.EventBusy, Tono.ERROR, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TarjetaContador(
                if (ui.esAdmin) "Por validar" else "En revisión", r.porValidar,
                Icons.Filled.HourglassTop, Tono.ADVERTENCIA, Modifier.weight(1f)
            )
            if (ui.esAdmin) {
                TarjetaContador(
                    "Problemas abiertos", r.problemasAbiertos,
                    Icons.Filled.ReportProblem, Tono.ERROR, Modifier.weight(1f)
                )
            } else {
                TarjetaContador(
                    "Validadas", r.validadas,
                    Icons.Filled.CheckCircle, Tono.EXITO, Modifier.weight(1f)
                )
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Avisos", style = MaterialTheme.typography.titleMedium)
                    if (ui.alertas.isNotEmpty()) {
                        TextButton(onClick = onVerAlertas) { Text("Ver todos (${ui.alertas.size})") }
                    }
                }
                if (ui.alertas.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.semanticColors.success
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Todo al día. No hay avisos pendientes.")
                    }
                } else {
                    ui.alertas.take(3).forEach { alerta ->
                        AlertaContenido(
                            alerta,
                            Modifier.fillMaxWidth().clickable { onAbrirAlerta(alerta) }
                        )
                    }
                }
            }
        }

        OutlinedButton(onClick = onVerHistorial, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.History, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver historial de actividades")
        }
    }
}

@Composable
private fun TarjetaContador(
    titulo: String,
    valor: Int,
    icono: ImageVector,
    tono: Tono,
    modifier: Modifier = Modifier
) {
    val (fondo, contenido) = coloresTono(tono)
    // El número "cuenta" hasta su nuevo valor cuando cambia
    val animado by animateIntAsState(valor, tween(700), label = "contador")
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = fondo, contentColor = contenido)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = null)
            Spacer(Modifier.height(8.dp))
            Text("$animado", style = MaterialTheme.typography.headlineLarge)
            Text(titulo, style = MaterialTheme.typography.labelLarge)
        }
    }
}
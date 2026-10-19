package cl.duoc.huerto.u_i.sensores

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.data.entities.LecturaSensor
import cl.duoc.huerto.domain.*
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.BotonEliminar
import cl.duoc.huerto.u_i.CampoValidado
import cl.duoc.huerto.u_i.ChipEstado
import cl.duoc.huerto.u_i.MensajeError
import cl.duoc.huerto.u_i.panel.EstadoVacio
import cl.duoc.huerto.ui.theme.Tono
import cl.duoc.huerto.ui.theme.coloresTono
import cl.duoc.huerto.ui.theme.tono

@Composable
fun SensoresScreen(sectorId: Int, onVolver: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SensoresViewModel = viewModel(
        factory = SensoresViewModel.factory(context.applicationContext as Application, sectorId)
    )
    val sector by viewModel.sector.collectAsStateWithLifecycle()
    val lecturas by viewModel.lecturas.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()

    LaunchedEffect(form.mensaje) {
        form.mensaje?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.mensajeMostrado()
        }
    }

    val numero = KeyboardOptions(keyboardType = KeyboardType.Decimal)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column {
                    Text("Sensores", style = MaterialTheme.typography.titleLarge)
                    sector?.let {
                        Text(
                            "${it.codigo} · ${it.nombre}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Aviso: es una simulación académica, no controla nada
        item {
            val (fondo, contenido) = coloresTono(Tono.INFO)
            Surface(color = fondo, contentColor = contenido, shape = MaterialTheme.shapes.medium) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Sensors, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Simulación académica: las lecturas \"Dato de prueba\" se generan al azar. " +
                            "La app solo muestra información y no controla el riego.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Button(onClick = viewModel::simular, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Generar lectura de prueba")
            }
        }

        // --- Registro manual ---
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Registrar lectura manual", style = MaterialTheme.typography.titleMedium)
                    CampoValidado(
                        value = form.humedadSuelo,
                        onValueChange = viewModel::onHumedadSuelo,
                        label = "Humedad del suelo (%)",
                        icono = Icons.Filled.WaterDrop,
                        error = form.errores.humedadSuelo,
                        keyboardOptions = numero,
                        enabled = !form.procesando
                    )
                    CampoValidado(
                        value = form.temperatura,
                        onValueChange = viewModel::onTemperatura,
                        label = "Temperatura (°C)",
                        icono = Icons.Filled.Thermostat,
                        error = form.errores.temperatura,
                        keyboardOptions = numero,
                        enabled = !form.procesando
                    )
                    CampoValidado(
                        value = form.humedadAmbiente,
                        onValueChange = viewModel::onHumedadAmbiente,
                        label = "Humedad ambiente (%)",
                        icono = Icons.Filled.Cloud,
                        error = form.errores.humedadAmbiente,
                        keyboardOptions = numero,
                        enabled = !form.procesando
                    )
                    MensajeError(form.error)
                    BotonAccion(
                        texto = "Guardar lectura",
                        cargando = form.procesando,
                        onClick = viewModel::registrarManual,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item { Text("Historial de lecturas", style = MaterialTheme.typography.titleMedium) }

        val lista = lecturas
        when {
            lista == null -> item {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            lista.isEmpty() -> item {
                EstadoVacio(
                    Icons.Filled.Sensors, "Sin lecturas",
                    "Genera una lectura de prueba o registra una manual.",
                    Modifier.height(240.dp)
                )
            }
            else -> {
                items(lista, key = { it.id }) { l ->
                    Box(Modifier.animateItem()) { LecturaCard(l) }
                }
                if (lista.any { it.origen == OrigenLectura.SIMULADA }) {
                    item {
                        BotonEliminar(
                            texto = "Borrar datos de prueba",
                            titulo = "¿Borrar datos de prueba?",
                            mensaje = "Se eliminan solo las lecturas simuladas de este sector. " +
                                "Los registros manuales se conservan.",
                            onConfirmar = viewModel::borrarSimuladas,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LecturaCard(l: LecturaSensor) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatearFechaHora(l.fecha),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                ChipEstado(
                    l.origen.etiqueta(),
                    if (l.origen == OrigenLectura.SIMULADA) Tono.NEUTRO else Tono.INFO
                )
            }
            FilaVariable(Variable.HUMEDAD_SUELO, l.humedadSuelo)
            FilaVariable(Variable.TEMPERATURA, l.temperatura)
            FilaVariable(Variable.HUMEDAD_AMBIENTE, l.humedadAmbiente)
        }
    }
}

@Composable
private fun FilaVariable(variable: Variable, valor: Double) {
    val estado = variable.evaluar(valor)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(variable.etiqueta, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text("$valor ${variable.unidad}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        ChipEstado(estado.etiqueta(), estado.tono())
    }
}

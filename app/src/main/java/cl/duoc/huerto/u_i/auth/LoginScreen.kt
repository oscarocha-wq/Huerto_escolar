package cl.duoc.huerto.u_i.auth

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.u_i.BotonAccion
import cl.duoc.huerto.u_i.MensajeError

@Composable
fun LoginScreen(
    onLoginExitoso: () -> Unit,
    viewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    // Cuando el login es exitoso, avisamos una sola vez y consumimos el evento
    LaunchedEffect(estado.loginExitoso) {
        if (estado.loginExitoso) {
            onLoginExitoso()
            viewModel.loginConsumido()
        }
    }

    LoginContent(
        estado = estado,
        onUsuarioChange = viewModel::onUsuarioChange,
        onClaveChange = viewModel::onClaveChange,
        onIngresar = viewModel::iniciarSesion
    )
}

@Composable
private fun LoginContent(
    estado: LoginUiState,
    onUsuarioChange: (String) -> Unit,
    onClaveChange: (String) -> Unit,
    onIngresar: () -> Unit
) {
    var mostrarClave by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Eco,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Huerto Escolar",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = estado.usuario,
                onValueChange = onUsuarioChange,
                label = { Text("Usuario") },
                singleLine = true,
                enabled = !estado.cargando,
                isError = estado.error != null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = estado.clave,
                onValueChange = onClaveChange,
                label = { Text("Clave") },
                singleLine = true,
                enabled = !estado.cargando,
                isError = estado.error != null,
                visualTransformation =
                    if (mostrarClave) VisualTransformation.None
                    else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { mostrarClave = !mostrarClave }) {
                        Icon(
                            imageVector =
                                if (mostrarClave) Icons.Filled.VisibilityOff
                                else Icons.Filled.Visibility,
                            contentDescription =
                                if (mostrarClave) "Ocultar clave" else "Mostrar clave"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Mensaje de error animado, con ícono
            Spacer(Modifier.height(8.dp))
            MensajeError(estado.error, Modifier.fillMaxWidth())

            Spacer(Modifier.height(16.dp))

            BotonAccion(
                texto = "Ingresar",
                cargando = estado.cargando,
                onClick = onIngresar,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Ayuda para la demostración (solo cuentas ficticias)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Cuentas de prueba (datos ficticios)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Docente: ADM-01 / 1234", style = MaterialTheme.typography.bodyMedium)
                    Text("Estudiante: EST-01 / 1234", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
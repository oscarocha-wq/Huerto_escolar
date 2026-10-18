package cl.duoc.huerto.u_i.avisos

import android.Manifest
import android.app.Application
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.huerto.data.SesionManager
import cl.duoc.huerto.domain.mensaje

@Composable
fun AvisosNotificacion(
    viewModel: AvisosViewModel = viewModel(
        factory = AvisosViewModel.factory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val context = LocalContext.current
    val sesion by SesionManager.sesion.collectAsStateWithLifecycle()
    val resumen by viewModel.resumen.collectAsStateWithLifecycle()

    var permiso by remember { mutableStateOf(Notificador.tienePermiso(context)) }
    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { permiso = it } // si lo niega, la app sigue funcionando sin avisos

    LaunchedEffect(Unit) {
        if (!permiso && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(resumen, permiso, sesion) {
        val texto = resumen.mensaje(sesion?.esAdministrador == true)
        if (permiso && texto != null) {
            Notificador.mostrarResumen(context, "Huerto Escolar", texto)
        } else {
            Notificador.cancelar(context)
        }
    }

    // Al cerrar sesión, el aviso de esa persona no debe quedar visible
    DisposableEffect(Unit) { onDispose { Notificador.cancelar(context) } }
}
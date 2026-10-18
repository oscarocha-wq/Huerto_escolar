package cl.duoc.huerto.u_i

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cl.duoc.huerto.data.copiarEvidencia
import cl.duoc.huerto.data.crearArchivoCamara
import cl.duoc.huerto.data.uriDeArchivo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Elegir una foto desde la galería (Photo Picker) o tomarla con la cámara.
 * Muestra la vista previa. No contiene lógica de negocio: avisa con callbacks.
 */
@Composable
fun SelectorFoto(
    fotoRuta: String?,
    onFoto: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
    onQuitar: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Sobrevive a rotaciones mientras la cámara está abierta
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    val hayCamara = remember {
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    // --- Galería (no requiere permisos) ---
    val galeria = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        // Si el usuario cancela, uri es null y no pasa nada
        if (uri != null) {
            scope.launch {
                val ruta = withContext(Dispatchers.IO) { copiarEvidencia(context, uri) }
                if (ruta != null) onFoto(ruta) else onError("No se pudo cargar la imagen")
            }
        }
    }

    // --- Cámara ---
    val camara = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaPendiente
        rutaPendiente = null
        if (ruta != null) {
            val archivo = File(ruta)
            if (exito && archivo.length() > 0) onFoto(ruta)
            else archivo.delete() // cancelada: no dejamos archivos vacíos
        }
    }

    val lanzarCamara = {
        try {
            val archivo = crearArchivoCamara(context)
            rutaPendiente = archivo.absolutePath
            camara.launch(uriDeArchivo(context, archivo))
        } catch (e: Exception) {
            rutaPendiente = null
            onError("No se pudo abrir la cámara")
        }
    }

    val pedirPermisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) lanzarCamara()
        else onError("Sin permiso de cámara. Puedes usar la galería o activarlo en Ajustes.")
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    galeria.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Galería")
            }
            if (hayCamara) {
                OutlinedButton(
                    onClick = {
                        val tienePermiso = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                        if (tienePermiso) lanzarCamara()
                        else pedirPermisoCamara.launch(Manifest.permission.CAMERA)
                    },
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cámara")
                }
            }
        }

        fotoRuta?.let {
            ImagenLocal(
                it,
                Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium)
            )
            if (onQuitar != null) {
                TextButton(onClick = onQuitar) { Text("Quitar fotografía") }
            }
        }
    }
}
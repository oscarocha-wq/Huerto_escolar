package cl.duoc.huerto.data

import android.content.Context
import android.net.Uri
import java.io.File
import androidx.core.content.FileProvider

/** Copia la imagen elegida al almacenamiento interno de la app. Devuelve la ruta o null. */
fun copiarEvidencia(context: Context, uri: Uri): String? {
    return try {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val destino = File(carpeta, "ev_${System.currentTimeMillis()}.jpg")
        val entrada = context.contentResolver.openInputStream(uri) ?: return null
        entrada.use { origen ->
            destino.outputStream().use { salida -> origen.copyTo(salida) }
        }
        destino.absolutePath
    } catch (e: Exception) {
        null
    }
}

/** Archivo vacío (en la carpeta interna de evidencias) donde la cámara guardará la foto. */
fun crearArchivoCamara(context: Context): File {
    val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
    return File(carpeta, "cam_${System.currentTimeMillis()}.jpg")
}

/** URI segura (content://) para que la app de cámara pueda escribir en nuestro archivo. */
fun uriDeArchivo(context: Context, archivo: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
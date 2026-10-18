package cl.duoc.huerto.u_i.avisos

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import cl.duoc.huerto.MainActivity

object Notificador {
    private const val CANAL = "avisos_huerto"
    private const val ID_RESUMEN = 1

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL, "Avisos del huerto", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Tareas atrasadas, por validar y problemas sin revisar" }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(canal)
        }
    }

    /** En Android 12 o inferior no hay permiso que pedir. */
    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // se verifica con tienePermiso()
    fun mostrarResumen(context: Context, titulo: String, texto: String) {
        if (!tienePermiso(context)) return
        val abrirApp = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(texto)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true) // actualizar el aviso no vuelve a sonar
            .build()
        // Mismo id: el aviso nuevo reemplaza al anterior, no se acumulan
        androidx.core.app.NotificationManagerCompat.from(context).notify(ID_RESUMEN, notificacion)
    }

    fun cancelar(context: Context) {
        androidx.core.app.NotificationManagerCompat.from(context).cancel(ID_RESUMEN)
    }
}
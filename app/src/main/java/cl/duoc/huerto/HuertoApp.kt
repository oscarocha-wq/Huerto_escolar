package cl.duoc.huerto

import android.app.Application
import cl.duoc.huerto.core.notification.Notificador

class HuertoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Notificador.crearCanal(this)
    }
}

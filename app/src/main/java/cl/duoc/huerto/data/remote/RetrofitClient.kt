package cl.duoc.huerto.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit único para toda la app.
 *
 * 10.0.2.2 es "el localhost del computador" visto desde el EMULADOR de Android.
 * Con un celular físico, cambia BASE_URL por la IP del PC en la red Wi-Fi
 * (por ejemplo http://192.168.1.15:8080/) y agrégala en res/xml/network_security_config.xml.
 */
object RetrofitClient {
    const val BASE_URL = "http://10.0.2.2:8080/"

    // Tiempos cortos: si el backend no está encendido, la app sigue rápido con los datos locales
    private val http = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    val api: HuertoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuertoApi::class.java)
    }
}

import com.ivandev.proyectoveterinaria.network.UsuarioService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api-veterinaria-service.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES) // Tiempo para establecer conexión
        .readTimeout(1, TimeUnit.MINUTES)    // Tiempo para recibir datos
        .writeTimeout(1, TimeUnit.MINUTES)   // Tiempo para enviar datos
        .build()

    val instance: UsuarioService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UsuarioService::class.java)
    }
}
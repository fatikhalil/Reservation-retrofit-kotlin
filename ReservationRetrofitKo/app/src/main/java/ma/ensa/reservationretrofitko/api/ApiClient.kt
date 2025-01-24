package ma.ensa.reservationretrofitko.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {
    companion object {
        private const val BASE_URL = "http://192.168.1.160:8082/" // Remplacez par l'URL de votre backend
        private var retrofit: Retrofit? = null

        fun getClient(): Retrofit {
            if (retrofit == null) {
                // Créer un interceptor pour mesurer la taille des données
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY // Niveau BODY pour voir les détails des requêtes
                }

                // Créer un interceptor personnalisé pour mesurer la taille des données reçues
                val httpClient = OkHttpClient.Builder().apply {
                    addInterceptor { chain ->
                        val request = chain.request()
                        val response = chain.proceed(request)

                        // Mesurer la taille des données reçues
                        val contentLength = response.body?.contentLength() ?: 0 // Taille en octets
                        val sizeInKB = contentLength / 1024.0 // Convertir en KB

                        println("Taille des données reçues : $sizeInKB KB")

                        response
                    }
                    addInterceptor(loggingInterceptor) // Ajouter le logging interceptor
                }

                // Configurer Retrofit avec OkHttpClient personnalisé
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build()
            }
            return retrofit!!
        }
    }
}
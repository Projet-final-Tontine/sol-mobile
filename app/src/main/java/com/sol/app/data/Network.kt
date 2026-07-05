package com.sol.app.data

import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/** Configuration reseau : Retrofit + jeton JWT automatique. */
object Network {

    // 10.0.2.2 correspond au "localhost" de la machine hote vu depuis l'emulateur Android.
    // A remplacer par l'adresse IP du serveur lors d'un test sur telephone reel.
    private const val BASE_URL = "http://192.168.0.112:8080/"

    private val authInterceptor = Interceptor { chain ->
        val requete = chain.request().newBuilder()
        Session.token?.let { requete.addHeader("Authorization", "Bearer $it") }
        chain.proceed(requete.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        )
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

/** Transforme une exception reseau/metier en message lisible pour l'utilisateur. */
fun messageErreur(e: Throwable): String = when (e) {
    is HttpException -> {
        val corps = e.response()?.errorBody()?.string()
        try {
            JsonParser.parseString(corps).asJsonObject.get("message").asString
        } catch (_: Exception) {
            "Erreur ${e.code()}"
        }
    }
    is IOException -> "Impossible de joindre le serveur. Verifiez votre connexion."
    else -> e.message ?: "Une erreur est survenue."
}

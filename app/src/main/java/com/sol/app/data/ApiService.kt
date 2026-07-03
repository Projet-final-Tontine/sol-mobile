package com.sol.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Points d'acces de l'API backend utilises par l'application mobile. */
interface ApiService {

    @POST("api/auth/inscription")
    suspend fun inscription(@Body body: InscriptionRequest): UtilisateurResponse

    @POST("api/auth/connexion")
    suspend fun connexion(@Body body: ConnexionRequest): ConnexionResponse

    @GET("api/sols/mes-sols")
    suspend fun mesSols(): List<SolResponse>

    @POST("api/sols/rejoindre")
    suspend fun rejoindre(@Body body: RejoindreRequest): MembreSolResponse
}

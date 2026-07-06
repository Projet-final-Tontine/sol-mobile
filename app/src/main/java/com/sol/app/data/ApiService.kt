package com.sol.app.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

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

    @POST("api/sols")
    suspend fun creerSol(@Body body: CreerSolRequest): SolResponse

    @GET("api/sols/{solId}/membres")
    suspend fun membresDuSol(@Path("solId") solId: String): List<MembreSolResponse>

    @POST("api/sols/{solId}/demarrer")
    suspend fun demarrerCycle(@Path("solId") solId: String): SolResponse

    @DELETE("api/sols/{solId}/membres/moi")
    suspend fun quitterSol(@Path("solId") solId: String): Response<Unit>

    @GET("api/mes-cotisations")
    suspend fun mesCotisations(): List<CotisationResponse>

    @GET("api/portefeuille")
    suspend fun portefeuille(): PortefeuilleResponse

    @POST("api/portefeuille/depot")
    suspend fun deposer(@Body body: DepotRequest): PortefeuilleResponse

    @POST("api/cotisations/{cotisationId}/payer")
    suspend fun payerCotisation(
        @Path("cotisationId") cotisationId: String,
        @Body body: PayerCotisationRequest,
    ): PaiementResponse

    @GET("api/sols/{solId}/paiements-en-attente")
    suspend fun paiementsEnAttente(@Path("solId") solId: String): List<PaiementResponse>

    @POST("api/paiements/{paiementId}/valider")
    suspend fun validerPaiement(@Path("paiementId") paiementId: String): CotisationResponse

    @POST("api/paiements/{paiementId}/rejeter")
    suspend fun rejeterPaiement(@Path("paiementId") paiementId: String): CotisationResponse

    @POST("api/sols/{solId}/tours")
    suspend fun ouvrirTour(
        @Path("solId") solId: String,
        @Body body: OuvrirTourRequest,
    ): TourResponse

    @PUT("api/auth/profil")
    suspend fun modifierProfil(@Body body: ModifierProfilRequest): UtilisateurResponse

    @POST("api/auth/changer-mot-de-passe")
    suspend fun changerMotDePasse(@Body body: ChangerMotDePasseRequest): Map<String, String>

    @Multipart
    @POST("api/fichiers/photo")
    suspend fun televerserPhoto(@Part fichier: MultipartBody.Part): Map<String, String>
}

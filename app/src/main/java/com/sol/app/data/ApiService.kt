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
import retrofit2.http.Query

/** Points d'acces de l'API backend utilises par l'application mobile. */
interface ApiService {

    @POST("api/auth/inscription")
    suspend fun inscription(@Body body: InscriptionRequest): UtilisateurResponse

    @POST("api/auth/connexion")
    suspend fun connexion(@Body body: ConnexionRequest): ConnexionResponse

    /** Connexion « Continuer avec Google » (jeton Firebase vérifié côté serveur). */
    @POST("api/auth/google")
    suspend fun connexionGoogle(@Body body: GoogleAuthRequest): ConnexionResponse

    @GET("api/sols/mes-sols")
    suspend fun mesSols(): List<SolResponse>

    @GET("api/sols/mes-tours")
    suspend fun mesTours(): List<MonTourResponse>

    @POST("api/sols/rejoindre")
    suspend fun rejoindre(@Body body: RejoindreRequest): MembreSolResponse

    @POST("api/sols")
    suspend fun creerSol(@Body body: CreerSolRequest): SolResponse

    @GET("api/sols/{solId}/membres")
    suspend fun membresDuSol(@Path("solId") solId: String): List<MembreSolResponse>

    // ----- Demandes d'adhesion (Manman sol) -----

    @GET("api/sols/{solId}/demandes")
    suspend fun demandesAdhesion(@Path("solId") solId: String): List<MembreSolResponse>

    @POST("api/sols/membres/{membreSolId}/approuver")
    suspend fun approuverMembre(@Path("membreSolId") membreSolId: String): MembreSolResponse

    @POST("api/sols/membres/{membreSolId}/refuser")
    suspend fun refuserMembre(@Path("membreSolId") membreSolId: String): Response<Unit>

    @GET("api/sols/{solId}/detail")
    suspend fun detailDuSol(@Path("solId") solId: String): SolDetailResponse

    // ----- Sondages / votes -----

    @POST("api/sols/{solId}/sondages")
    suspend fun creerSondage(
        @Path("solId") solId: String,
        @Body body: CreerSondageRequest,
    ): SondageResponse

    @GET("api/sols/{solId}/sondages")
    suspend fun sondagesDuSol(@Path("solId") solId: String): List<SondageResponse>

    @POST("api/sondages/{sondageId}/voter")
    suspend fun voterSondage(
        @Path("sondageId") sondageId: String,
        @Body body: VoterRequest,
    ): SondageResponse

    @POST("api/sondages/{sondageId}/cloturer")
    suspend fun cloturerSondage(@Path("sondageId") sondageId: String): SondageResponse

    // ----- Messagerie -----

    @GET("api/sols/{solId}/messages")
    suspend fun messagesSol(@Path("solId") solId: String): List<MessageResponse>

    @POST("api/sols/{solId}/messages")
    suspend fun envoyerAuSol(
        @Path("solId") solId: String,
        @Body body: EnvoyerMessageRequest,
    ): MessageResponse

    @GET("api/messages/prive/{autreId}")
    suspend fun messagesPrives(@Path("autreId") autreId: String): List<MessageResponse>

    /** Messages récents destinés à l'utilisateur (notifications). */
    @GET("api/messages/recents")
    suspend fun messagesRecents(@Query("depuis") depuis: String?): List<MessageRecentResponse>

    /** Génère le Relevé de Fiabilité Financière du membre connecté. */
    @GET("api/releve")
    suspend fun monReleve(): ReleveResponse

    // ----- Registre Inviolable (grand livre à hash chaîné) -----

    /** Liste des blocs scellés (du plus récent au plus ancien). */
    @GET("api/registre")
    suspend fun registre(): List<BlocRegistreResponse>

    /** Vérifie l'intégrité de toute la chaîne (badge « inviolable »). */
    @GET("api/registre/verifier")
    suspend fun verifierRegistre(): VerificationRegistreResponse

    // ----- Fon Sekou (caisse de solidarité) -----

    @GET("api/sols/{solId}/fon-sekou")
    suspend fun fonSekou(@Path("solId") solId: String): FonSekouResponse

    @POST("api/sols/{solId}/fon-sekou/contribuer")
    suspend fun contribuerSekou(
        @Path("solId") solId: String,
        @Body body: ContribuerRequest,
    ): FonSekouResponse

    @POST("api/sols/{solId}/fon-sekou/demandes")
    suspend fun demanderSekou(
        @Path("solId") solId: String,
        @Body body: DemanderSekouRequest,
    ): FonSekouResponse

    @POST("api/fon-sekou/demandes/{demandeId}/voter")
    suspend fun voterSekou(
        @Path("demandeId") demandeId: String,
        @Body body: VoterSekouRequest,
    ): FonSekouResponse

    @POST("api/fon-sekou/demandes/{demandeId}/cloturer")
    suspend fun cloturerSekou(@Path("demandeId") demandeId: String): FonSekouResponse

    @POST("api/messages/prive/{destinataireId}")
    suspend fun envoyerPrive(
        @Path("destinataireId") destinataireId: String,
        @Body body: EnvoyerMessageRequest,
    ): MessageResponse

    @POST("api/sols/{solId}/demarrer")
    suspend fun demarrerCycle(@Path("solId") solId: String): SolResponse

    @DELETE("api/sols/{solId}/membres/moi")
    suspend fun quitterSol(@Path("solId") solId: String): Response<Unit>

    @GET("api/mes-cotisations")
    suspend fun mesCotisations(): List<CotisationResponse>

    @GET("api/portefeuille")
    suspend fun portefeuille(): PortefeuilleResponse

    /** Vue consolidée « Mon activité » (indicateurs + projections + courbe). */
    @GET("api/tableau-de-bord")
    suspend fun tableauDeBord(): TableauDeBordResponse

    @POST("api/portefeuille/depot")
    suspend fun deposer(@Body body: DepotRequest): PortefeuilleResponse

    // ----- Passerelle de paiement (dépôt / retrait) -----

    /** Crée un ordre de paiement ; l'app ouvrira l'URL renvoyée dans le navigateur. */
    @POST("api/portefeuille/paiement/initier")
    suspend fun initierPaiement(@Body body: InitierPaiementRequest): InitierPaiementResponse

    /** État de l'ordre de paiement (interrogé au retour dans l'app). */
    @GET("api/portefeuille/paiement/statut/{orderId}")
    suspend fun statutPaiement(@Path("orderId") orderId: String): StatutPaiementResponse

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

    // ----- Vérification d'identité (KYC) -----

    /** État courant du KYC (identité pré-remplie + statut). */
    @GET("api/kyc")
    suspend fun kyc(): KycEtatResponse

    /** Étape 1 : confirme/corrige l'identité. */
    @PUT("api/kyc/identite")
    suspend fun majIdentiteKyc(@Body body: MajIdentiteRequest): KycEtatResponse

    /** Étape finale : soumission des documents. */
    @POST("api/kyc/soumettre")
    suspend fun soumettreKyc(@Body body: SoumettreKycRequest): KycEtatResponse

    // ----- Annuaire public (username, recherche, transfert) -----

    /** Disponibilité d'un username (temps réel, public). */
    @GET("api/users/username-disponible")
    suspend fun usernameDisponible(@Query("username") username: String): DisponibiliteResponse

    /** Modifie le username de l'utilisateur connecté. */
    @PUT("api/users/username")
    suspend fun modifierUsername(@Body body: MajUsernameRequest): UtilisateurResponse

    /** Recherche un bénéficiaire par username ou e-mail (détection auto). */
    @GET("api/users/search")
    suspend fun rechercherUtilisateur(@Query("q") q: String): RechercheUtilisateurResponse

    /** Transfert d'argent vers un autre utilisateur (renvoie le reçu numérique). */
    @POST("api/transferts")
    suspend fun transferer(@Body body: TransfertRequest): RecuTransfertResponse

    /** Historique des transferts (envoyés + reçus). */
    @GET("api/transferts")
    suspend fun historiqueTransferts(
        @Query("filtre") filtre: String? = null,
        @Query("recherche") recherche: String? = null,
    ): List<TransfertHistoriqueItem>

    /** Détail d'un transfert. */
    @GET("api/transferts/{id}")
    suspend fun detailTransfert(@Path("id") id: String): TransfertDetailResponse

    /** Liste des bénéficiaires favoris. */
    @GET("api/favoris")
    suspend fun favoris(): List<FavoriResponse>

    /** Ajoute un bénéficiaire aux favoris. */
    @POST("api/favoris/{beneficiaireId}")
    suspend fun ajouterFavori(@Path("beneficiaireId") beneficiaireId: String): Map<String, String>

    /** Retire un bénéficiaire des favoris. */
    @DELETE("api/favoris/{beneficiaireId}")
    suspend fun supprimerFavori(@Path("beneficiaireId") beneficiaireId: String): Map<String, String>

    @PUT("api/auth/profil")
    suspend fun modifierProfil(@Body body: ModifierProfilRequest): UtilisateurResponse

    @POST("api/auth/changer-mot-de-passe")
    suspend fun changerMotDePasse(@Body body: ChangerMotDePasseRequest): Map<String, String>

    @Multipart
    @POST("api/fichiers/photo")
    suspend fun televerserPhoto(@Part fichier: MultipartBody.Part): Map<String, String>
}

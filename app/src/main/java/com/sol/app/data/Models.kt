package com.sol.app.data

// Modeles correspondant aux DTO echanges avec l'API backend.

data class InscriptionRequest(
    val nom: String,
    val prenom: String,
    val sexe: String,
    val telephone: String,
    val email: String,
    val adresse: String,
    val cinNif: String,
    val dateNaissance: String, // format AAAA-MM-JJ
    val motDePasse: String,
    val role: String = "MEMBRE",
)

data class ConnexionRequest(
    val telephone: String,
    val motDePasse: String,
)

data class UtilisateurResponse(
    val id: String,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val statut: String,
)

data class ConnexionResponse(
    val token: String,
    val utilisateur: UtilisateurResponse,
)

data class SolResponse(
    val id: String,
    val nom: String,
    val description: String?,
    val codeInvitation: String,
    val nombreMaxMembres: Int,
    val montantCotisation: Double,
    val frequence: String,
    val statut: String,
    val mamanSolId: String,
)

data class RejoindreRequest(
    val codeInvitation: String,
)

data class MembreSolResponse(
    val id: String,
    val utilisateurId: String,
    val nomComplet: String?,
    val solId: String,
    val ordrePassage: Int?,
    val statutMembre: String,
)

data class CreerSolRequest(
    val nom: String,
    val description: String?,
    val nombreMaxMembres: Int,
    val montantCotisation: Double,
    val frequence: String,
    val dateDebut: String, // format AAAA-MM-JJ
)

data class CotisationResponse(
    val id: String,
    val membreSolId: String,
    val solId: String,
    val tourId: String?,
    val montantAttendu: Double,
    val montantPaye: Double?,
    val dateEcheance: String?,
    val statut: String, // EN_ATTENTE, VALIDE...
)

data class PaiementResponse(
    val id: String,
    val cotisationId: String,
    val utilisateurId: String,
    val typePaiement: String?,
    val referenceTransaction: String?,
    val montantPaye: Double?,
    val statutPaiement: String?,
)

data class PayerCotisationRequest(
    val referenceMonCash: String,
)

data class OuvrirTourRequest(
    val datePrevue: String, // format AAAA-MM-JJ
)

data class TourResponse(
    val id: String,
    val statut: String?,
)

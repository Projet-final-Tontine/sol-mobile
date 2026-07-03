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
    val solId: String,
    val ordrePassage: Int?,
    val statutMembre: String,
)

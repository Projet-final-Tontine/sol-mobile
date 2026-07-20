package com.sol.app.data

// Modeles correspondant aux DTO echanges avec l'API backend.

data class InscriptionRequest(
    val nom: String,
    val prenom: String,
    val sexe: String,
    val telephone: String,
    val email: String,
    val username: String,
    val adresse: String,
    val dateNaissance: String, // format AAAA-MM-JJ
    val motDePasse: String,
    val role: String = "MEMBRE",
)

// ----- Annuaire public (username, recherche, transfert) -----

data class DisponibiliteResponse(val disponible: Boolean, val message: String)
data class MajUsernameRequest(val username: String)

/** Résultat de recherche d'un bénéficiaire (avant un transfert). */
data class RechercheUtilisateurResponse(
    val id: String,
    val username: String?,
    val nomComplet: String?,
    val photoUrl: String?,
    val kycVerifie: Boolean,
)

data class TransfertRequest(
    val beneficiaire: String,   // username (@...) ou e-mail
    val montant: Double,
    val note: String?,
)

data class ConnexionRequest(
    val telephone: String,
    val motDePasse: String,
)

/** Requête « Continuer avec Google » : le jeton d'identité Firebase. */
data class GoogleAuthRequest(
    val idToken: String,
)

data class UtilisateurResponse(
    val id: String,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val username: String?,
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

data class ModifierProfilRequest(
    val nom: String?,
    val prenom: String?,
    val adresse: String?,
    val photoUrl: String?,
)

data class ChangerMotDePasseRequest(
    val ancienMotDePasse: String,
    val nouveauMotDePasse: String,
)

// ----- Vérification d'identité (KYC) -----

/** État courant du KYC : identité pré-remplie + statut de vérification. */
data class KycEtatResponse(
    val nom: String?,
    val prenom: String?,
    val dateNaissance: String?,   // AAAA-MM-JJ
    val adresse: String?,
    val statut: String,           // NON_SOUMIS, SOUMIS, APPROUVE, REJETE
    val typeDocument: String?,
    val dateSoumission: String?,
)

/** Étape 1 : confirmation/correction de l'identité. */
data class MajIdentiteRequest(
    val nom: String?,
    val prenom: String?,
    val dateNaissance: String?,
    val adresse: String?,
)

/** Étape finale : soumission des documents. */
data class SoumettreKycRequest(
    val typeDocument: String,     // CARTE_IDENTITE, PASSEPORT, PERMIS
    val rectoUrl: String,
    val versoUrl: String?,
)

// ----- Portefeuille (wallet) -----

data class DepotRequest(
    val montant: Double,
    val referenceMonCash: String,
)

data class TransactionPortefeuilleResponse(
    val id: String,
    val type: String,        // DEPOT, COTISATION, GAIN_MAIN, RETRAIT
    val sens: String,        // CREDIT, DEBIT
    val montant: Double,
    val soldeApres: Double,
    val referenceExterne: String?,
    val description: String?,
    val dateCreation: String?,
)

data class PortefeuilleResponse(
    val id: String,
    val solde: Double,
    val transactions: List<TransactionPortefeuilleResponse>,
)

// ----- Detail complet d'un Sol -----

data class TourInfo(
    val id: String,
    val numero: Int?,
    val beneficiaireId: String?,
    val beneficiaireNom: String?,
    val datePrevue: String?,
    val statut: String?,
    val montantPot: Double?,
)

data class EtatCotisation(
    val utilisateurId: String,
    val membreNom: String?,
    val photoUrl: String?,
    val ordre: Int?,
    val montant: Double,
    val statut: String,       // EN_ATTENTE, VALIDE, REJETE
    val dateEcheance: String?,
)

data class MembreInfo(
    val utilisateurId: String,
    val nom: String?,
    val photoUrl: String?,
    val ordre: Int?,
    val statutMembre: String?,
)

data class PositionMembre(
    val ordre: Int,
    val total: Int,
    val datePrevue: String?,
    val dateEstimee: Boolean,
)

data class SanteSol(
    val score: Int,
    val niveau: String,   // EXCELLENT, MOYEN, RISQUE
)

data class EvenementJournal(
    val date: String?,
    val type: String,     // ADHESION, PAIEMENT, MAIN, TOUR_OUVERT, RETARD
    val acteurNom: String?,
    val montant: Double?,
)

data class SolDetailResponse(
    val sol: SolResponse,
    val nombreMembres: Int,
    val toursJoues: Int,
    val totalTours: Int,
    val tourCourant: TourInfo?,
    val tours: List<TourInfo>,
    val etatCotisations: List<EtatCotisation>,
    val membres: List<MembreInfo>,
    val maPosition: PositionMembre?,
    val sante: SanteSol?,
    val journal: List<EvenementJournal>?,
)

// ----- Calendrier intelligent : mes tours (distributions) -----

data class MonTourResponse(
    val solId: String,
    val solNom: String?,
    val numero: Int?,
    val beneficiaireNom: String?,
    val jeSuisBeneficiaire: Boolean,
    val datePrevue: String?,   // format AAAA-MM-JJ
    val statut: String?,
)

// ----- Sondages / votes de groupe -----

data class CreerSondageRequest(
    val question: String,
    val options: List<String>,
)

data class VoterRequest(
    val optionIndex: Int,
)

data class OptionResultat(
    val index: Int,
    val texte: String,
    val votes: Int,
    val pourcentage: Int,
)

data class SondageResponse(
    val id: String,
    val question: String,
    val createurNom: String?,
    val statut: String,          // OUVERT, CLOS
    val dateCreation: String?,
    val totalVotes: Int,
    val monVoteIndex: Int?,      // index de mon vote, ou null
    val peutCloturer: Boolean,
    val options: List<OptionResultat>,
)

// ----- Messagerie (chat) -----

data class MessageResponse(
    val id: String,
    val expediteurId: String,
    val expediteurNom: String?,
    val expediteurPhoto: String?,
    val contenu: String,
    val pieceJointeUrl: String? = null,   // URL de l'image / document joint
    val typePiece: String? = null,        // "IMAGE" ou "DOCUMENT"
    val dateEnvoi: String?,
)

data class EnvoyerMessageRequest(
    val contenu: String,
    val pieceJointeUrl: String? = null,
    val typePiece: String? = null,
)

// ---------------- Fon Sekou (caisse de solidarité) ----------------

data class FonSekouResponse(
    val solde: Double,
    val estMamanSol: Boolean,
    val demandes: List<DemandeSekouInfo>,
)

data class DemandeSekouInfo(
    val id: String,
    val demandeurId: String,
    val demandeurNom: String?,
    val type: String,
    val montant: Double,
    val motif: String,
    val justificatifUrl: String?,
    val statut: String,          // EN_ATTENTE, PAYE, REJETE
    val nbPour: Long,
    val nbContre: Long,
    val monVote: Boolean?,        // true=pour, false=contre, null=pas voté
    val estMoi: Boolean,
    val dateCreation: String?,
)

data class ContribuerRequest(val montant: Double)

data class DemanderSekouRequest(
    val type: String,
    val montant: Double,
    val motif: String,
    val justificatifUrl: String? = null,
)

data class VoterSekouRequest(val pour: Boolean)

/** Relevé de Fiabilité Financière du membre (certificat vérifiable par QR). */
data class ReleveResponse(
    val reference: String,
    val nomComplet: String?,
    val membreDepuis: String?,
    val nbSols: Int,
    val totalCotise: Double,
    val nbCotisations: Int,
    val nbATemps: Int,
    val nbRetards: Int,
    val nbDefauts: Int,
    val scoreGlobal: Int,
    val note: String,
    val niveau: String,
    val historiqueSuffisant: Boolean,
    val dateEmission: String?,
    val hash: String,
    val urlVerification: String,
)

// ----- Tableau de bord « Mon activité » (accueil) -----

data class TableauDeBordResponse(
    val soldePortefeuille: Double,
    val totalCotise: Double,
    val totalARecevoir: Double,
    val nbSolsActifs: Int,
    val prochaineEcheance: EcheanceInfo?,   // null si rien à payer
    val prochaineMain: MainInfo?,           // null si aucune main à venir
    val epargne: List<PointEpargneInfo>,
)

data class EcheanceInfo(val date: String?, val montant: Double, val solNom: String?)
data class MainInfo(val date: String?, val montant: Double, val solNom: String?)
data class PointEpargneInfo(val date: String?, val cumul: Double)

// ----- Passerelle de paiement (dépôt / retrait via MonCash, NatCash, cartes) -----

/** Demande d'initialisation d'un paiement. */
data class InitierPaiementRequest(
    val sens: String,     // DEPOT ou RETRAIT
    val moyen: String,    // MONCASH, NATCASH, VISA, MASTERCARD, VIREMENT
    val montant: Double,
)

/** Réponse : l'app ouvre redirectUrl dans le navigateur pour finaliser. */
data class InitierPaiementResponse(
    val orderId: String,
    val reference: String,
    val redirectUrl: String,
    val statut: String,
)

/** État d'un ordre de paiement (interrogé au retour du navigateur). */
data class StatutPaiementResponse(
    val orderId: String,
    val sens: String,
    val moyen: String,
    val montant: Double,
    val statut: String,   // EN_ATTENTE, PAYE, ECHOUE
    val reference: String,
)

/** Un bloc du Registre Inviolable (grand livre à hash chaîné). */
data class BlocRegistreResponse(
    val position: Long,
    val date: String?,
    val type: String,           // DEPOT, COTISATION, GAIN_MAIN, FON_SEKOU…
    val sens: String,           // CREDIT, DEBIT
    val montant: Double,
    val description: String?,
    val hash: String,
    val hashPrecedent: String,
)

/** Résultat de la vérification d'intégrité du Registre Inviolable. */
data class VerificationRegistreResponse(
    val intacte: Boolean,
    val nombreBlocs: Long,
    val positionRupture: Long?,   // null si la chaîne est intègre
    val empreinteGlobale: String?,
    val message: String,
)

/** Message récent destiné à l'utilisateur (pour les notifications). */
data class MessageRecentResponse(
    val expediteurNom: String?,
    val apercu: String?,
    val solId: String? = null,   // null si message privé
    val solNom: String? = null,  // nom du Sol (groupe) ou null (privé)
    val dateEnvoi: String?,
)

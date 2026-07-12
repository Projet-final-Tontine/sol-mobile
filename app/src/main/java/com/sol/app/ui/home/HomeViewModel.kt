package com.sol.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.CotisationResponse
import com.sol.app.data.CreerSolRequest
import com.sol.app.data.EnvoyerMessageRequest
import com.sol.app.data.MessageResponse
import com.sol.app.data.MembreSolResponse
import com.sol.app.data.CreerSondageRequest
import com.sol.app.data.SondageResponse
import com.sol.app.data.VoterRequest
import com.sol.app.data.Session
import com.sol.app.data.MonTourResponse
import com.sol.app.data.Network
import com.sol.app.data.OuvrirTourRequest
import com.sol.app.data.PaiementResponse
import com.sol.app.data.PayerCotisationRequest
import com.sol.app.data.PortefeuilleResponse
import com.sol.app.data.RejoindreRequest
import com.sol.app.data.SolDetailResponse
import com.sol.app.data.SolResponse
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Ecran d'accueil : Sols, cotisations (solde, paiement Mon Cash),
 * validation des paiements et tours (Manman sol).
 */
class HomeViewModel : ViewModel() {

    var sols by mutableStateOf<List<SolResponse>>(emptyList())
        private set
    var cotisations by mutableStateOf<List<CotisationResponse>>(emptyList())
        private set
    // Tours (distributions) de tous mes Sols : alimente le calendrier intelligent.
    var mesTours by mutableStateOf<List<MonTourResponse>>(emptyList())
        private set
    var enChargement by mutableStateOf(false)
        private set
    var erreur by mutableStateOf<String?>(null)
        private set
    var messageSucces by mutableStateOf<String?>(null)
        private set

    var dialogueRejoindreOuvert by mutableStateOf(false)
        private set
    var dialogueCreerOuvert by mutableStateOf(false)
        private set

    // Cotisation en cours de paiement (ouvre le dialogue Mon Cash).
    var cotisationAPayer by mutableStateOf<CotisationResponse?>(null)
        private set

    // Sol dont on affiche le detail.
    var solDetail by mutableStateOf<SolResponse?>(null)
        private set
    var membresDetail by mutableStateOf<List<MembreSolResponse>>(emptyList())
        private set
    // Demandes d'adhesion en attente (visibles par la Manman sol).
    var demandesAdhesion by mutableStateOf<List<MembreSolResponse>>(emptyList())
        private set
    // Sondages / votes du Sol affiche.
    var sondages by mutableStateOf<List<SondageResponse>>(emptyList())
        private set
    var dialogueSondageOuvert by mutableStateOf(false)
        private set
    // Detail complet du Sol : progression, tour courant, cotisations, position.
    var detailComplet by mutableStateOf<SolDetailResponse?>(null)
        private set

    // ----- Chat (groupe / prive) -----
    var chatCible by mutableStateOf<ChatCible?>(null)
        private set
    var messagesChat by mutableStateOf<List<MessageResponse>>(emptyList())
        private set
    var envoiEnCours by mutableStateOf(false)
        private set
    var choixMembrePriveOuvert by mutableStateOf(false)
        private set
    var paiementsEnAttente by mutableStateOf<List<PaiementResponse>>(emptyList())
        private set
    var chargementMembres by mutableStateOf(false)
        private set
    var dialogueTourOuvert by mutableStateOf(false)
        private set

    // ----- Portefeuille (wallet) -----
    var portefeuille by mutableStateOf<PortefeuilleResponse?>(null)
        private set

    // Dialogue « bientot disponible » (depot / retrait a venir via Mon Cash).
    var dialogueBientotOuvert by mutableStateOf(false)
        private set
    var texteBientot by mutableStateOf("")
        private set

    /** Solde disponible dans le portefeuille de l'utilisateur. */
    val solde: Double
        get() = portefeuille?.solde ?: 0.0

    /** Total deja verse et valide, tous Sols confondus. */
    val soldeTotal: Double
        get() = cotisations
            .filter { it.statut.equals("VALIDE", ignoreCase = true) }
            .sumOf { it.montantPaye ?: it.montantAttendu }

    fun ouvrirDialogueRejoindre() { dialogueRejoindreOuvert = true }
    fun fermerDialogueRejoindre() { dialogueRejoindreOuvert = false }
    fun ouvrirDialogueCreer() { dialogueCreerOuvert = true }
    fun fermerDialogueCreer() { dialogueCreerOuvert = false }
    fun demanderPaiement(c: CotisationResponse) { cotisationAPayer = c }
    fun annulerPaiement() { cotisationAPayer = null }
    fun ouvrirDialogueTour() { dialogueTourOuvert = true }
    fun fermerDialogueTour() { dialogueTourOuvert = false }
    fun montrerBientot(texte: String) { texteBientot = texte; dialogueBientotOuvert = true }
    fun fermerBientot() { dialogueBientotOuvert = false }

    // ----- Moyens de paiement (depot / retrait) -----

    // Type de selecteur ouvert : "DEPOT", "RETRAIT" ou null (ferme).
    var moyensType by mutableStateOf<String?>(null)
        private set
    var ficheOuvert by mutableStateOf(false)
        private set
    var envoiFicheEnCours by mutableStateOf(false)
        private set

    fun ouvrirMoyens(type: String) { moyensType = type }
    fun fermerMoyens() { moyensType = null }
    fun ouvrirFiche() { moyensType = null; ficheOuvert = true }
    fun fermerFiche() { ficheOuvert = false }

    /**
     * Televerse la fiche de paie / le recu d'un depot bancaire. Le fichier est
     * envoye au serveur ; il reste « en attente de confirmation » par l'administrateur.
     */
    fun envoyerFichePaie(octets: ByteArray) {
        erreur = null
        messageSucces = null
        envoiFicheEnCours = true
        viewModelScope.launch {
            try {
                val corps = octets.toRequestBody("image/*".toMediaTypeOrNull())
                val partie = MultipartBody.Part.createFormData("fichier", "recu.jpg", corps)
                Network.api.televerserPhoto(partie)
                messageSucces = "Reçu envoyé ! En attente de confirmation. ✅"
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                envoiFicheEnCours = false
                ficheOuvert = false
            }
        }
    }

    fun chargerTout() {
        chargerMesSols()
        chargerCotisations()
        chargerPortefeuille()
        chargerMesTours()
    }

    /** Recupere les tours (distributions) de tous mes Sols pour le calendrier. */
    fun chargerMesTours() {
        viewModelScope.launch {
            try {
                mesTours = Network.api.mesTours()
            } catch (_: Throwable) {
                // Silencieux : le calendrier restera sans couronnes si l'appel echoue.
            }
        }
    }

    /** Recupere le solde et l'historique du portefeuille. */
    fun chargerPortefeuille() {
        viewModelScope.launch {
            try {
                portefeuille = Network.api.portefeuille()
            } catch (_: Throwable) {
                // Silencieux : le solde restera a zero si l'appel echoue.
            }
        }
    }


    fun chargerMesSols() {
        erreur = null
        enChargement = true
        viewModelScope.launch {
            try {
                sols = Network.api.mesSols()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enChargement = false
            }
        }
    }

    fun chargerCotisations() {
        viewModelScope.launch {
            try {
                cotisations = Network.api.mesCotisations()
            } catch (_: Throwable) {
                // Silencieux : le solde restera a zero si l'appel echoue.
            }
        }
    }

    /**
     * Paie la cotisation en cours depuis le solde du portefeuille.
     * Si le solde est insuffisant, le backend renvoie une erreur invitant a
     * deposer de l'argent d'abord.
     */
    fun payerCotisationDepuisWallet() {
        val cotisation = cotisationAPayer ?: return
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.payerCotisation(
                    cotisation.id, PayerCotisationRequest("PORTEFEUILLE")
                )
                messageSucces = "Cotisation payée depuis votre solde ! ✅"
                chargerCotisations()
                chargerPortefeuille()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                cotisationAPayer = null
            }
        }
    }

    fun ouvrirDetail(sol: SolResponse) {
        solDetail = sol
        membresDetail = emptyList()
        paiementsEnAttente = emptyList()
        demandesAdhesion = emptyList()
        sondages = emptyList()
        detailComplet = null
        chargementMembres = true
        viewModelScope.launch {
            try {
                membresDetail = Network.api.membresDuSol(sol.id)
                paiementsEnAttente = Network.api.paiementsEnAttente(sol.id)
                detailComplet = Network.api.detailDuSol(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                chargementMembres = false
            }
            // Demandes d'adhesion : seulement pour la Manman sol (appel isole).
            if (sol.mamanSolId == Session.utilisateurId) {
                try {
                    demandesAdhesion = Network.api.demandesAdhesion(sol.id)
                } catch (_: Throwable) {
                    // Silencieux : pas de demandes ou acces non autorise.
                }
            }
            // Sondages du Sol (visibles par tous les membres).
            try {
                sondages = Network.api.sondagesDuSol(sol.id)
            } catch (_: Throwable) {
                // Silencieux : pas de sondages ou acces non autorise.
            }
        }
    }

    fun ouvrirDialogueSondage() { dialogueSondageOuvert = true }
    fun fermerDialogueSondage() { dialogueSondageOuvert = false }

    /** Cree un sondage dans le Sol affiche. */
    fun creerSondage(question: String, options: List<String>) {
        val sol = solDetail ?: return
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.creerSondage(sol.id, CreerSondageRequest(question, options))
                messageSucces = "Sondage créé ! 🗳️"
                sondages = Network.api.sondagesDuSol(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                dialogueSondageOuvert = false
            }
        }
    }

    /** Vote (ou change de vote) pour une option d'un sondage. */
    fun voterSondage(sondageId: String, optionIndex: Int) {
        val sol = solDetail ?: return
        viewModelScope.launch {
            try {
                Network.api.voterSondage(sondageId, VoterRequest(optionIndex))
                sondages = Network.api.sondagesDuSol(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    /** Clot un sondage (createur ou Manman sol). */
    fun cloturerSondage(sondageId: String) {
        val sol = solDetail ?: return
        viewModelScope.launch {
            try {
                Network.api.cloturerSondage(sondageId)
                sondages = Network.api.sondagesDuSol(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    /** Approuve une demande d'adhesion (Manman sol) puis rafraichit les listes. */
    fun approuverMembre(membreSolId: String) {
        val sol = solDetail ?: return
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.approuverMembre(membreSolId)
                messageSucces = "Membre approuvé ✅"
                demandesAdhesion = Network.api.demandesAdhesion(sol.id)
                membresDetail = Network.api.membresDuSol(sol.id)
                detailComplet = Network.api.detailDuSol(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    /** Refuse une demande d'adhesion (Manman sol). */
    fun refuserMembre(membreSolId: String) {
        val sol = solDetail ?: return
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.refuserMembre(membreSolId)
                messageSucces = "Demande refusée."
                demandesAdhesion = Network.api.demandesAdhesion(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    fun fermerDetail() { solDetail = null; detailComplet = null }

    // ---------- Chat ----------

    fun ouvrirChatGroupe(sol: SolResponse) {
        messagesChat = emptyList()
        chatCible = ChatCible(estGroupe = true, solId = sol.id, titre = sol.nom)
    }

    fun ouvrirSelecteurPrive() { choixMembrePriveOuvert = true }
    fun fermerSelecteurPrive() { choixMembrePriveOuvert = false }

    fun ouvrirChatPrive(autreId: String, autreNom: String) {
        choixMembrePriveOuvert = false
        messagesChat = emptyList()
        chatCible = ChatCible(estGroupe = false, solId = "", titre = autreNom, autreId = autreId)
    }

    fun fermerChat() {
        chatCible = null
        messagesChat = emptyList()
    }

    /** Recupere les messages de la discussion courante (appelee en boucle, silencieuse). */
    suspend fun rafraichirMessages() {
        val cible = chatCible ?: return
        try {
            messagesChat = if (cible.estGroupe) {
                Network.api.messagesSol(cible.solId)
            } else {
                Network.api.messagesPrives(cible.autreId ?: return)
            }
        } catch (_: Throwable) {
            // Silencieux : on ne perturbe pas l'ecran pendant le rafraichissement.
        }
    }

    fun envoyerMessage(contenu: String) {
        if (contenu.isBlank()) return
        envoiEnCours = true
        viewModelScope.launch {
            envoyerRequete(EnvoyerMessageRequest(contenu.trim()))
            envoiEnCours = false
        }
    }

    /**
     * Televerse une piece jointe (image ou document) puis l'envoie comme message.
     * [type] vaut "IMAGE" ou "DOCUMENT".
     */
    fun envoyerPieceJointe(octets: ByteArray, nomFichier: String, type: String) {
        chatCible ?: return
        envoiEnCours = true
        viewModelScope.launch {
            try {
                val mime = if (type == "IMAGE") "image/*" else "application/octet-stream"
                val corps = octets.toRequestBody(mime.toMediaTypeOrNull())
                val nom = if (nomFichier.isBlank()) "piece" else nomFichier
                val partie = MultipartBody.Part.createFormData("fichier", nom, corps)
                val reponse = Network.api.televerserPhoto(partie)
                val url = reponse["url"]
                    ?: throw IllegalStateException("Le serveur n'a pas renvoyé d'URL.")
                envoyerRequete(EnvoyerMessageRequest("", pieceJointeUrl = url, typePiece = type))
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                envoiEnCours = false
            }
        }
    }

    /** Envoie une requete de message (groupe ou prive) puis rafraichit la liste. */
    private suspend fun envoyerRequete(corps: EnvoyerMessageRequest) {
        val cible = chatCible ?: return
        try {
            if (cible.estGroupe) {
                Network.api.envoyerAuSol(cible.solId, corps)
            } else {
                Network.api.envoyerPrive(cible.autreId ?: return, corps)
            }
            rafraichirMessages()
        } catch (e: Throwable) {
            erreur = messageErreur(e)
        }
    }

    fun validerPaiement(paiementId: String) {
        viewModelScope.launch {
            try {
                Network.api.validerPaiement(paiementId)
                messageSucces = "Paiement validé."
                solDetail?.let { paiementsEnAttente = Network.api.paiementsEnAttente(it.id) }
                chargerCotisations()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    fun rejeterPaiement(paiementId: String) {
        viewModelScope.launch {
            try {
                Network.api.rejeterPaiement(paiementId)
                messageSucces = "Paiement rejeté."
                solDetail?.let { paiementsEnAttente = Network.api.paiementsEnAttente(it.id) }
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    fun ouvrirTour(datePrevue: String) {
        val sol = solDetail ?: return
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.ouvrirTour(sol.id, OuvrirTourRequest(datePrevue))
                messageSucces =
                    "Tour ouvert ! Les cotisations du tour ont été générées pour chaque membre."
                chargerCotisations()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                dialogueTourOuvert = false
                solDetail = null
            }
        }
    }

    fun rejoindre(code: String, onTermine: () -> Unit) {
        if (code.isBlank()) {
            erreur = "Veuillez saisir un code d'invitation."
            dialogueRejoindreOuvert = false
            return
        }
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.rejoindre(RejoindreRequest(code.trim().uppercase()))
                messageSucces =
                    "Demande d'adhésion envoyée ! En attente de l'approbation de la Manman sol. ⏳"
                chargerTout()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                dialogueRejoindreOuvert = false
                onTermine()
            }
        }
    }

    fun creerSol(req: CreerSolRequest) {
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                val sol = Network.api.creerSol(req)
                messageSucces =
                    "Sol « ${sol.nom} » créé ! Code d'invitation : ${sol.codeInvitation}"
                chargerMesSols()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                dialogueCreerOuvert = false
            }
        }
    }

    fun demarrerCycle(solId: String) {
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.demarrerCycle(solId)
                messageSucces = "Le cycle est démarré : plus aucune adhésion possible."
                chargerMesSols()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                solDetail = null
            }
        }
    }

    fun quitterSol(solId: String) {
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                val reponse = Network.api.quitterSol(solId)
                if (reponse.isSuccessful) {
                    messageSucces = "Vous avez quitté le Sol."
                    chargerTout()
                } else {
                    erreur = "Impossible de quitter ce Sol (dette active ou cycle en cours)."
                }
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                solDetail = null
            }
        }
    }

    fun effacerMessages() {
        erreur = null
        messageSucces = null
    }
}

/**
 * Cible d'une discussion ouverte.
 * - Groupe : [estGroupe] vrai, [solId] renseigne.
 * - Prive  : [estGroupe] faux, [autreId] renseigne (l'autre participant).
 */
data class ChatCible(
    val estGroupe: Boolean,
    val solId: String,
    val titre: String,
    val autreId: String? = null,
)

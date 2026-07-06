package com.sol.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.CotisationResponse
import com.sol.app.data.CreerSolRequest
import com.sol.app.data.MembreSolResponse
import com.sol.app.data.Network
import com.sol.app.data.OuvrirTourRequest
import com.sol.app.data.PaiementResponse
import com.sol.app.data.PayerCotisationRequest
import com.sol.app.data.PortefeuilleResponse
import com.sol.app.data.RejoindreRequest
import com.sol.app.data.SolResponse
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch

/**
 * Ecran d'accueil : Sols, cotisations (solde, paiement Mon Cash),
 * validation des paiements et tours (Manman sol).
 */
class HomeViewModel : ViewModel() {

    var sols by mutableStateOf<List<SolResponse>>(emptyList())
        private set
    var cotisations by mutableStateOf<List<CotisationResponse>>(emptyList())
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

    fun chargerTout() {
        chargerMesSols()
        chargerCotisations()
        chargerPortefeuille()
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
        chargementMembres = true
        viewModelScope.launch {
            try {
                membresDetail = Network.api.membresDuSol(sol.id)
                paiementsEnAttente = Network.api.paiementsEnAttente(sol.id)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                chargementMembres = false
            }
        }
    }

    fun fermerDetail() { solDetail = null }

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
                messageSucces = "Vous avez rejoint le Sol avec succès !"
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

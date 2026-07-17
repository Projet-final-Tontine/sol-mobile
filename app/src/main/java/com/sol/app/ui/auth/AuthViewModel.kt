package com.sol.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.ConnexionRequest
import com.sol.app.data.GoogleAuthRequest
import com.sol.app.data.InscriptionRequest
import com.sol.app.data.Network
import com.sol.app.data.Session
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch

/** Gere la connexion et l'inscription. */
class AuthViewModel : ViewModel() {

    var enChargement by mutableStateOf(false)
        private set
    var erreur by mutableStateOf<String?>(null)
        private set

    fun connexion(telephone: String, motDePasse: String, onSucces: () -> Unit) {
        if (telephone.isBlank() || motDePasse.isBlank()) {
            erreur = "Veuillez remplir tous les champs."
            return
        }
        lancer {
            val rep = Network.api.connexion(ConnexionRequest(telephone.trim(), motDePasse))
            Session.token = rep.token
            Session.nomComplet = "${rep.utilisateur.prenom} ${rep.utilisateur.nom}"
            Session.utilisateurId = rep.utilisateur.id
            Session.email = rep.utilisateur.email
            Session.telephone = rep.utilisateur.telephone
            Session.statut = rep.utilisateur.statut
            Session.photoUrl = rep.utilisateur.photoUrl
            onSucces()
        }
    }

    /**
     * Connexion « Continuer avec Google » : envoie le jeton Firebase au backend,
     * qui connecte le compte lié à cet e-mail (ou le crée) et renvoie un JWT.
     * Le stockage de session est identique à la connexion classique.
     */
    fun connexionGoogle(idTokenFirebase: String, onSucces: () -> Unit) {
        lancer {
            val rep = Network.api.connexionGoogle(GoogleAuthRequest(idTokenFirebase))
            Session.token = rep.token
            Session.nomComplet = "${rep.utilisateur.prenom} ${rep.utilisateur.nom}"
            Session.utilisateurId = rep.utilisateur.id
            Session.email = rep.utilisateur.email
            Session.telephone = rep.utilisateur.telephone
            Session.statut = rep.utilisateur.statut
            Session.photoUrl = rep.utilisateur.photoUrl
            onSucces()
        }
    }

    /** Signale une erreur survenue pendant le flux Google (hors appel réseau). */
    fun signalerErreur(message: String) {
        erreur = message
        enChargement = false
    }

    fun inscription(
        req: InscriptionRequest,
        onSucces: () -> Unit,
        onEmailDejaUtilise: (String) -> Unit = {},
    ) {
        if (req.nom.isBlank() || req.prenom.isBlank() || req.telephone.isBlank() ||
            req.email.isBlank() || req.motDePasse.isBlank()
        ) {
            erreur = "Veuillez remplir les champs obligatoires."
            return
        }
        erreur = null
        enChargement = true
        viewModelScope.launch {
            try {
                Network.api.inscription(req)
                enChargement = false
                onSucces()
            } catch (e: Throwable) {
                enChargement = false
                val message = messageErreur(e)
                // Cas particulier : l'e-mail existe deja -> on redirige vers la
                // connexion (avec l'e-mail pre-rempli) au lieu d'afficher l'erreur.
                if (message.contains("email", ignoreCase = true) &&
                    message.contains("utilis", ignoreCase = true)
                ) {
                    onEmailDejaUtilise(req.email.trim())
                } else {
                    erreur = message
                }
            }
        }
    }

    fun effacerErreur() {
        erreur = null
    }

    private fun lancer(bloc: suspend () -> Unit) {
        erreur = null
        enChargement = true
        viewModelScope.launch {
            try {
                bloc()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enChargement = false
            }
        }
    }
}

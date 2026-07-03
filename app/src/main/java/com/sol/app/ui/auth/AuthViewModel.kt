package com.sol.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.ConnexionRequest
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
            onSucces()
        }
    }

    fun inscription(req: InscriptionRequest, onSucces: () -> Unit) {
        if (req.nom.isBlank() || req.prenom.isBlank() || req.telephone.isBlank() ||
            req.email.isBlank() || req.motDePasse.isBlank()
        ) {
            erreur = "Veuillez remplir les champs obligatoires."
            return
        }
        lancer {
            Network.api.inscription(req)
            onSucces()
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

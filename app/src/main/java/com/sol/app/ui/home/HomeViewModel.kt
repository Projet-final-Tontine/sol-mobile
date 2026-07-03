package com.sol.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.Network
import com.sol.app.data.RejoindreRequest
import com.sol.app.data.SolResponse
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch

/** Gere l'ecran d'accueil du membre : liste de ses Sols et adhesion par code. */
class HomeViewModel : ViewModel() {

    var sols by mutableStateOf<List<SolResponse>>(emptyList())
        private set
    var enChargement by mutableStateOf(false)
        private set
    var erreur by mutableStateOf<String?>(null)
        private set
    var messageSucces by mutableStateOf<String?>(null)
        private set

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

    fun rejoindre(code: String, onTermine: () -> Unit) {
        if (code.isBlank()) {
            erreur = "Veuillez saisir un code d'invitation."
            return
        }
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.rejoindre(RejoindreRequest(code.trim().uppercase()))
                messageSucces = "Vous avez rejoint le Sol avec succes !"
                chargerMesSols()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                onTermine()
            }
        }
    }

    fun effacerMessages() {
        erreur = null
        messageSucces = null
    }
}

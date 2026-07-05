package com.sol.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.ChangerMotDePasseRequest
import com.sol.app.data.ModifierProfilRequest
import com.sol.app.data.Network
import com.sol.app.data.Session
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Actions du profil : photo, informations personnelles, mot de passe. */
class ProfilViewModel : ViewModel() {

    var enTraitement by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var erreur by mutableStateOf<String?>(null)
        private set

    var photoUrl by mutableStateOf(Session.photoUrl)
        private set
    var nomComplet by mutableStateOf(Session.nomComplet ?: "Membre")
        private set

    var dialogueInfosOuvert by mutableStateOf(false)
        private set
    var dialogueMotDePasseOuvert by mutableStateOf(false)
        private set

    fun ouvrirDialogueInfos() { dialogueInfosOuvert = true }
    fun fermerDialogueInfos() { dialogueInfosOuvert = false }
    fun ouvrirDialogueMotDePasse() { dialogueMotDePasseOuvert = true }
    fun fermerDialogueMotDePasse() { dialogueMotDePasseOuvert = false }

    /** Televerse la photo puis l'enregistre dans le profil. */
    fun televerserPhoto(octets: ByteArray) {
        lancer {
            val corps = octets.toRequestBody("image/*".toMediaTypeOrNull())
            val partie = MultipartBody.Part.createFormData("fichier", "photo.jpg", corps)
            val reponse = Network.api.televerserPhoto(partie)
            val url = reponse["url"]
                ?: throw IllegalStateException("Le serveur n'a pas renvoyé d'URL.")
            val utilisateur = Network.api.modifierProfil(
                ModifierProfilRequest(nom = null, prenom = null, adresse = null, photoUrl = url)
            )
            Session.photoUrl = utilisateur.photoUrl
            photoUrl = utilisateur.photoUrl
            message = "Photo de profil mise à jour ! 📷"
        }
    }

    fun modifierInfos(nom: String, prenom: String, adresse: String) {
        lancer {
            val utilisateur = Network.api.modifierProfil(
                ModifierProfilRequest(
                    nom = nom.trim().ifBlank { null },
                    prenom = prenom.trim().ifBlank { null },
                    adresse = adresse.trim().ifBlank { null },
                    photoUrl = null,
                )
            )
            Session.nomComplet = "${utilisateur.prenom} ${utilisateur.nom}"
            nomComplet = Session.nomComplet ?: nomComplet
            dialogueInfosOuvert = false
            message = "Informations mises à jour !"
        }
    }

    fun changerMotDePasse(ancien: String, nouveau: String) {
        lancer {
            Network.api.changerMotDePasse(ChangerMotDePasseRequest(ancien, nouveau))
            dialogueMotDePasseOuvert = false
            message = "Mot de passe modifié avec succès ! 🔒"
        }
    }

    fun effacerMessages() {
        message = null
        erreur = null
    }

    private fun lancer(bloc: suspend () -> Unit) {
        erreur = null
        message = null
        enTraitement = true
        viewModelScope.launch {
            try {
                bloc()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enTraitement = false
            }
        }
    }
}

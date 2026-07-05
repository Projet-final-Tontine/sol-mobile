package com.sol.app.data

import android.content.Context
import com.sol.app.App

/** Stocke le jeton JWT et le nom de l'utilisateur connecte (SharedPreferences). */
object Session {

    private val prefs by lazy {
        App.instance.getSharedPreferences("sol_session", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().apply {
            if (value == null) remove("token") else putString("token", value)
        }.apply()

    var nomComplet: String?
        get() = prefs.getString("nom", null)
        set(value) = prefs.edit().putString("nom", value).apply()

    var utilisateurId: String?
        get() = prefs.getString("utilisateur_id", null)
        set(value) = prefs.edit().putString("utilisateur_id", value).apply()

    var email: String?
        get() = prefs.getString("email", null)
        set(value) = prefs.edit().putString("email", value).apply()

    var telephone: String?
        get() = prefs.getString("telephone", null)
        set(value) = prefs.edit().putString("telephone", value).apply()

    /** Statut du compte cote serveur : EN_ATTENTE, ACTIF ou BLOQUE (sert au badge KYC). */
    var statut: String?
        get() = prefs.getString("statut", null)
        set(value) = prefs.edit().putString("statut", value).apply()

    /** URL relative de la photo de profil (ex : /uploads/xxx.jpg). */
    var photoUrl: String?
        get() = prefs.getString("photo_url", null)
        set(value) = prefs.edit().apply {
            if (value == null) remove("photo_url") else putString("photo_url", value)
        }.apply()

    val estConnecte: Boolean
        get() = token != null

    fun deconnecter() {
        prefs.edit().clear().apply()
    }
}

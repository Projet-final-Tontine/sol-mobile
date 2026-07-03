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

    val estConnecte: Boolean
        get() = token != null

    fun deconnecter() {
        prefs.edit().clear().apply()
    }
}

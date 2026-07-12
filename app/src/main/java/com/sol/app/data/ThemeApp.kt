package com.sol.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Thème de l'application : clair, sombre ou automatique (suit le téléphone).
 *
 * [mode] est un état observable par Compose : quand il change, `MainActivity`
 * se recompose et applique aussitôt le nouveau thème, sans redémarrer l'app.
 * Le choix est persisté dans [Session] (conservé au prochain lancement).
 */
object ThemeApp {

    /** Valeurs : "SYSTEME" (automatique), "CLAIR" ou "SOMBRE". */
    var mode by mutableStateOf(Session.themeMode)
        private set

    fun definir(nouveau: String) {
        mode = nouveau
        Session.themeMode = nouveau
    }
}

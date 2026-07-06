package com.sol.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Gestion de la langue de l'interface (Français / Kreyòl ayisyen).
 *
 * `langue` est un état observable par Compose : quand elle change, tous les
 * textes affichés via [tr] se recomposent automatiquement, sans redemarrer
 * l'application. Le choix est persiste dans [Session].
 */
object I18n {
    var langue by mutableStateOf(Session.langue)
        private set

    fun definir(code: String) {
        langue = code
        Session.langue = code
    }
}

/**
 * Renvoie le texte dans la langue courante.
 * Exemple : `tr("Bonjour", "Bonjou")`.
 *
 * Lu pendant la composition, l'acces a [I18n.langue] est observe : changer de
 * langue declenche la recomposition et met a jour l'affichage.
 */
fun tr(fr: String, ht: String): String = if (I18n.langue == "ht") ht else fr

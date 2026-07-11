package com.sol.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sol.app.data.SolDetailResponse
import com.sol.app.data.tr
import kotlinx.coroutines.launch

// ============================================================================
//  ASSISTANT DU SOL  —  Questions/reponses par regles, 100 % hors-ligne.
//
//  Aucune API externe : l'assistant lit directement le detail complet du Sol
//  deja charge (detailComplet) et le solde du portefeuille. Il comprend des
//  questions courantes en francais ET en kreyol grace a une detection par
//  mots-cles (accents ignores). Ideal pour un usage sans connexion fiable.
// ============================================================================

private val FondAssistant = Color(0xF21A1338)
private val BulleAssistant = Color(0xFF2A2152)
private val BulleUtilisateur = Color(0xFF6C4EE8)
private val PucesFond = Color(0xFF241C48)
private val TexteClair = Color(0xFFF6F3FF)
private val TexteDoux = Color(0xFFB7AEDC)
private val AccentAssistant = Color(0xFF8B6CF8)

/** Un echange affiche dans la conversation de l'assistant. */
private data class MessageAssistant(val texte: String, val deMoi: Boolean)

/** Enleve les accents et met en minuscules pour comparer les mots-cles. */
private fun normaliser(texte: String): String =
    java.text.Normalizer.normalize(texte.lowercase(), java.text.Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
        .trim()

/** Formate un montant en gourdes, sans decimales superflues. */
private fun montantHtg(montant: Double): String {
    val f = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
    f.maximumFractionDigits = 0
    return "${f.format(montant)} HTG"
}

/** Date lisible « 12 mars 2026 » a partir d'une date ISO, ou null. */
private fun dateAmicale(iso: String?): String? = try {
    iso?.take(10)?.let {
        val d = java.time.LocalDate.parse(it)
        val mois = listOf(
            "janvier", "février", "mars", "avril", "mai", "juin",
            "juillet", "août", "septembre", "octobre", "novembre", "décembre",
        )
        "${d.dayOfMonth} ${mois[d.monthValue - 1]} ${d.year}"
    }
} catch (_: Throwable) {
    null
}

/**
 * Coeur de l'assistant : renvoie une reponse en langage naturel a partir de la
 * question posee, du detail du Sol et du solde. Tout est calcule localement.
 */
fun repondreAssistant(
    question: String,
    detail: SolDetailResponse?,
    monId: String?,
    solde: Double,
): String {
    val q = normaliser(question)
    fun contient(vararg cles: String) = cles.any { q.contains(normaliser(it)) }

    if (detail == null) {
        return tr(
            "Je n'ai pas encore les informations de ce Sol. Réessayez dans un instant.",
            "Mwen poko gen enfòmasyon Sòl sa a. Eseye ankò nan yon ti moman.",
        )
    }

    // ----- Mon solde -----
    if (contient("mon solde", "solde", "kob mwen", "lajan mwen", "konbyen kob", "mon argent")) {
        return tr(
            "Votre solde disponible est de ${montantHtg(solde)}.",
            "Balans ou disponib se ${montantHtg(solde)}.",
        )
    }

    // ----- Qui n'a pas payé -----
    if (contient(
            "qui n a pas paye", "qui na pas paye", "pas paye", "poko peye", "ki moun ki poko",
            "en retard", "retard", "impaye", "pa peye", "ki poko",
        )
    ) {
        val impayes = detail.etatCotisations.filter { !it.statut.equals("VALIDE", true) }
        if (detail.tourCourant == null) {
            return tr(
                "Aucun tour n'est ouvert pour le moment, il n'y a donc pas de cotisation en cours.",
                "Pa gen okenn tou ki louvri kounye a, kidonk pa gen kotizasyon k ap fèt.",
            )
        }
        return if (impayes.isEmpty()) {
            tr(
                "Tout le monde a payé pour le tour en cours ! ✅",
                "Tout moun peye pou tou k ap fèt la ! ✅",
            )
        } else {
            val noms = impayes.joinToString("\n") { "• ${it.membreNom ?: "Membre"}" }
            tr(
                "${impayes.size} membre(s) n'ont pas encore payé le tour en cours :\n$noms",
                "${impayes.size} manm poko peye tou k ap fèt la :\n$noms",
            )
        }
    }

    // ----- Quand est mon tour -----
    if (contient(
            "mon tour", "quand mon tour", "quand est ce que je recois", "quand je recois",
            "tou pa m", "tou pa mwen", "kile map resevwa", "kile pou mwen", "kile tou",
        )
    ) {
        val pos = detail.maPosition
            ?: return tr(
                "Je ne trouve pas votre position dans la rotation de ce Sol.",
                "Mwen pa jwenn plas ou nan rotasyon Sòl sa a.",
            )
        val date = dateAmicale(pos.datePrevue)
        val precision = if (pos.dateEstimee) tr(" (date estimée)", " (dat estime)") else ""
        return if (date != null) {
            tr(
                "Vous êtes en position ${pos.ordre} sur ${pos.total}. Votre tour est prévu le $date$precision.",
                "Ou nan pozisyon ${pos.ordre} sou ${pos.total}. Tou pa w prevwa pou $date$precision.",
            )
        } else {
            tr(
                "Vous êtes en position ${pos.ordre} sur ${pos.total}. La date de votre tour n'est pas encore fixée.",
                "Ou nan pozisyon ${pos.ordre} sou ${pos.total}. Dat tou pa w poko fikse.",
            )
        }
    }

    // ----- Prochain bénéficiaire -----
    if (contient(
            "prochain beneficiaire", "prochain qui recoit", "qui recoit", "qui va recevoir",
            "pwochen moun", "ki moun kap resevwa", "ki moun ki pral resevwa", "pwochen benefisye",
        )
    ) {
        val prochain = detail.tourCourant
            ?: detail.tours.firstOrNull { !it.statut.equals("CLOTURE", true) }
        return if (prochain?.beneficiaireNom != null) {
            val date = dateAmicale(prochain.datePrevue)?.let { tr(" (prévu le $it)", " (prevwa pou $it)") } ?: ""
            tr(
                "Le prochain bénéficiaire est ${prochain.beneficiaireNom}$date.",
                "Pwochen moun k ap resevwa se ${prochain.beneficiaireNom}$date.",
            )
        } else {
            tr(
                "Aucun tour ouvert : le prochain bénéficiaire sera désigné à l'ouverture du prochain tour.",
                "Pa gen tou louvri : pwochen moun nan ap chwazi lè tou pwochen an louvri.",
            )
        }
    }

    // ----- Combien de tours restants -----
    if (contient(
            "combien de tours", "tours restants", "reste de tours", "reste combien",
            "konbyen tou", "tou ki rete", "konbyen ki rete",
        )
    ) {
        val restants = (detail.totalTours - detail.toursJoues).coerceAtLeast(0)
        return tr(
            "Il reste $restants tour(s) sur ${detail.totalTours}. ${detail.toursJoues} déjà joué(s).",
            "Rete $restants tou sou ${detail.totalTours}. ${detail.toursJoues} deja fèt.",
        )
    }

    // ----- Santé du Sol -----
    if (contient("sante", "sikte", "solidite", "health", "en forme", "bon etat")) {
        val s = detail.sante
            ?: return tr(
                "La santé de ce Sol n'est pas encore calculable (pas assez de cotisations).",
                "Sante Sòl sa a poko ka kalkile (pa gen ase kotizasyon).",
            )
        val niveau = when (s.niveau) {
            "EXCELLENT" -> tr("EXCELLENTE 💎", "TRÈ BON 💎")
            "MOYEN" -> tr("MOYENNE ⚠️", "MWAYEN ⚠️")
            else -> tr("À RISQUE 🔴", "GEN RISK 🔴")
        }
        return tr(
            "La santé du Sol est $niveau (${s.score} % des cotisations réglées à temps).",
            "Sante Sòl la se $niveau (${s.score} % kotizasyon peye alè).",
        )
    }

    // ----- Montant / combien je dois payer -----
    if (contient(
            "combien je dois", "montant", "combien payer", "combien coute", "cotisation",
            "konbyen pou peye", "konbyen pou mwen peye", "montan", "kotizasyon",
        )
    ) {
        val freq = when (detail.sol.frequence.uppercase()) {
            "HEBDOMADAIRE" -> tr(" par semaine", " chak semèn")
            "MENSUELLE" -> tr(" par mois", " chak mwa")
            else -> ""
        }
        return tr(
            "La cotisation de ce Sol est de ${montantHtg(detail.sol.montantCotisation)}$freq.",
            "Kotizasyon Sòl sa a se ${montantHtg(detail.sol.montantCotisation)}$freq.",
        )
    }

    // ----- Combien de membres -----
    if (contient("combien de membres", "combien de personnes", "konbyen moun", "konbyen manm", "nombre de membres")) {
        return tr(
            "Ce Sol compte ${detail.nombreMembres} membre(s) sur ${detail.sol.nombreMaxMembres} places.",
            "Sòl sa a gen ${detail.nombreMembres} manm sou ${detail.sol.nombreMaxMembres} plas.",
        )
    }

    // ----- Salutation -----
    if (contient("bonjour", "bonswa", "bonjou", "salut", "alo", "hello", "koman")) {
        return tr(
            "Bonjour ! Je suis l'assistant de votre Sol. Posez-moi une question ou touchez une suggestion ci-dessous. 👇",
            "Bonjou ! Se mwen ki asistan Sòl ou a. Poze m yon kesyon oswa peze youn nan sijesyon anba yo. 👇",
        )
    }

    // ----- Réponse par défaut (aide) -----
    return tr(
        "Je peux répondre à des questions sur ce Sol. Essayez :\n" +
            "• Qui n'a pas payé ?\n" +
            "• Quand est mon tour ?\n" +
            "• Combien de tours restants ?\n" +
            "• Quelle est la santé du Sol ?\n" +
            "• Quel est mon solde ?\n" +
            "• Quel est le montant de la cotisation ?",
        "Mwen ka reponn kesyon sou Sòl sa a. Eseye :\n" +
            "• Ki moun ki poko peye ?\n" +
            "• Kilè tou pa m ?\n" +
            "• Konbyen tou ki rete ?\n" +
            "• Kijan sante Sòl la ye ?\n" +
            "• Konbyen kòb mwen genyen ?\n" +
            "• Konbyen kotizasyon an ye ?",
    )
}

/**
 * Boite de dialogue de l'assistant : mini-conversation avec bulles, puces de
 * suggestions et champ de saisie. Repond instantanement, sans reseau.
 */
@Composable
fun DialogueAssistant(
    detail: SolDetailResponse?,
    monId: String?,
    solde: Double,
    onFermer: () -> Unit,
) {
    val messages = remember {
        mutableStateListOf(
            MessageAssistant(
                tr(
                    "Bonjour 👋 Je suis l'assistant de votre Sol. Posez-moi une question, ou touchez une suggestion.",
                    "Bonjou 👋 Se mwen ki asistan Sòl ou a. Poze m yon kesyon, oswa peze yon sijesyon.",
                ),
                deMoi = false,
            )
        )
    }
    var saisie by remember { mutableStateOf("") }
    val defilement = rememberScrollState()
    val portee = rememberCoroutineScope()

    fun envoyer(texte: String) {
        val propre = texte.trim()
        if (propre.isEmpty()) return
        messages.add(MessageAssistant(propre, deMoi = true))
        messages.add(MessageAssistant(repondreAssistant(propre, detail, monId, solde), deMoi = false))
        saisie = ""
        portee.launch { defilement.animateScrollTo(defilement.maxValue) }
    }

    val suggestions = listOf(
        tr("Qui n'a pas payé ?", "Ki moun ki poko peye ?"),
        tr("Quand est mon tour ?", "Kilè tou pa m ?"),
        tr("Combien de tours restants ?", "Konbyen tou ki rete ?"),
        tr("Santé du Sol ?", "Sante Sòl la ?"),
        tr("Mon solde ?", "Konbyen kòb mwen ?"),
    )

    Dialog(
        onDismissRequest = onFermer,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(FondAssistant)
                .padding(16.dp),
        ) {
            // En-tete
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentAssistant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✨", fontSize = 20.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tr("Assistant du Sol", "Asistan Sòl la"),
                        color = TexteClair,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        tr("Réponses instantanées, hors-ligne", "Repons rapid, san entènèt"),
                        color = TexteDoux,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onFermer) {
                    Icon(Icons.Default.Close, contentDescription = tr("Fermer", "Fèmen"), tint = TexteDoux)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Conversation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 360.dp)
                    .verticalScroll(defilement),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                messages.forEach { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.deMoi) Arrangement.End else Arrangement.Start,
                    ) {
                        Text(
                            text = message.texte,
                            color = TexteClair,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .widthIn(max = 260.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (message.deMoi) 16.dp else 4.dp,
                                        bottomEnd = if (message.deMoi) 4.dp else 16.dp,
                                    )
                                )
                                .background(if (message.deMoi) BulleUtilisateur else BulleAssistant)
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Puces de suggestions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                suggestions.forEach { suggestion ->
                    Text(
                        text = suggestion,
                        color = TexteClair,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(PucesFond)
                            .clickable { envoyer(suggestion) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Champ de saisie
            OutlinedTextField(
                value = saisie,
                onValueChange = { saisie = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        tr("Posez une question…", "Poze yon kesyon…"),
                        color = TexteDoux,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { envoyer(saisie) }),
                trailingIcon = {
                    IconButton(onClick = { envoyer(saisie) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = tr("Envoyer", "Voye"),
                            tint = AccentAssistant,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TexteClair,
                    unfocusedTextColor = TexteClair,
                    focusedBorderColor = AccentAssistant,
                    unfocusedBorderColor = BulleAssistant,
                    cursorColor = AccentAssistant,
                ),
            )
        }
    }
}

package com.sol.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sol.app.data.SondageResponse
import com.sol.app.data.tr

// Palette alignee sur l'ecran Detail du Sol (cartes sombres premium).
private val SondCarte = Color(0xE6191233)
private val SondInterne = Color(0xFF241C48)
private val SondBordure = Color(0x552C2452)
private val SondViolet = Color(0xFF8B6CF8)
private val SondVioletDoux = Color(0xFFC5B5FF)
private val SondBlanc = Color(0xFFF6F3FF)
private val SondMuet = Color(0xFFA79FC8)
private val SondVert = Color(0xFF35D07F)
private val SondTrack = Color(0xFF39325E)

/**
 * Section « Votes du Sol » : liste des sondages avec resultats, vote en un clic,
 * bouton de creation et cloture. Auto-contenue (le dialogue de creation est ici).
 */
@Composable
fun SectionSondages(vm: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SondCarte)
            .border(1.dp, SondBordure, RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🗳️", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                tr("Votes du Sol", "Vòt Sòl la"),
                color = SondBlanc,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SondViolet)
                    .clickable { vm.ouvrirDialogueSondage() }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(tr("Créer", "Kreye"), color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        if (vm.sondages.isEmpty()) {
            Text(
                tr("Aucun sondage pour l'instant. Lancez le premier vote !",
                    "Poko gen sondaj. Lanse premye vòt la !"),
                color = SondMuet,
                fontSize = 13.sp,
            )
        } else {
            vm.sondages.forEachIndexed { index, sondage ->
                CarteSondage(
                    sondage = sondage,
                    onVoter = { i -> vm.voterSondage(sondage.id, i) },
                    onCloturer = { vm.cloturerSondage(sondage.id) },
                )
                if (index < vm.sondages.lastIndex) Spacer(Modifier.height(12.dp))
            }
        }
    }

    if (vm.dialogueSondageOuvert) {
        DialogueCreerSondage(
            onValider = { question, options -> vm.creerSondage(question, options) },
            onFermer = { vm.fermerDialogueSondage() },
        )
    }
}

/** Une carte de sondage : question + options (barres) + total + cloture. */
@Composable
private fun CarteSondage(
    sondage: SondageResponse,
    onVoter: (Int) -> Unit,
    onCloturer: () -> Unit,
) {
    val ouvert = sondage.statut.equals("OUVERT", true)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SondInterne.copy(alpha = 0.55f))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                sondage.question,
                color = SondBlanc,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background((if (ouvert) SondVert else SondMuet).copy(alpha = 0.22f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    if (ouvert) tr("Ouvert", "Ouvè") else tr("Clos", "Fèmen"),
                    color = if (ouvert) SondVert else SondMuet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        sondage.options.forEach { option ->
            val estMonVote = sondage.monVoteIndex != null && sondage.monVoteIndex == option.index
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SondTrack)
                    .then(if (ouvert) Modifier.clickable { onVoter(option.index) } else Modifier),
            ) {
                // Barre de remplissage proportionnelle au pourcentage.
                Box(
                    modifier = Modifier
                        .fillMaxWidth(option.pourcentage / 100f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            (if (estMonVote) SondViolet else SondViolet.copy(alpha = 0.35f))
                        ),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (estMonVote) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        option.texte,
                        color = SondBlanc,
                        fontSize = 14.sp,
                        fontWeight = if (estMonVote) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${option.pourcentage}% (${option.votes})",
                        color = SondVioletDoux,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${sondage.totalVotes} " + tr("vote(s)", "vòt") +
                    (sondage.createurNom?.let { " · $it" } ?: ""),
                color = SondMuet,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (sondage.peutCloturer) {
                Text(
                    tr("Clore", "Fèmen"),
                    color = SondVioletDoux,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onCloturer() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

/** Dialogue de creation d'un sondage : question + 2 a 6 options. */
@Composable
private fun DialogueCreerSondage(
    onValider: (String, List<String>) -> Unit,
    onFermer: () -> Unit,
) {
    var question by remember { mutableStateOf("") }
    val options = remember { mutableStateListOf("", "") }

    Dialog(onDismissRequest = onFermer, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xF21A1338))
                .padding(18.dp)
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🗳️", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    tr("Nouveau sondage", "Nouvo sondaj"),
                    color = SondBlanc,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = tr("Fermer", "Fèmen"),
                    tint = SondMuet,
                    modifier = Modifier.size(22.dp).clickable { onFermer() },
                )
            }
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text(tr("Question", "Kesyon")) },
                singleLine = false,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = champsCouleurs(),
            )
            Spacer(Modifier.height(12.dp))
            Text(tr("Options", "Chwa yo"), color = SondMuet, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))

            options.forEachIndexed { index, valeur ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp),
                ) {
                    OutlinedTextField(
                        value = valeur,
                        onValueChange = { options[index] = it },
                        label = { Text(tr("Option ", "Chwa ") + "${index + 1}") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f),
                        colors = champsCouleurs(),
                    )
                    if (options.size > 2) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = tr("Retirer", "Retire"),
                            tint = SondMuet,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { options.removeAt(index) },
                        )
                    }
                }
            }

            if (options.size < 6) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { options.add("") }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SondVioletDoux, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(tr("Ajouter une option", "Ajoute yon chwa"), color = SondVioletDoux, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(18.dp))
            val valide = question.isNotBlank() && options.count { it.isNotBlank() } >= 2
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (valide) SondViolet else SondViolet.copy(alpha = 0.4f))
                    .then(
                        if (valide) Modifier.clickable {
                            onValider(question.trim(), options.map { it.trim() }.filter { it.isNotEmpty() })
                        } else Modifier
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(tr("Créer le sondage", "Kreye sondaj la"), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun champsCouleurs() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = SondBlanc,
    unfocusedTextColor = SondBlanc,
    focusedBorderColor = SondViolet,
    unfocusedBorderColor = SondInterne,
    focusedLabelColor = SondVioletDoux,
    unfocusedLabelColor = SondMuet,
    cursorColor = SondViolet,
)

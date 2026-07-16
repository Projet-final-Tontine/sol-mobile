package com.sol.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sol.app.data.DemandeSekouInfo
import com.sol.app.data.tr

/**
 * Section « Fon Sekou » du détail d'un Sol : la caisse de solidarité, les
 * boutons Contribuer / Demander de l'aide, et la liste des demandes de secours
 * avec les votes du groupe.
 */
@Composable
fun SectionFonSekou(vm: HomeViewModel) {
    val fs = vm.fonSekou ?: return

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🛡️", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                tr("Fon Sekou — caisse de solidarité", "Fon Sekou — kès solidarite"),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Spacer(Modifier.height(10.dp))

        // Solde de la caisse + actions.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    tr("Solde de la caisse", "Kòb ki nan kès la"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Text(
                    "${fs.solde.toLong()} HTG",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { vm.ouvrirDialogueContribuer() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text(tr("Contribuer", "Kontribye")) }
                    OutlinedButton(
                        onClick = { vm.ouvrirDialogueDemandeSekou() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                    ) { Text(tr("Demander de l'aide", "Mande èd")) }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (fs.demandes.isEmpty()) {
            Text(
                tr("Aucune demande de secours pour le moment.", "Poko gen okenn demann sekou."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            fs.demandes.forEachIndexed { index, d ->
                CarteDemandeSekou(
                    d = d,
                    estMamanSol = fs.estMamanSol,
                    onVoter = { pour -> vm.voterSekou(d.id, pour) },
                    onCloturer = { vm.cloturerSekou(d.id) },
                )
                if (index < fs.demandes.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (vm.dialogueContribuerOuvert) {
        DialogueMontant(
            titre = tr("Contribuer au Fon Sekou", "Kontribye nan Fon Sekou"),
            libelle = tr("Montant (HTG)", "Montan (HTG)"),
            texteBouton = tr("Contribuer", "Kontribye"),
            onValider = { vm.contribuerSekou(it) },
            onFermer = { vm.fermerDialogueContribuer() },
        )
    }

    if (vm.dialogueDemandeSekouOuvert) {
        DialogueDemandeSekou(
            onValider = { type, montant, motif -> vm.demanderSekou(type, montant, motif) },
            onFermer = { vm.fermerDialogueDemandeSekou() },
        )
    }
}

@Composable
private fun CarteDemandeSekou(
    d: DemandeSekouInfo,
    estMamanSol: Boolean,
    onVoter: (Boolean) -> Unit,
    onCloturer: () -> Unit,
) {
    val enAttente = d.statut == "EN_ATTENTE"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(libelleType(d.type), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                BadgeStatutSekou(d.statut)
            }
            Text(
                d.demandeurNom ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text("${d.montant.toLong()} HTG", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(d.motif, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👍 ${d.nbPour}", fontWeight = FontWeight.SemiBold, color = Color(0xFF1B8A4E))
                Spacer(Modifier.width(14.dp))
                Text("👎 ${d.nbContre}", fontWeight = FontWeight.SemiBold, color = Color(0xFFC62828))
            }

            // Boutons de vote (si en attente et pas ma demande).
            if (enAttente && !d.estMoi) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onVoter(true) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (d.monVote == true) Color(0xFF1B8A4E)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (d.monVote == true) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) { Text(tr("Pour", "Dakò")) }
                    Button(
                        onClick = { onVoter(false) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (d.monVote == false) Color(0xFFC62828)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (d.monVote == false) Color.White
                            else MaterialTheme.colorScheme.onSurface,
                        ),
                    ) { Text(tr("Contre", "Pa dakò")) }
                }
            }

            // Clôture par la Manman sol.
            if (enAttente && estMamanSol) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCloturer,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                ) { Text(tr("Clôturer (verser si accepté)", "Fèmen (bay si dakò)")) }
            }
        }
    }
}

@Composable
private fun BadgeStatutSekou(statut: String) {
    val (texte, couleur) = when (statut) {
        "PAYE" -> tr("Versé ✅", "Peye ✅") to Color(0xFF1B8A4E)
        "REJETE" -> tr("Rejeté ❌", "Rejte ❌") to Color(0xFFC62828)
        else -> tr("En vote ⏳", "Nan vòt ⏳") to Color(0xFFB8860B)
    }
    Box(
        modifier = Modifier
            .background(couleur.copy(alpha = 0.14f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(texte, color = couleur, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun libelleType(type: String): String = when (type) {
    "DECES" -> tr("🕊️ Décès", "🕊️ Lanmò")
    "MALADIE" -> tr("🏥 Maladie", "🏥 Maladi")
    "CATASTROPHE" -> tr("🌀 Catastrophe", "🌀 Katastwòf")
    else -> tr("🆘 Autre", "🆘 Lòt")
}

/** Dialogue générique de saisie d'un montant (contribution). */
@Composable
private fun DialogueMontant(
    titre: String,
    libelle: String,
    texteBouton: String,
    onValider: (Double) -> Unit,
    onFermer: () -> Unit,
) {
    var montant by remember { mutableStateOf("") }
    val valeur = montant.toDoubleOrNull()
    Dialog(onDismissRequest = onFermer, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(titre, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it.filter { c -> c.isDigit() } },
                    label = { Text(libelle) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onFermer) { Text(tr("Annuler", "Anile")) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { valeur?.let { onValider(it) } },
                        enabled = valeur != null && valeur > 0,
                    ) { Text(texteBouton) }
                }
            }
        }
    }
}

/** Dialogue de demande de secours : type, montant, motif. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogueDemandeSekou(
    onValider: (String, Double, String) -> Unit,
    onFermer: () -> Unit,
) {
    var type by remember { mutableStateOf("MALADIE") }
    var montant by remember { mutableStateOf("") }
    var motif by remember { mutableStateOf("") }
    val valeur = montant.toDoubleOrNull()
    val ok = valeur != null && valeur > 0 && motif.isNotBlank()

    Dialog(onDismissRequest = onFermer, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(tr("Demander de l'aide", "Mande èd"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))

                Text(tr("Type de malheur", "Kalite pwoblèm"), style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                val types = listOf("MALADIE", "DECES", "CATASTROPHE", "AUTRE")
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.take(2).forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(libelleType(t)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        types.drop(2).forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(libelleType(t)) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = montant,
                    onValueChange = { montant = it.filter { c -> c.isDigit() } },
                    label = { Text(tr("Montant demandé (HTG)", "Montan w mande (HTG)")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = motif,
                    onValueChange = { motif = it },
                    label = { Text(tr("Décrivez votre situation", "Eksplike sitiyasyon ou")) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onFermer) { Text(tr("Annuler", "Anile")) }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { if (ok) onValider(type, valeur!!, motif.trim()) },
                        enabled = ok,
                    ) { Text(tr("Envoyer", "Voye")) }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sol.app.data.Network
import com.sol.app.data.RechercheUtilisateurResponse
import com.sol.app.data.TransfertRequest
import com.sol.app.data.messageErreur
import com.sol.app.data.tr
import kotlinx.coroutines.launch

private val VERT = Color(0xFF1B8A4E)

/**
 * Section « Envoyer de l'argent » à un autre utilisateur : recherche par
 * username (@...) ou e-mail, confirmation visuelle du bénéficiaire (photo, nom,
 * username, badge KYC), puis transfert. [onTransfertReussi] rafraîchit le solde.
 */
@Composable
fun SectionEnvoyerArgent(onTransfertReussi: () -> Unit, onFermer: (() -> Unit)? = null) {
    val scope = rememberCoroutineScope()

    var requete by remember { mutableStateOf("") }
    var beneficiaire by remember { mutableStateOf<RechercheUtilisateurResponse?>(null) }
    var montant by remember { mutableStateOf("") }
    var enRecherche by remember { mutableStateOf(false) }
    var enEnvoi by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var succes by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tr("Envoyer à un utilisateur", "Voye bay yon itilizatè"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        tr("Par username (@…) ou e-mail", "Pa non itilizatè (@…) oswa imèl"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onFermer != null) {
                    IconButton(onClick = onFermer) {
                        Icon(Icons.Default.Close, contentDescription = tr("Fermer", "Fèmen"))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = requete,
                onValueChange = { requete = it.trim(); beneficiaire = null; succes = null },
                label = { Text(tr("@username ou e-mail", "@non oswa imèl")) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    erreur = null; succes = null; beneficiaire = null
                    enRecherche = true
                    scope.launch {
                        try {
                            beneficiaire = Network.api.rechercherUtilisateur(requete.trim())
                        } catch (e: Throwable) {
                            erreur = messageErreur(e)
                        } finally {
                            enRecherche = false
                        }
                    }
                },
                enabled = requete.length >= 3 && !enRecherche,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (enRecherche) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(tr("Rechercher", "Chèche"))
                }
            }

            // Carte du bénéficiaire trouvé (confirmation visuelle).
            beneficiaire?.let { b ->
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val url = b.photoUrl
                    if (!url.isNullOrBlank()) {
                        AsyncImage(
                            model = Network.BASE_URL.trimEnd('/') + url,
                            contentDescription = null,
                            modifier = Modifier.size(46.dp).clip(CircleShape),
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(46.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    }
                    Spacer(Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(b.nomComplet ?: "—", fontWeight = FontWeight.SemiBold)
                            if (b.kycVerifie) {
                                Spacer(Modifier.size(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Vérifié",
                                    tint = VERT, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(
                            b.username?.let { "@$it" } ?: "",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = montant,
                    onValueChange = { saisie -> montant = saisie.filter { it.isDigit() }.take(9) },
                    label = { Text(tr("Montant (HTG)", "Montan (HTG)")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        val m = montant.toDoubleOrNull() ?: return@Button
                        erreur = null; succes = null
                        enEnvoi = true
                        scope.launch {
                            try {
                                Network.api.transferer(
                                    TransfertRequest("@" + (b.username ?: ""), m, null)
                                )
                                succes = tr("Envoyé à @${b.username} ✅", "Voye bay @${b.username} ✅")
                                beneficiaire = null; requete = ""; montant = ""
                                onTransfertReussi()
                            } catch (e: Throwable) {
                                erreur = messageErreur(e)
                            } finally {
                                enEnvoi = false
                            }
                        }
                    },
                    enabled = (montant.toIntOrNull() ?: 0) > 0 && !enEnvoi,
                    colors = ButtonDefaults.buttonColors(containerColor = VERT),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    if (enEnvoi) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    else Text(tr("Envoyer", "Voye"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            erreur?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            succes?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = VERT, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

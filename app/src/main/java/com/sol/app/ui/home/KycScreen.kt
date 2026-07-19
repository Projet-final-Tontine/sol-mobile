package com.sol.app.ui.home

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sol.app.data.MajIdentiteRequest
import com.sol.app.data.Network
import com.sol.app.data.SoumettreKycRequest
import com.sol.app.data.messageErreur
import com.sol.app.data.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val VERT = Color(0xFF1B8A4E)
private val ROUGE = Color(0xFFC62828)

/**
 * Assistant de vérification d'identité (KYC) en 4 étapes :
 * 1) confirmer/corriger son identité, 2) choisir une pièce, 3) téléverser les
 * documents, 4) confirmation. En mode démonstration, la vérification est
 * approuvée automatiquement côté serveur.
 */
@Composable
fun EcranKyc(onFermer: () -> Unit, onTermine: () -> Unit) {
    val contexte = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var etape by remember { mutableStateOf(1) }
    var chargement by remember { mutableStateOf(true) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var envoi by remember { mutableStateOf(false) }

    // Identité (étape 1)
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var dateNaissance by remember { mutableStateOf("") }
    var adresse by remember { mutableStateOf("") }

    // Pièce (étape 2) et documents (étape 3)
    var typeDoc by remember { mutableStateOf<String?>(null) }
    var rectoUrl by remember { mutableStateOf<String?>(null) }
    var versoUrl by remember { mutableStateOf<String?>(null) }
    var cible by remember { mutableStateOf("recto") }
    var uploadEnCours by remember { mutableStateOf<String?>(null) }

    // Résultat (étape 4)
    var statutFinal by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val zone = cible
            uploadEnCours = zone
            scope.launch {
                val url = televerserDocument(contexte, uri)
                uploadEnCours = null
                if (url == null) {
                    erreur = tr("Échec du téléversement. Réessayez.", "Echèk voye a. Reeseye.")
                } else if (zone == "recto") rectoUrl = url else versoUrl = url
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            val e = Network.api.kyc()
            nom = e.nom ?: ""
            prenom = e.prenom ?: ""
            dateNaissance = e.dateNaissance ?: ""
            adresse = e.adresse ?: ""
        } catch (ex: Throwable) {
            erreur = messageErreur(ex)
        } finally {
            chargement = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Barre du haut + progression
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { if (etape > 1 && etape < 4) etape-- else onFermer() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Retour", "Tounen"))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tr("Vérification d'identité", "Verifikasyon idantite"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (etape < 4) {
                    Text(
                        tr("Étape ", "Etap ") + etape + " / 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        erreur?.let {
            Text(it, color = ROUGE, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
        }

        if (chargement) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            when (etape) {
                1 -> {
                    Text(
                        tr("Vérifie que ces informations correspondent à ta pièce d'identité. Tu peux les corriger ici.",
                            "Verifye enfòmasyon sa yo koresponn ak pyès idantite ou. Ou ka korije yo isit la."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    ChampKyc(tr("Nom", "Non"), nom) { nom = it }
                    ChampKyc(tr("Prénom", "Premye non"), prenom) { prenom = it }
                    ChampKyc(tr("Date de naissance (AAAA-MM-JJ)", "Dat nesans (AAAA-MM-JJ)"), dateNaissance) { dateNaissance = it }
                    ChampKyc(tr("Adresse", "Adrès"), adresse) { adresse = it }
                    Spacer(Modifier.height(20.dp))
                    BoutonKyc(
                        texte = tr("Continuer", "Kontinye"),
                        enCours = envoi,
                        actif = nom.isNotBlank() && prenom.isNotBlank(),
                    ) {
                        envoi = true
                        erreur = null
                        scope.launch {
                            try {
                                Network.api.majIdentiteKyc(
                                    MajIdentiteRequest(nom, prenom, dateNaissance.ifBlank { null }, adresse)
                                )
                                etape = 2
                            } catch (ex: Throwable) {
                                erreur = messageErreur(ex)
                            } finally {
                                envoi = false
                            }
                        }
                    }
                }

                2 -> {
                    Text(
                        tr("Choisis la pièce d'identité que tu veux utiliser.",
                            "Chwazi pyès idantite ou vle itilize a."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    CartePiece("🪪", tr("Carte d'identité", "Kat idantite"),
                        tr("Recto et verso", "Devan ak dèyè")) {
                        typeDoc = "CARTE_IDENTITE"; rectoUrl = null; versoUrl = null; etape = 3
                    }
                    Spacer(Modifier.height(12.dp))
                    CartePiece("📘", tr("Passeport", "Paspò"),
                        tr("Page photo", "Paj foto a")) {
                        typeDoc = "PASSEPORT"; rectoUrl = null; versoUrl = null; etape = 3
                    }
                    Spacer(Modifier.height(12.dp))
                    CartePiece("🚗", tr("Permis de conduire", "Pèmi kondwi"),
                        tr("Recto et verso", "Devan ak dèyè")) {
                        typeDoc = "PERMIS"; rectoUrl = null; versoUrl = null; etape = 3
                    }
                }

                3 -> {
                    val besoinVerso = typeDoc == "CARTE_IDENTITE" || typeDoc == "PERMIS"
                    Text(
                        tr("Prends une photo nette de ta pièce.", "Pran yon foto klè pyès ou."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    ZoneDocument(
                        titre = if (besoinVerso) tr("Recto", "Devan") else tr("Page photo", "Paj foto"),
                        rempli = rectoUrl != null,
                        enCours = uploadEnCours == "recto",
                    ) { cible = "recto"; picker.launch("image/*") }

                    if (besoinVerso) {
                        Spacer(Modifier.height(12.dp))
                        ZoneDocument(
                            titre = tr("Verso", "Dèyè"),
                            rempli = versoUrl != null,
                            enCours = uploadEnCours == "verso",
                        ) { cible = "verso"; picker.launch("image/*") }
                    }

                    Spacer(Modifier.height(20.dp))
                    val pret = rectoUrl != null && (!besoinVerso || versoUrl != null)
                    BoutonKyc(texte = tr("Soumettre", "Soumèt"), enCours = envoi, actif = pret) {
                        envoi = true
                        erreur = null
                        scope.launch {
                            try {
                                val r = Network.api.soumettreKyc(
                                    SoumettreKycRequest(typeDoc ?: "CARTE_IDENTITE", rectoUrl!!, versoUrl)
                                )
                                statutFinal = r.statut
                                etape = 4
                            } catch (ex: Throwable) {
                                erreur = messageErreur(ex)
                            } finally {
                                envoi = false
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        tr("🔒 Environnement de démonstration — tes documents ne sont pas transmis à un tiers.",
                            "🔒 Anviwònman demonstrasyon — dokiman ou pa voye bay okenn lòt moun."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                else -> EtapeResultat(statutFinal) {
                    onTermine()
                    onFermer()
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ChampKyc(label: String, valeur: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = valeur,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    )
}

@Composable
private fun CartePiece(emoji: String, titre: String, sous: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titre, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(sous, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ZoneDocument(titre: String, rempli: Boolean, enCours: Boolean, onClick: () -> Unit) {
    val accent = if (rempli) VERT else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !enCours) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                enCours -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                rempli -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VERT)
                else -> Icon(Icons.Default.CameraAlt, contentDescription = null, tint = accent)
            }
            Spacer(Modifier.size(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titre, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        enCours -> tr("Envoi en cours…", "Ap voye…")
                        rempli -> tr("Photo ajoutée ✓", "Foto ajoute ✓")
                        else -> tr("Toucher pour ajouter une photo", "Manyen pou ajoute yon foto")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EtapeResultat(statut: String?, onTerminer: () -> Unit) {
    var verifEnCours by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(2500)
        verifEnCours = false
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (verifEnCours) {
            CircularProgressIndicator()
            Spacer(Modifier.height(18.dp))
            Text(
                tr("Vérification en cours…", "Verifikasyon ap fèt…"),
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            )
            Text(
                tr("Nous analysons tes documents.", "N ap analize dokiman ou yo."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val approuve = statut == "APPROUVE"
            val accent = if (approuve) VERT else MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(accent.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(if (approuve) "✅" else "🔵", fontSize = 46.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                if (approuve) tr("Identité vérifiée !", "Idantite verifye !")
                else tr("Vérification soumise", "Verifikasyon soumèt"),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = accent,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                if (approuve)
                    tr("Ton identité est confirmée. Merci !", "Idantite ou konfime. Mèsi !")
                else
                    tr("Ton dossier est en cours de vérification. Tu seras notifié.",
                        "Dosye ou an ap verifye. N ap fè ou konnen."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onTerminer,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(tr("Terminer", "Fini"), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BoutonKyc(texte: String, enCours: Boolean, actif: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = actif && !enCours,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
    ) {
        if (enCours) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
        } else {
            Text(texte, fontWeight = FontWeight.Bold)
        }
    }
}

/** Téléverse une image et renvoie son URL (ou null en cas d'échec). */
private suspend fun televerserDocument(contexte: Context, uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            val octets = contexte.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext null
            val corps = octets.toRequestBody("image/*".toMediaTypeOrNull())
            val partie = MultipartBody.Part.createFormData("fichier", "kyc.jpg", corps)
            Network.api.televerserPhoto(partie)["url"]
        } catch (e: Throwable) {
            null
        }
    }

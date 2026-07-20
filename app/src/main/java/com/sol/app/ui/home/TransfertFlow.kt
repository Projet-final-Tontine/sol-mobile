package com.sol.app.ui.home

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sol.app.data.FavoriResponse
import com.sol.app.data.Network
import com.sol.app.data.RecuTransfertResponse
import com.sol.app.data.RechercheUtilisateurResponse
import com.sol.app.data.TransfertRequest
import com.sol.app.data.messageErreur
import com.sol.app.data.tr
import kotlinx.coroutines.launch

private val VERT = Color(0xFF1B8A4E)
private val VIOLET = Color(0xFF3A22A8)

/** Les étapes du parcours d'envoi. */
private enum class EtapeTransfert { RECHERCHE, MONTANT, CONFIRMATION, SUCCES }

/**
 * Parcours complet « Transférer de l'argent » (qualité bancaire) :
 * recherche du bénéficiaire → montant/devise/message → confirmation →
 * authentification (empreinte / visage / mot de passe) → animation de succès →
 * reçu numérique (QR, partage, PDF).
 *
 * [onFermer] revient à l'accueil, [onTransfertReussi] rafraîchit le solde.
 */
@Composable
fun EcranEnvoyerArgent(onFermer: () -> Unit, onTransfertReussi: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var etape by remember { mutableStateOf(EtapeTransfert.RECHERCHE) }
    var beneficiaire by remember { mutableStateOf<RechercheUtilisateurResponse?>(null) }
    var montant by remember { mutableStateOf("") }
    var devise by remember { mutableStateOf("HTG") }
    var message by remember { mutableStateOf("") }
    var recu by remember { mutableStateOf<RecuTransfertResponse?>(null) }
    var enEnvoi by remember { mutableStateOf(false) }
    var erreur by remember { mutableStateOf<String?>(null) }

    fun reinitialiser() {
        beneficiaire = null; montant = ""; devise = "HTG"; message = ""
        recu = null; erreur = null; etape = EtapeTransfert.RECHERCHE
    }

    fun lancerTransfert(methodeAuth: String) {
        val b = beneficiaire ?: return
        val m = montant.toDoubleOrNull() ?: return
        erreur = null; enEnvoi = true
        scope.launch {
            try {
                recu = Network.api.transferer(
                    TransfertRequest(
                        beneficiaire = "@" + (b.username ?: ""),
                        montant = m,
                        devise = devise,
                        message = message.ifBlank { null },
                        methodeAuth = methodeAuth,
                    )
                )
                onTransfertReussi()
                etape = EtapeTransfert.SUCCES
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enEnvoi = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barre supérieure
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VIOLET)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    when (etape) {
                        EtapeTransfert.RECHERCHE -> onFermer()
                        EtapeTransfert.MONTANT -> etape = EtapeTransfert.RECHERCHE
                        EtapeTransfert.CONFIRMATION -> etape = EtapeTransfert.MONTANT
                        EtapeTransfert.SUCCES -> onFermer()
                    }
                }) {
                    Icon(
                        if (etape == EtapeTransfert.SUCCES) Icons.Default.Close
                        else Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = tr("Retour", "Retounen"),
                        tint = Color.White,
                    )
                }
                Text(
                    when (etape) {
                        EtapeTransfert.RECHERCHE -> tr("Envoyer de l'argent", "Voye lajan")
                        EtapeTransfert.MONTANT -> tr("Montant", "Montan")
                        EtapeTransfert.CONFIRMATION -> tr("Confirmation", "Konfimasyon")
                        EtapeTransfert.SUCCES -> tr("Reçu", "Resi")
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when (etape) {
                    EtapeTransfert.RECHERCHE -> EtapeRecherche(
                        onBeneficiaire = { beneficiaire = it; etape = EtapeTransfert.MONTANT },
                    )
                    EtapeTransfert.MONTANT -> beneficiaire?.let { b ->
                        EtapeMontant(
                            beneficiaire = b,
                            montant = montant,
                            devise = devise,
                            message = message,
                            onMontant = { montant = it },
                            onDevise = { devise = it },
                            onMessage = { message = it },
                            onContinuer = { etape = EtapeTransfert.CONFIRMATION },
                        )
                    }
                    EtapeTransfert.CONFIRMATION -> beneficiaire?.let { b ->
                        EtapeConfirmation(
                            beneficiaire = b,
                            montant = montant.toDoubleOrNull() ?: 0.0,
                            devise = devise,
                            message = message,
                            enEnvoi = enEnvoi,
                            erreur = erreur,
                            onConfirmer = {
                                val activity = context.activiteFragment()
                                if (activity != null) {
                                    demanderAuthTransfert(
                                        activity,
                                        onSucces = { methode -> lancerTransfert(methode) },
                                    )
                                } else {
                                    lancerTransfert(tr("Mot de passe", "Modpas"))
                                }
                            },
                        )
                    }
                    EtapeTransfert.SUCCES -> recu?.let { r ->
                        EtapeSucces(
                            recu = r,
                            onAccueil = onFermer,
                            onNouveau = { reinitialiser() },
                        )
                    }
                }
            }
        }
    }
}

// ============================ Étape 1 : recherche ============================

@Composable
private fun EtapeRecherche(onBeneficiaire: (RechercheUtilisateurResponse) -> Unit) {
    val scope = rememberCoroutineScope()
    var requete by remember { mutableStateOf("") }
    var enRecherche by remember { mutableStateOf(false) }
    var resultat by remember { mutableStateOf<RechercheUtilisateurResponse?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var favoris by remember { mutableStateOf<List<FavoriResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            favoris = Network.api.favoris()
        } catch (_: Throwable) { /* pas bloquant */ }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            tr("À qui voulez-vous envoyer ?", "Ki moun ou vle voye ?"),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            tr("Recherchez par username (@…) ou e-mail", "Chèche pa non itilizatè (@…) oswa imèl"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = requete,
            onValueChange = { requete = it.trim(); resultat = null; erreur = null },
            label = { Text(tr("@username ou e-mail", "@non oswa imèl")) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        Button(
            onClick = {
                erreur = null; resultat = null; enRecherche = true
                scope.launch {
                    try {
                        resultat = Network.api.rechercherUtilisateur(requete.trim())
                    } catch (e: Throwable) {
                        erreur = messageErreur(e)
                    } finally {
                        enRecherche = false
                    }
                }
            },
            enabled = requete.length >= 3 && !enRecherche,
            colors = ButtonDefaults.buttonColors(containerColor = VIOLET),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            if (enRecherche) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            else Text(tr("Rechercher", "Chèche"), color = Color.White, fontWeight = FontWeight.SemiBold)
        }

        erreur?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Résultat de recherche : carte cliquable pour continuer.
        resultat?.let { b ->
            Spacer(Modifier.height(16.dp))
            CarteBeneficiaire(b, onClick = { onBeneficiaire(b) })
        }

        // Favoris (transferts rapides).
        if (favoris.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                tr("Favoris", "Favori"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(10.dp))
            favoris.forEach { f ->
                CarteFavori(f, onClick = {
                    onBeneficiaire(
                        RechercheUtilisateurResponse(
                            id = f.beneficiaireId,
                            username = f.username,
                            nomComplet = f.nomComplet,
                            photoUrl = f.photoUrl,
                            kycVerifie = f.kycVerifie,
                            scoreFiabilite = null,
                        )
                    )
                })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Carte bénéficiaire enrichie : photo, nom, @username, badge KYC, score de fiabilité. */
@Composable
private fun CarteBeneficiaire(b: RechercheUtilisateurResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBenef(b.photoUrl, 52.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            b.nomComplet ?: "—",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (b.kycVerifie) {
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = tr("Vérifié", "Verifye"),
                                tint = VERT, modifier = Modifier.size(17.dp))
                        }
                    }
                    Text(
                        b.username?.let { "@$it" } ?: "",
                        color = VIOLET,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Icon(Icons.Default.Send, contentDescription = null, tint = VIOLET)
            }
            b.scoreFiabilite?.let { score ->
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = couleurScore(score), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        tr("Score de fiabilité : ", "Nòt konfyans : ") + "$score/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = couleurScore(score),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun CarteFavori(f: FavoriResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarBenef(f.photoUrl, 42.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(f.nomComplet ?: "—", fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    if (f.kycVerifie) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null,
                            tint = VERT, modifier = Modifier.size(15.dp))
                    }
                }
                Text(f.username?.let { "@$it" } ?: "", color = VIOLET,
                    style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300),
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AvatarBenef(photoUrl: String?, taille: androidx.compose.ui.unit.Dp) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = Network.BASE_URL.trimEnd('/') + photoUrl,
            contentDescription = null,
            modifier = Modifier.size(taille).clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier.size(taille)
                .background(VIOLET.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Default.Person, contentDescription = null, tint = VIOLET) }
    }
}

// ============================ Étape 2 : montant ============================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EtapeMontant(
    beneficiaire: RechercheUtilisateurResponse,
    montant: String,
    devise: String,
    message: String,
    onMontant: (String) -> Unit,
    onDevise: (String) -> Unit,
    onMessage: (String) -> Unit,
    onContinuer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        // Rappel du destinataire
        Row(verticalAlignment = Alignment.CenterVertically) {
            AvatarBenef(beneficiaire.photoUrl, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(beneficiaire.nomComplet ?: "—", fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text(beneficiaire.username?.let { "@$it" } ?: "", color = VIOLET,
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(24.dp))

        Text(tr("Montant à envoyer", "Montan pou voye"),
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = montant,
            onValueChange = { saisie -> onMontant(saisie.filter { it.isDigit() }.take(9)) },
            label = { Text(tr("Montant", "Montan")) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))

        // Sélecteur de devise
        Text(tr("Devise", "Deviz"), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row {
            listOf("HTG", "USD").forEach { d ->
                FilterChip(
                    selected = devise == d,
                    onClick = { onDevise(d) },
                    label = { Text(d) },
                )
                Spacer(Modifier.width(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = message,
            onValueChange = { onMessage(it.take(140)) },
            label = { Text(tr("Message (facultatif)", "Mesaj (opsyonèl)")) },
            singleLine = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onContinuer,
            enabled = (montant.toIntOrNull() ?: 0) > 0,
            colors = ButtonDefaults.buttonColors(containerColor = VIOLET),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(tr("Continuer", "Kontinye"), color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ========================= Étape 3 : confirmation =========================

@Composable
private fun EtapeConfirmation(
    beneficiaire: RechercheUtilisateurResponse,
    montant: Double,
    devise: String,
    message: String,
    enEnvoi: Boolean,
    erreur: String?,
    onConfirmer: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            "%,.2f %s".format(montant, devise),
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = VIOLET,
        )
        Spacer(Modifier.height(4.dp))
        Text(tr("sera envoyé à", "pral voye bay"),
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarBenef(beneficiaire.photoUrl, 46.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(beneficiaire.nomComplet ?: "—", fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface)
                            if (beneficiaire.kycVerifie) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null,
                                    tint = VERT, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(beneficiaire.username?.let { "@$it" } ?: "", color = VIOLET,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (message.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    LigneRecap(tr("Message", "Mesaj"), message)
                }
                Spacer(Modifier.height(8.dp))
                LigneRecap(tr("Frais", "Frè"), "0.00 $devise")
                LigneRecap(tr("Total débité", "Total debite"), "%,.2f %s".format(montant, devise))
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Fingerprint, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                tr("Confirmation sécurisée requise", "Konfimasyon sekirize obligatwa"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        erreur?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onConfirmer,
            enabled = !enEnvoi,
            colors = ButtonDefaults.buttonColors(containerColor = VERT),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(54.dp),
        ) {
            if (enEnvoi) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            else {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(tr("Confirmer et envoyer", "Konfime epi voye"),
                    color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LigneRecap(label: String, valeur: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Text(valeur, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall)
    }
}

// ===================== Étape 4 : succès + reçu numérique =====================

@Composable
private fun EtapeSucces(
    recu: RecuTransfertResponse,
    onAccueil: () -> Unit,
    onNouveau: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val echelle = remember { Animatable(0f) }
    LaunchedEffect(Unit) { echelle.animateTo(1f, animationSpec = tween(500)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        // Coche verte animée
        Box(
            modifier = Modifier
                .size((96 * echelle.value).dp)
                .background(VERT, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White,
                modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(tr("Transfert réussi !", "Transfè reyisi !"),
            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VERT)
        Text(
            "%,.2f %s".format(recu.montant, recu.devise),
            fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            tr("envoyé à ", "voye bay ") + (recu.beneficiaireUsername?.let { "@$it" }
                ?: recu.beneficiaireNom ?: "—"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(20.dp))

        // Reçu numérique
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(tr("Reçu numérique", "Resi nimerik"),
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(12.dp))

                // QR code de vérification
                val urlVerif = recu.urlVerification
                    ?: (Network.BASE_URL.trimEnd('/') + "/api/transferts/verifier/" + recu.reference)
                val qr = remember(urlVerif) { genererQrCode(urlVerif, 320) }
                Image(
                    bitmap = qr.asImageBitmap(),
                    contentDescription = tr("QR de vérification", "QR verifikasyon"),
                    modifier = Modifier.size(160.dp),
                )
                Spacer(Modifier.height(14.dp))

                LigneRecu(tr("N° de confirmation", "Nº konfimasyon"), recu.reference)
                LigneRecu(tr("ID transaction", "ID tranzaksyon"), recu.transactionId)
                LigneRecu(tr("Date", "Dat"), recu.date ?: "—")
                LigneRecu(tr("Statut", "Estati"), statutLisible(recu.statut))
                LigneRecu(tr("Solde restant", "Balans ki rete"),
                    "%,.2f %s".format(recu.soldeRestant, recu.devise))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Boutons : partager, PDF
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = {
                    val uri = enregistrerPdfRecu(context, recu, genererQrCode(
                        recu.urlVerification
                            ?: (Network.BASE_URL.trimEnd('/') + "/api/transferts/verifier/" + recu.reference),
                        320,
                    ))
                    if (uri != null) partagerFichier(context, uri)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Partager", "Pataje"))
            }
            Spacer(Modifier.width(10.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        val uri = enregistrerPdfRecu(context, recu, genererQrCode(
                            recu.urlVerification
                                ?: (Network.BASE_URL.trimEnd('/') + "/api/transferts/verifier/" + recu.reference),
                            320,
                        ))
                        if (uri != null) ouvrirFichier(context, uri)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("PDF")
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onNouveau,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Nouveau", "Nouvo"))
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onAccueil,
                colors = ButtonDefaults.buttonColors(containerColor = VIOLET),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Accueil", "Akèy"), color = Color.White)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun LigneRecu(label: String, valeur: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Text(valeur, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall)
    }
}

// ============================== Utilitaires ==============================

/**
 * Retrouve la [FragmentActivity] hôte en remontant la chaîne des ContextWrapper.
 * Nécessaire car, à l'intérieur d'une fenêtre Dialog, LocalContext n'est pas
 * directement l'activité (BiometricPrompt en a pourtant besoin).
 */
internal fun Context.activiteFragment(): FragmentActivity? {
    var courant: Context? = this
    while (courant is android.content.ContextWrapper) {
        if (courant is FragmentActivity) return courant
        courant = courant.baseContext
    }
    return null
}

/** Couleur d'un score de fiabilité (0-100). */
internal fun couleurScore(score: Int): Color = when {
    score >= 80 -> VERT
    score >= 50 -> Color(0xFFFB8C00)
    else -> Color(0xFFC62828)
}

/** Libellé lisible d'un statut de transfert. */
internal fun statutLisible(statut: String): String = when (statut.uppercase()) {
    "REUSSI" -> tr("Réussi", "Reyisi")
    "EN_ATTENTE" -> tr("En attente", "Ap tann")
    "ECHEC" -> tr("Échec", "Echwe")
    "ANNULE" -> tr("Annulé", "Anile")
    else -> statut
}

/** Couleur associée au statut d'un transfert. */
internal fun couleurStatut(statut: String): Color = when (statut.uppercase()) {
    "REUSSI" -> VERT
    "EN_ATTENTE" -> Color(0xFFFB8C00)
    "ECHEC", "ANNULE" -> Color(0xFFC62828)
    else -> Color(0xFF6B7280)
}

/**
 * Authentification avant transfert : empreinte digitale en priorité, puis
 * reconnaissance faciale, puis mot de passe (code du téléphone). Renvoie la
 * méthode réellement utilisée pour l'enregistrer dans le reçu.
 */
private fun demanderAuthTransfert(activity: FragmentActivity, onSucces: (String) -> Unit) {
    val gestionnaire = BiometricManager.from(activity)
    val autorises =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        }

    // Aucun moyen sécurisé configuré : on n'empêche pas la démo.
    if (gestionnaire.canAuthenticate(autorises) != BiometricManager.BIOMETRIC_SUCCESS) {
        onSucces(tr("Mot de passe", "Modpas"))
        return
    }

    // Méthode probable : si une biométrie est enrôlée on la privilégie,
    // sinon c'est le code (mot de passe) du téléphone. Déterminé à l'avance
    // pour rester compatible avec toutes les versions d'androidx.biometric.
    val biometrieDisponible = gestionnaire.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS
    val methodePrevue =
        if (biometrieDisponible) methodeBiometrique(activity) else tr("Mot de passe", "Modpas")

    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSucces(methodePrevue)
            }
        },
    )
    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(tr("Confirmer le transfert", "Konfime transfè a"))
        .setSubtitle(tr("Authentifiez-vous pour envoyer", "Otantifye w pou voye"))
        .setAllowedAuthenticators(autorises)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        builder.setNegativeButtonText(tr("Annuler", "Anile"))
    }
    try {
        prompt.authenticate(builder.build())
    } catch (_: Throwable) {
        onSucces(tr("Mot de passe", "Modpas"))
    }
}

/** Détermine la biométrie disponible (empreinte prioritaire, sinon visage). */
private fun methodeBiometrique(context: Context): String {
    val pm = context.packageManager
    return when {
        pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT) ->
            tr("Empreinte digitale", "Anprint dijital")
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && pm.hasSystemFeature(PackageManager.FEATURE_FACE) ->
            tr("Reconnaissance faciale", "Rekonesans figi")
        else -> tr("Biométrie", "Byometri")
    }
}

/** URL publique de vérification d'un reçu à partir de sa référence. */
internal fun urlVerificationTransfert(reference: String): String =
    Network.BASE_URL.trimEnd('/') + "/api/transferts/verifier/" + reference

/** Reconstruit un reçu à partir du détail (pour régénérer le PDF depuis l'historique). */
internal fun com.sol.app.data.TransfertDetailResponse.versRecu(): RecuTransfertResponse =
    RecuTransfertResponse(
        id = id, reference = reference, transactionId = transactionId, statut = statut,
        montant = montant, devise = devise, frais = frais, totalDebite = montant + frais,
        message = message, date = date,
        expediteurNom = expediteurNom, expediteurUsername = expediteurUsername,
        beneficiaireNom = beneficiaireNom, beneficiaireUsername = beneficiaireUsername,
        soldeRestant = 0.0, urlVerification = urlVerificationTransfert(reference),
    )

/** Génère un QR code hors ligne (ZXing). */
internal fun genererQrCode(texte: String, taille: Int): Bitmap {
    val matrice = QRCodeWriter().encode(texte, BarcodeFormat.QR_CODE, taille, taille)
    val bitmap = Bitmap.createBitmap(taille, taille, Bitmap.Config.RGB_565)
    for (x in 0 until taille) {
        for (y in 0 until taille) {
            bitmap.setPixel(
                x, y,
                if (matrice[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE,
            )
        }
    }
    return bitmap
}

/**
 * Construit le reçu PDF et l'enregistre dans Téléchargements (MediaStore,
 * Android 10+). Renvoie l'URI du fichier, ou null si non supporté.
 */
internal fun enregistrerPdfRecu(context: Context, r: RecuTransfertResponse, qr: Bitmap): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

    val largeur = 595
    val hauteur = 842 // A4
    val doc = android.graphics.pdf.PdfDocument()
    val page = doc.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(largeur, hauteur, 1).create())
    val c: Canvas = page.canvas
    val p = Paint(Paint.ANTI_ALIAS_FLAG)

    // En-tête violet
    p.color = android.graphics.Color.parseColor("#3A22A8")
    c.drawRect(0f, 0f, largeur.toFloat(), 90f, p)
    p.color = android.graphics.Color.WHITE
    p.textSize = 24f
    p.isFakeBoldText = true
    c.drawText("SOL EN LIGNE", 40f, 48f, p)
    p.textSize = 13f
    p.isFakeBoldText = false
    c.drawText("Reçu de transfert", 40f, 72f, p)

    // Montant
    p.color = android.graphics.Color.parseColor("#1B8A4E")
    p.textSize = 40f
    p.isFakeBoldText = true
    c.drawText("%,.2f %s".format(r.montant, r.devise), 40f, 160f, p)

    // QR en haut à droite
    c.drawBitmap(Bitmap.createScaledBitmap(qr, 130, 130, false), largeur - 170f, 105f, null)

    // Corps
    p.color = android.graphics.Color.BLACK
    p.isFakeBoldText = false
    p.textSize = 14f
    var y = 250f
    fun ligne(label: String, valeur: String) {
        p.color = android.graphics.Color.parseColor("#6B7280")
        c.drawText(label, 40f, y, p)
        p.color = android.graphics.Color.BLACK
        c.drawText(valeur, 260f, y, p)
        y += 30f
    }
    ligne("Expéditeur", (r.expediteurNom ?: "—") +
        (r.expediteurUsername?.let { "  @$it" } ?: ""))
    ligne("Bénéficiaire", (r.beneficiaireNom ?: "—") +
        (r.beneficiaireUsername?.let { "  @$it" } ?: ""))
    ligne("Frais", "%,.2f %s".format(r.frais, r.devise))
    ligne("Total débité", "%,.2f %s".format(r.totalDebite, r.devise))
    ligne("Date", r.date ?: "—")
    ligne("Statut", r.statut)
    r.message?.takeIf { it.isNotBlank() }?.let { ligne("Message", it) }

    // Pied : référence + vérification
    y += 20f
    p.color = android.graphics.Color.parseColor("#3A22A8")
    p.textSize = 12f
    c.drawText("N° de confirmation : ${r.reference}", 40f, y, p)
    c.drawText("ID transaction : ${r.transactionId}", 40f, y + 20f, p)
    val urlVerif = r.urlVerification
        ?: (Network.BASE_URL.trimEnd('/') + "/api/transferts/verifier/" + r.reference)
    c.drawText("Vérifiez l'authenticité : $urlVerif", 40f, y + 40f, p)
    p.color = android.graphics.Color.parseColor("#9CA3AF")
    c.drawText("Document généré par SOL EN LIGNE — enregistré au Registre Inviolable.", 40f, y + 62f, p)

    doc.finishPage(page)

    val valeurs = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "Recu_${r.reference}.pdf")
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.RELATIVE_PATH, "Download/SolEnLigne")
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valeurs)
    if (uri != null) {
        resolver.openOutputStream(uri)?.use { doc.writeTo(it) }
    }
    doc.close()
    return uri
}

/** Ouvre le sélecteur de partage pour un PDF (URI MediaStore). */
internal fun partagerFichier(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, tr("Partager le reçu", "Pataje resi a"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/** Ouvre le PDF dans une application de lecture. */
internal fun ouvrirFichier(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Throwable) {
        partagerFichier(context, uri)
    }
}

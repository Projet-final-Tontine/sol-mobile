package com.sol.app.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sol.app.R
import com.sol.app.data.I18n
import com.sol.app.data.Network
import com.sol.app.data.Session
import com.sol.app.data.tr

/** Page « Mon Profil » : photo, KYC, informations, securite, documents. */
@Composable
fun ProfilScreen(
    onDeconnexion: () -> Unit,
    vm: ProfilViewModel = viewModel(),
) {
    var confirmerDeconnexion by remember { mutableStateOf(false) }
    var documentOuvert by remember { mutableStateOf<String?>(null) }
    var dialogueLangueOuvert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.chargerFiabilite() }

    val contexte = LocalContext.current
    val choisirPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val octets = contexte.contentResolver.openInputStream(it)?.use { flux ->
                flux.readBytes()
            }
            if (octets != null) vm.televerserPhoto(octets)
        }
    }

    val statut = (Session.statut ?: "EN_ATTENTE").uppercase()
    val estVerifie = statut == "ACTIF"
    val (kycTexte, kycCouleur) = when (statut) {
        "ACTIF" -> tr("🟢 Vérifié", "🟢 Verifye") to Color(0xFF1B8A4E)
        "EN_ATTENTE" -> tr("🟡 Vérification en cours", "🟡 Verifikasyon ap fèt") to Color(0xFFB8860B)
        else -> tr("🔴 Non vérifié", "🔴 Poko verifye") to MaterialTheme.colorScheme.error
    }

    val nomComplet = vm.nomComplet
    val prenom = nomComplet.substringBefore(" ")
    val nomFamille = nomComplet.substringAfter(" ", "")

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC3A22A8), Color(0xE6140A38))
                    )
                ),
        )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // 1. En-tete
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                tr("Mon Profil", "Pwofil mwen"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            if (vm.enTraitement) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Paramètres",
                    tint = Color.White,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Messages de succes / erreur
        vm.message?.let { MessageProfil(it, MaterialTheme.colorScheme.primary) }
        vm.erreur?.let { MessageProfil(it, MaterialTheme.colorScheme.error) }

        // 2. Carte utilisateur
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    val photo = vm.photoUrl
                    if (photo != null) {
                        AsyncImage(
                            model = Network.BASE_URL.trimEnd('/') + photo,
                            contentDescription = "Photo de profil",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { choisirPhoto.launch("image/*") },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = "Modifier la photo",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    nomComplet,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    Session.email ?: "E-mail non renseigné",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    Session.telephone ?: "Téléphone non renseigné",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 2.5 Profil de confiance (differenciateur)
        CarteFiabilite(vm)

        Spacer(Modifier.height(16.dp))

        // 3. Verification d'identite (KYC)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = kycCouleur)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            tr("Vérification d'identité (KYC)", "Verifikasyon idantite (KYC)"),
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(kycTexte, fontWeight = FontWeight.Bold, color = kycCouleur, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    tr(
                        "La vérification de votre identité est requise pour effectuer des dépôts et des retraits.",
                        "Ou dwe verifye idantite ou pou fè depo ak retrè.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 4. Informations personnelles
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("Informations personnelles", "Enfòmasyon pèsonèl"),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { vm.ouvrirDialogueInfos() }) {
                        Text(tr("Modifier", "Modifye"), color = MaterialTheme.colorScheme.primary)
                    }
                }
                LigneInfo(tr("Nom", "Non"), nomFamille.ifBlank { "—" })
                LigneInfo(tr("Prénom", "Premye non"), prenom)
                LigneInfo(tr("Adresse e-mail", "Adrès imèl"), Session.email ?: "—")
                LigneInfo(tr("Téléphone", "Telefòn"), Session.telephone ?: "—")
            }
        }

        Spacer(Modifier.height(16.dp))

        // 5. Securite
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            LigneAction(
                icone = { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                titre = tr("Modifier le mot de passe", "Chanje modpas"),
                onClick = { vm.ouvrirDialogueMotDePasse() },
            )
        }

        Spacer(Modifier.height(12.dp))

        // 5b. Langue de l'application
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            LigneAction(
                icone = { Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary) },
                titre = tr("Langue", "Lang") + " : " +
                    if (I18n.langue == "ht") "Kreyòl ayisyen" else "Français",
                onClick = { dialogueLangueOuvert = true },
            )
        }

        Spacer(Modifier.height(12.dp))

        // 6. Documents legaux
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column {
                LigneAction(
                    icone = { Icon(Icons.Default.Description, null, tint = MaterialTheme.colorScheme.primary) },
                    titre = tr("Conditions d'utilisation", "Kondisyon itilizasyon"),
                    onClick = { documentOuvert = tr("Conditions d'utilisation", "Kondisyon itilizasyon") },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 18.dp))
                LigneAction(
                    icone = { Icon(Icons.Outlined.PrivacyTip, null, tint = MaterialTheme.colorScheme.primary) },
                    titre = tr("Politique de confidentialité", "Politik konfidansyalite"),
                    onClick = { documentOuvert = tr("Politique de confidentialité", "Politik konfidansyalite") },
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // 7. Deconnexion
        Button(
            onClick = { confirmerDeconnexion = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(tr("Se déconnecter", "Dekonekte"), fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
    }
    }

    if (vm.dialogueInfosOuvert) {
        DialogueModifierInfos(
            nomInitial = nomFamille,
            prenomInitial = prenom,
            enTraitement = vm.enTraitement,
            onValider = { n, p, a -> vm.modifierInfos(n, p, a) },
            onAnnuler = { vm.fermerDialogueInfos() },
        )
    }

    if (vm.dialogueMotDePasseOuvert) {
        DialogueChangerMotDePasse(
            enTraitement = vm.enTraitement,
            onValider = { ancien, nouveau -> vm.changerMotDePasse(ancien, nouveau) },
            onAnnuler = { vm.fermerDialogueMotDePasse() },
        )
    }

    if (dialogueLangueOuvert) {
        DialogueLangue(
            langueActuelle = I18n.langue,
            onChoisir = { code ->
                I18n.definir(code)
                dialogueLangueOuvert = false
            },
            onFermer = { dialogueLangueOuvert = false },
        )
    }

    if (confirmerDeconnexion) {
        AlertDialog(
            onDismissRequest = { confirmerDeconnexion = false },
            shape = RoundedCornerShape(20.dp),
            title = { Text(tr("Se déconnecter ?", "Dekonekte ?"), fontWeight = FontWeight.Bold) },
            text = {
                Text(tr(
                    "Vous devrez vous reconnecter pour accéder à vos tontines.",
                    "Ou pral bezwen rekonekte pou ou aksede sòl ou yo.",
                ))
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmerDeconnexion = false
                    Session.deconnecter()
                    onDeconnexion()
                }) {
                    Text(
                        tr("Se déconnecter", "Dekonekte"),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmerDeconnexion = false }) {
                    Text(tr("Annuler", "Anile"))
                }
            },
        )
    }

    documentOuvert?.let { titre ->
        AlertDialog(
            onDismissRequest = { documentOuvert = null },
            shape = RoundedCornerShape(20.dp),
            title = { Text(titre, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Le texte officiel des « $titre » de Tontine Numérique sera publié ici " +
                        "prochainement. En attendant, l'utilisation de l'application vaut " +
                        "acceptation des règles de fonctionnement du Sol présentées lors de " +
                        "l'inscription.",
                )
            },
            confirmButton = {
                TextButton(onClick = { documentOuvert = null }) { Text("Fermer") }
            },
        )
    }
}

/**
 * Carte « Profil de confiance » : le differenciateur de l'application.
 * Elle transforme l'historique de paiement du membre en un score de fiabilite
 * lisible d'un coup d'oeil (couleur + emoji + niveau). C'est la « reputation »
 * numerique qui remplace la confiance orale de la Manman sol traditionnelle.
 */
@Composable
private fun CarteFiabilite(vm: ProfilViewModel) {
    val score = vm.scoreFiabilite
    val couleur = when {
        vm.estNouveau -> Color(0xFF6A4BAF)
        score >= 90 -> Color(0xFF1B8A4E)
        score >= 75 -> Color(0xFF2E7D32)
        score >= 50 -> Color(0xFFB8860B)
        else -> Color(0xFFC62828)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛡️", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    tr("Profil de confiance", "Pwofil konfyans"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge circulaire du score
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(couleur.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (vm.estNouveau) "—" else "$score",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = couleur,
                        )
                        Text("/100", fontSize = 11.sp, color = couleur)
                    }
                }

                Spacer(Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${vm.emojiFiabilite}  ${vm.niveauFiabilite}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = couleur,
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (vm.estNouveau) 0f else score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = couleur,
                        trackColor = couleur.copy(alpha = 0.15f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (vm.estNouveau)
                            "Payez vos premières cotisations pour bâtir votre réputation."
                        else
                            "Basé sur votre ponctualité de paiement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // Sous-statistiques
            Row(modifier = Modifier.fillMaxWidth()) {
                StatFiabilite(tr("Sols rejoints", "Sòl ou antre"), "${vm.nbSols}", Modifier.weight(1f))
                StatFiabilite(tr("Cotisations à jour", "Kotizasyon ajou"), "${vm.cotisationsPayees}", Modifier.weight(1f))
                StatFiabilite(
                    tr("Ponctualité", "Pontyalite"),
                    if (vm.estNouveau) "—" else "$score%",
                    Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatFiabilite(label: String, valeur: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            valeur,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MessageProfil(texte: String, couleur: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = couleur.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        Text(
            texte,
            color = couleur,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DialogueModifierInfos(
    nomInitial: String,
    prenomInitial: String,
    enTraitement: Boolean,
    onValider: (String, String, String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var nom by remember { mutableStateOf(nomInitial) }
    var prenom by remember { mutableStateOf(prenomInitial) }
    var adresse by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Modifier mes informations", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = prenom, onValueChange = { prenom = it },
                    label = { Text("Prénom") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = adresse, onValueChange = { adresse = it },
                    label = { Text("Adresse (facultatif)") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !enTraitement && nom.isNotBlank() && prenom.isNotBlank(),
                onClick = { onValider(nom, prenom, adresse) },
            ) { Text("Enregistrer", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}

@Composable
private fun DialogueChangerMotDePasse(
    enTraitement: Boolean,
    onValider: (String, String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var ancien by remember { mutableStateOf("") }
    var nouveau by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    val valide = ancien.isNotBlank() && nouveau.length >= 8 && nouveau == confirmation
    val transformation =
        if (visible) VisualTransformation.None else PasswordVisualTransformation()

    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Modifier le mot de passe", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = ancien, onValueChange = { ancien = it },
                    label = { Text("Ancien mot de passe") }, singleLine = true,
                    visualTransformation = transformation,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = nouveau, onValueChange = { nouveau = it },
                    label = { Text("Nouveau mot de passe") }, singleLine = true,
                    visualTransformation = transformation,
                    supportingText = { Text("8 caractères minimum.") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = confirmation, onValueChange = { confirmation = it },
                    label = { Text("Confirmer le nouveau mot de passe") }, singleLine = true,
                    visualTransformation = transformation,
                    isError = confirmation.isNotBlank() && confirmation != nouveau,
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                if (visible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valide && !enTraitement,
                onClick = { onValider(ancien, nouveau) },
            ) { Text("Modifier", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}

@Composable
private fun DialogueLangue(
    langueActuelle: String,
    onChoisir: (String) -> Unit,
    onFermer: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onFermer,
        shape = RoundedCornerShape(20.dp),
        title = { Text(tr("Choisir la langue", "Chwazi lang"), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OptionLangue("Français", "fr", langueActuelle, onChoisir)
                OptionLangue("Kreyòl ayisyen", "ht", langueActuelle, onChoisir)
            }
        },
        confirmButton = {
            TextButton(onClick = onFermer) { Text(tr("Fermer", "Fèmen")) }
        },
    )
}

@Composable
private fun OptionLangue(
    libelle: String,
    code: String,
    selection: String,
    onChoisir: (String) -> Unit,
) {
    val estSelectionne = selection == code
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChoisir(code) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            libelle,
            modifier = Modifier.weight(1f),
            fontWeight = if (estSelectionne) FontWeight.Bold else FontWeight.Normal,
        )
        if (estSelectionne) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun LigneInfo(label: String, valeur: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            valeur,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LigneAction(
    icone: @Composable () -> Unit,
    titre: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icone()
        Spacer(Modifier.width(12.dp))
        Text(titre, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

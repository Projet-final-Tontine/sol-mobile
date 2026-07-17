package com.sol.app.ui.home

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sol.app.R
import com.sol.app.data.CotisationResponse
import com.sol.app.data.CreerSolRequest
import com.sol.app.data.MembreSolResponse
import com.sol.app.data.Network
import com.sol.app.data.NotifsMessages
import com.sol.app.data.Rappels
import com.sol.app.data.Session
import com.sol.app.data.SolResponse
import com.sol.app.data.tr
import com.sol.app.ui.theme.SolViolet
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDeconnexion: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    var onglet by rememberSaveable { mutableIntStateOf(0) }

    val contexte = LocalContext.current
    // Demande la permission de notifications (Android 13+) ; on planifie ensuite.
    val lanceurNotif = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { accordee ->
        // Des que la permission est accordee, on verifie tout de suite les echeances.
        if (accordee) Rappels.verifierMaintenant(contexte)
    }

    LaunchedEffect(Unit) {
        vm.chargerTout()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            lanceurNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Programme les rappels de fond + verifie tout de suite les echeances.
        Rappels.planifier(contexte)
        Rappels.verifierMaintenant(contexte)
        // Notifications de nouveaux messages (groupe + privé), meme app fermee.
        NotifsMessages.planifier(contexte)
        NotifsMessages.verifierMaintenant(contexte)
    }

    // « Retour » : ferme le chat / le détail, ou revient à l'Accueil, au lieu
    // de quitter l'application.
    BackHandler(enabled = vm.chatCible != null || vm.solDetail != null || onglet != 0) {
        when {
            vm.chatCible != null -> vm.fermerChat()
            vm.solDetail != null -> vm.fermerDetail()
            else -> onglet = 0
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Chat en plein écran : on masque la barre de navigation du bas.
            if (vm.chatCible == null) {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val elements = listOf(
                    Triple(tr("Accueil", "Akèy"), Icons.Default.Home, 0),
                    Triple(tr("Mes Tontines", "Sòl mwen yo"), Icons.Default.Groups, 1),
                    Triple(tr("Transferts", "Transfè"), Icons.AutoMirrored.Filled.CompareArrows, 2),
                    Triple(tr("Profil", "Pwofil"), Icons.Default.Person, 3),
                )
                elements.forEach { (titre, icone, index) ->
                    NavigationBarItem(
                        selected = onglet == index,
                        onClick = { onglet = index; vm.fermerChat(); vm.fermerDetail() },
                        icon = {
                            if (index == 1 && vm.sols.isNotEmpty()) {
                                BadgedBox(badge = { Badge { Text("${vm.sols.size}") } }) {
                                    Icon(icone, contentDescription = titre)
                                }
                            } else {
                                Icon(icone, contentDescription = titre)
                            }
                        },
                        label = { Text(titre, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                }
            }
            }
        },
        floatingActionButton = {
            if (onglet == 1 && vm.solDetail == null) {
                Column(horizontalAlignment = Alignment.End) {
                    ExtendedFloatingActionButton(
                        text = { Text(tr("Créer un Sol", "Kreye yon Sòl"), fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { vm.ouvrirDialogueCreer() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    ExtendedFloatingActionButton(
                        text = { Text(tr("Rejoindre", "Antre"), fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                        onClick = { vm.ouvrirDialogueRejoindre() },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                    )
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            val chat = vm.chatCible
            val solOuvert = vm.solDetail
            if (chat != null) {
                // Ecran de discussion (groupe ou prive).
                ChatScreen(vm = vm, cible = chat, onFermer = { vm.fermerChat() })
            } else if (solOuvert != null) {
                // Ecran plein « Détail du Sol » : tirer vers le bas pour rafraîchir.
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = vm.rafraichissement,
                    onRefresh = { vm.rafraichirDetail() },
                ) {
                    EcranDetailSol(
                        vm = vm,
                        sol = solOuvert,
                        onFermer = { vm.fermerDetail() },
                        onVoirCalendrier = { vm.fermerDetail(); onglet = 0 },
                    )
                }
            } else {
                // Onglets : tirer vers le bas pour rafraîchir les données.
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = vm.rafraichissement,
                    onRefresh = { vm.rafraichirOnglet(onglet) },
                ) {
                    when (onglet) {
                        0 -> OngletAccueil(
                            vm,
                            onVoirTontines = { onglet = 1 },
                            onVoirActivite = { onglet = 2 },
                        )
                        1 -> OngletTontines(vm)
                        2 -> OngletTransferts(vm)
                        3 -> ProfilScreen(onDeconnexion)
                    }
                }
            }
        }
    }

    if (vm.dialogueRejoindreOuvert) {
        DialogueRejoindre(
            onValider = { code -> vm.rejoindre(code) { } },
            onAnnuler = { vm.fermerDialogueRejoindre() },
        )
    }

    if (vm.dialogueCreerOuvert) {
        DialogueCreerSol(
            onValider = { vm.creerSol(it) },
            onAnnuler = { vm.fermerDialogueCreer() },
        )
    }

    vm.cotisationAPayer?.let { cotisation ->
        DialoguePayerCotisation(
            cotisation = cotisation,
            nomSol = vm.sols.find { it.id == cotisation.solId }?.nom ?: "Sol",
            solde = vm.solde,
            onPayer = { vm.payerCotisationDepuisWallet() },
            onDeposer = {
                vm.annulerPaiement()
                vm.montrerBientot(
                    "Le dépôt sera disponible avec l'intégration de Mon Cash. " +
                        "Cette fonctionnalité arrive bientôt."
                )
            },
            onAnnuler = { vm.annulerPaiement() },
        )
    }

    if (vm.dialogueBientotOuvert) {
        DialogueBientot(texte = vm.texteBientot, onFermer = { vm.fermerBientot() })
    }

    if (vm.choixMembrePriveOuvert) {
        DialogueChoixMembre(
            membres = vm.detailComplet?.membres.orEmpty()
                .filter { it.utilisateurId != Session.utilisateurId },
            onChoisir = { m -> vm.ouvrirChatPrive(m.utilisateurId, m.nom ?: "Membre") },
            onAnnuler = { vm.fermerSelecteurPrive() },
        )
    }

    if (vm.dialogueTourOuvert) {
        DialogueOuvrirTour(
            onValider = { date -> vm.ouvrirTour(date) },
            onAnnuler = { vm.fermerDialogueTour() },
        )
    }

    // Selecteur de moyen de paiement (Depot / Retrait).
    vm.moyensType?.let { type ->
        DialogueMoyensPaiement(
            type = type,
            onBientot = { nom ->
                vm.fermerMoyens()
                vm.montrerBientot(
                    tr(
                        "« $nom » sera bientôt disponible. Le lien de paiement arrive prochainement. 💳",
                        "« $nom » ap disponib byento. Lyen pèman an ap vini talè. 💳",
                    )
                )
            },
            onFiche = { vm.ouvrirFiche() },
            onFermer = { vm.fermerMoyens() },
        )
    }

    if (vm.ficheOuvert) {
        DialogueFichePaie(
            enCours = vm.envoiFicheEnCours,
            onEnvoyer = { octets -> vm.envoyerFichePaie(octets) },
            onFermer = { vm.fermerFiche() },
        )
    }
}

/**
 * Arrière-plan identique à la page de connexion : l'image `welcome_bg` en plein
 * écran, recouverte d'un voile dégradé violet foncé. Réutilisé sur Mes Tontines,
 * Portefeuille et Profil pour une identité visuelle cohérente et premium.
 * Sur ce fond sombre, les titres passent en blanc et les cartes blanches ressortent.
 */
@Composable
private fun FondLogin(content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
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
        content()
    }
}

// ---------- Onglet 1 : ACCUEIL (tableau de bord) ----------

@Composable
private fun OngletAccueil(
    vm: HomeViewModel,
    onVoirTontines: () -> Unit,
    onVoirActivite: () -> Unit,
) {
    FondLogin {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
        Spacer(Modifier.height(16.dp))

        // En-tete pose sur un ruban violet : lisible quel que soit le fond.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF3A22A8), Color(0xFF6C4EE8))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val photoAccueil = Session.photoUrl
            if (!photoAccueil.isNullOrBlank()) {
                AsyncImage(
                    model = Network.BASE_URL.trimEnd('/') + photoAccueil,
                    contentDescription = "Photo de profil",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = SolViolet,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tr("Bonjour,", "Bonjou,"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
                Text(
                    Session.nomComplet ?: "Membre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            BadgedBox(badge = { Badge { Text("1") } }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                )
            }
        }
        } // fin du ruban

        Spacer(Modifier.height(20.dp))

        // Carte solde total — style « carte bancaire » premium.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF6C4EE8), Color(0xFF3A22A8), Color(0xFF241562))
                    )
                )
                .padding(22.dp),
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        tr("Solde Total d'Épargne", "Total Ekonomi ou"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.weight(1f),
                    )
                    Text("💳", fontSize = 22.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "%,.2f HTG".format(vm.soldeTotal),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (vm.cotisations.isEmpty())
                            tr("Vos cotisations apparaîtront ici", "Kotizasyon ou yo ap parèt la")
                        else "${vm.cotisations.size} " + tr("cotisation(s) au total", "kotizasyon antou"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Text(
                        tr("Détails ›", "Detay ›"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.18f))
                            .clickable { onVoirActivite() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Mes tontines actives
        EnTeteSection(tr("Mes Tontines Actives", "Sòl aktif ou yo"), tr("Voir tout", "Wè tout"), onVoirTontines)
        Spacer(Modifier.height(12.dp))

        when {
            vm.enChargement -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            vm.sols.isEmpty() -> CarteVide(
                icone = Icons.Default.Groups,
                message = "Aucune tontine pour le moment.\nRejoignez-en une avec un code d'invitation !",
            )

            else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.sols) { sol -> CarteTontineActive(sol) }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Prochaines echeances : calendrier des cotisations selon la frequence.
        Text(
            tr("Prochaines Échéances", "Pwochen Echeyans"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(12.dp))
        CarteCalendrier(vm)
        Spacer(Modifier.height(12.dp))
        CarteEcheances(vm)

        Spacer(Modifier.height(24.dp))

        // Transactions recentes : les cotisations, payables directement.
        EnTeteSection(tr("Transactions Récentes", "Dènye Tranzaksyon"), tr("Voir tout", "Wè tout")) { }
        Spacer(Modifier.height(12.dp))
        if (vm.cotisations.isEmpty()) {
            CarteVide(
                icone = Icons.Default.Receipt,
                message = "Aucune transaction pour le moment.\nElles apparaîtront quand un tour sera ouvert.",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                vm.cotisations.take(5).forEach { cotisation ->
                    CarteCotisation(
                        cotisation = cotisation,
                        nomSol = vm.sols.find { it.id == cotisation.solId }?.nom ?: "Sol",
                        onPayer = { vm.demanderPaiement(cotisation) },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        } // fin de la colonne defilante
    } // fin du fond
}

/** Petite courbe decorative dans la carte du solde. */
@Composable
private fun CourbeActivite() {
    val couleur = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(width = 90.dp, height = 40.dp)) {
        val points = listOf(0.7f, 0.45f, 0.6f, 0.25f, 0.5f, 0.15f, 0.35f)
        val pas = size.width / (points.size - 1)
        val chemin = Path()
        points.forEachIndexed { i, p ->
            val x = i * pas
            val y = size.height * p
            if (i == 0) chemin.moveTo(x, y) else chemin.lineTo(x, y)
        }
        drawPath(chemin, color = couleur, style = Stroke(width = 5f))
    }
}

@Composable
private fun EnTeteSection(titre: String, action: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Titre blanc : le fond de l'accueil est desormais violet fonce.
        Text(
            titre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        TextButton(onClick = onAction) {
            Text(action, color = Color.White.copy(alpha = 0.9f))
        }
    }
}

@Composable
private fun CarteVide(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icone,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CarteTontineActive(sol: SolResponse) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        sol.nom.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    sol.nom,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "${sol.montantCotisation.toLong()} HTG · ${sol.frequence}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "${sol.nombreMaxMembres} places",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(10.dp))
            BadgeStatut(sol.statut)
        }
    }
}

@Composable
private fun BadgeStatut(statut: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            statut,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

// ---------- Carte des prochaines echeances (calendrier) ----------

private val MOIS_FR = listOf(
    "janv.", "févr.", "mars", "avr.", "mai", "juin",
    "juil.", "août", "sept.", "oct.", "nov.", "déc.",
)

/** Nombre de jours entre aujourd'hui et l'echeance (negatif = en retard). */
private fun joursAvantEcheance(dateEcheance: String?): Long? {
    if (dateEcheance.isNullOrBlank()) return null
    return try {
        val d = java.time.LocalDate.parse(dateEcheance.take(10))
        java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), d)
    } catch (_: Throwable) {
        null
    }
}

private val MOIS_COMPLET_FR = listOf(
    "janvier", "février", "mars", "avril", "mai", "juin",
    "juillet", "août", "septembre", "octobre", "novembre", "décembre",
)

/** Nature d'un jour marque dans le calendrier intelligent. */
private enum class MarqueurJour { COTISATION, DISTRIBUTION, RETARD }

private val VertCotisation = Color(0xFF35D07F)
private val RougeRetardCal = Color(0xFFFF5A6E)

/** Convertit une date ISO en LocalDate, ou null si illisible. */
private fun versDate(iso: String?): java.time.LocalDate? = try {
    iso?.take(10)?.let { java.time.LocalDate.parse(it) }
} catch (_: Throwable) {
    null
}

/**
 * Calendrier intelligent : affiche tout le mois et marque chaque jour selon sa
 * nature — 🟢 jour de cotisation, 🔴 cotisation en retard, 👑 distribution de la
 * main (un membre recoit). Le jour courant est mis en evidence. Fleches pour
 * naviguer d'un mois a l'autre.
 */
@Composable
private fun CarteCalendrier(vm: HomeViewModel) {
    var moisAffiche by remember { mutableStateOf(java.time.YearMonth.now()) }
    val aujourdHui = java.time.LocalDate.now()

    // Jours de distribution (couronne) : dates des tours de tous mes Sols.
    val joursDistribution = vm.mesTours
        .mapNotNull { versDate(it.datePrevue) }
        .filter { java.time.YearMonth.from(it) == moisAffiche }
        .map { it.dayOfMonth }
        .toSet()

    // Jours de retard : cotisation non reglee dont l'echeance est passee.
    val joursRetard = vm.cotisations
        .filter { !it.statut.equals("VALIDE", ignoreCase = true) }
        .mapNotNull { versDate(it.dateEcheance) }
        .filter { java.time.YearMonth.from(it) == moisAffiche && it.isBefore(aujourdHui) }
        .map { it.dayOfMonth }
        .toSet()

    // Jours de cotisation : toute echeance du mois qui n'est pas deja en retard.
    val joursCotisation = vm.cotisations
        .mapNotNull { versDate(it.dateEcheance) }
        .filter { java.time.YearMonth.from(it) == moisAffiche }
        .map { it.dayOfMonth }
        .toSet()

    // Un seul marqueur par jour, par ordre de priorite : couronne > retard > cotisation.
    fun marqueurDe(jour: Int): MarqueurJour? = when {
        joursDistribution.contains(jour) -> MarqueurJour.DISTRIBUTION
        joursRetard.contains(jour) -> MarqueurJour.RETARD
        joursCotisation.contains(jour) -> MarqueurJour.COTISATION
        else -> null
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // En-tete : mois + navigation
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { moisAffiche = moisAffiche.minusMonths(1) }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Mois précédent",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "${MOIS_COMPLET_FR[moisAffiche.monthValue - 1]} ${moisAffiche.year}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                IconButton(onClick = { moisAffiche = moisAffiche.plusMonths(1) }) {
                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Mois suivant",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            // Jours de la semaine (Dimanche -> Samedi)
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Di", "Lu", "Ma", "Me", "Je", "Ve", "Sa").forEach { jour ->
                    Text(
                        jour,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // Grille des jours
            val decalage = moisAffiche.atDay(1).dayOfWeek.value % 7 // Dimanche = 0
            val nbJours = moisAffiche.lengthOfMonth()
            val semaines = (decalage + nbJours + 6) / 7
            for (semaine in 0 until semaines) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (colonne in 0 until 7) {
                        val numeroCase = semaine * 7 + colonne
                        val jour = numeroCase - decalage + 1
                        if (jour in 1..nbJours) {
                            JourCalendrier(
                                jour = jour,
                                estAujourdHui = moisAffiche.atDay(jour) == aujourdHui,
                                marqueur = marqueurDe(jour),
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f).height(40.dp))
                        }
                    }
                }
            }

            // Legende : n'affiche que les reperes reellement presents ce mois-ci.
            val elementsLegende = buildList {
                if (joursCotisation.isNotEmpty()) {
                    add(MarqueurJour.COTISATION to tr("Cotisation", "Kotizasyon"))
                }
                if (joursDistribution.isNotEmpty()) {
                    add(MarqueurJour.DISTRIBUTION to tr("Distribution", "Distribisyon"))
                }
                if (joursRetard.isNotEmpty()) {
                    add(MarqueurJour.RETARD to tr("En retard", "An reta"))
                }
            }
            if (elementsLegende.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    elementsLegende.forEach { (type, libelle) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (type) {
                                MarqueurJour.DISTRIBUTION -> Text("👑", fontSize = 11.sp)
                                MarqueurJour.RETARD -> PointLegende(RougeRetardCal)
                                MarqueurJour.COTISATION -> PointLegende(VertCotisation)
                            }
                            Spacer(Modifier.width(5.dp))
                            Text(
                                libelle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Petit point colore de la legende du calendrier. */
@Composable
private fun PointLegende(couleur: Color) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .background(couleur),
    )
}

@Composable
private fun RowScope.JourCalendrier(jour: Int, estAujourdHui: Boolean, marqueur: MarqueurJour?) {
    val couleurTexte = when {
        estAujourdHui -> Color.White
        marqueur == MarqueurJour.RETARD -> RougeRetardCal
        marqueur == MarqueurJour.COTISATION -> VertCotisation
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(
                    if (estAujourdHui) Modifier.background(MaterialTheme.colorScheme.primary)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "$jour",
                color = couleurTexte,
                fontWeight = if (estAujourdHui || marqueur != null) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        // Repere sous le numero : couronne pour une distribution, point sinon.
        if (marqueur != null && !estAujourdHui) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (marqueur) {
                    MarqueurJour.DISTRIBUTION -> Text("👑", fontSize = 9.sp)
                    MarqueurJour.RETARD -> PointCalendrier(RougeRetardCal)
                    MarqueurJour.COTISATION -> PointCalendrier(VertCotisation)
                }
            }
        } else if (marqueur == MarqueurJour.DISTRIBUTION && estAujourdHui) {
            // Aujourd'hui + distribution : couronne posee en bas du cercle plein.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 2.dp),
            ) {
                Text("👑", fontSize = 9.sp)
            }
        }
    }
}

/** Point colore sous un jour du calendrier (cotisation ou retard). */
@Composable
private fun PointCalendrier(couleur: Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(couleur),
    )
}

/**
 * Carte « Prochaines échéances » : rappelle les cotisations a venir, triees par
 * date, avec un repere visuel (a venir / bientot / en retard). Les dates sont
 * generees par le backend selon la frequence choisie (hebdomadaire, mensuelle).
 */
@Composable
private fun CarteEcheances(vm: HomeViewModel) {
    val echeances = vm.cotisations
        .filter { !it.statut.equals("VALIDE", ignoreCase = true) }
        .sortedBy { it.dateEcheance ?: "9999-12-31" }
        .take(4)

    if (echeances.isEmpty()) {
        CarteVide(
            icone = Icons.Default.DateRange,
            message = "Aucune échéance à venir.\nVous êtes à jour ! 🎉",
        )
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            echeances.forEachIndexed { index, cotisation ->
                LigneEcheance(
                    cotisation = cotisation,
                    nomSol = vm.sols.find { it.id == cotisation.solId }?.nom ?: "Sol",
                    frequence = vm.sols.find { it.id == cotisation.solId }?.frequence ?: "",
                    onPayer = { vm.demanderPaiement(cotisation) },
                )
                if (index < echeances.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun LigneEcheance(
    cotisation: CotisationResponse,
    nomSol: String,
    frequence: String,
    onPayer: () -> Unit,
) {
    val jours = joursAvantEcheance(cotisation.dateEcheance)
    val (libelle, couleur) = when {
        jours == null -> "À planifier" to MaterialTheme.colorScheme.onSurfaceVariant
        jours < 0 -> "En retard" to MaterialTheme.colorScheme.error
        jours == 0L -> "Aujourd'hui" to Color(0xFFB8860B)
        jours <= 7L -> "Dans ${jours} j" to Color(0xFFB8860B)
        else -> "Dans ${jours} j" to MaterialTheme.colorScheme.primary
    }

    val dateLocale = try {
        cotisation.dateEcheance?.take(10)?.let { java.time.LocalDate.parse(it) }
    } catch (_: Throwable) {
        null
    }
    val jour = dateLocale?.dayOfMonth?.toString() ?: "--"
    val mois = dateLocale?.let { MOIS_FR[it.monthValue - 1] } ?: ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPayer() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pastille date (jour + mois court)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(couleur.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(jour, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = couleur)
                Text(mois, fontSize = 10.sp, color = couleur)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nomSol, fontWeight = FontWeight.SemiBold)
            Text(
                "${cotisation.montantAttendu.toLong()} HTG" +
                    if (frequence.isNotBlank()) " · ${frequence.lowercase()}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(couleur.copy(alpha = 0.14f))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(
                libelle,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = couleur,
            )
        }
    }
}

// ---------- Onglet 2 : MES TONTINES ----------

@Composable
private fun OngletTontines(vm: HomeViewModel) {
    FondLogin {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            tr("Mes Tontines", "Sòl mwen yo"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            "${vm.sols.size} " + tr("tontine(s)", "sòl"),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )
        Spacer(Modifier.height(14.dp))

        vm.messageSucces?.let {
            MessageCarte(it, MaterialTheme.colorScheme.primary)
        }
        vm.erreur?.let {
            MessageCarte(it, MaterialTheme.colorScheme.error)
        }

        when {
            vm.enChargement -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            vm.sols.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CarteVide(
                    icone = Icons.Default.Groups,
                    message = "Aucun Sol pour le moment.\nAppuyez sur « Rejoindre » avec un code d'invitation.",
                )
            }

            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.sols) { sol ->
                    CarteSolDetail(sol, onClick = { vm.ouvrirDetail(sol) })
                }
                item { Spacer(Modifier.height(150.dp)) }
            }
        }
    }
    }
}

@Composable
private fun MessageCarte(texte: String, couleur: androidx.compose.ui.graphics.Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = couleur.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
    ) {
        Text(
            texte,
            color = couleur,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CarteSolDetail(sol: SolResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            sol.nom.take(2).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        sol.nom,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                BadgeStatut(sol.statut)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InfoColonne("Cotisation", "${sol.montantCotisation.toLong()} HTG")
                InfoColonne("Fréquence", sol.frequence)
                InfoColonne("Places", "${sol.nombreMaxMembres}")
            }
            Spacer(Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Code : ${sol.codeInvitation}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    tr("Voir le détail", "Wè detay la") + "  ›",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@Composable
private fun InfoColonne(label: String, valeur: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valeur,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ---------- Onglet 3 : PORTEFEUILLE (wallet) ----------

/**
 * Ecran « Portefeuille » : solde disponible, depot in-app (Mon Cash) et
 * historique complet des mouvements. Les cotisations se paient depuis ce solde.
 */
@Composable
private fun OngletTransferts(vm: HomeViewModel) {
    val aPayer = vm.cotisations.filter { !it.statut.equals("VALIDE", ignoreCase = true) }
    val transactions = vm.portefeuille?.transactions ?: emptyList()

    FondLogin {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            tr("Mon Portefeuille", "Pòtfèy mwen"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            tr(
                "Déposez, payez vos cotisations, suivez chaque mouvement.",
                "Depoze, peye kotizasyon ou, swiv chak mouvman.",
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
        )

        Spacer(Modifier.height(16.dp))

        vm.messageSucces?.let { MessageCarte(it, MaterialTheme.colorScheme.primary) }
        vm.erreur?.let { MessageCarte(it, MaterialTheme.colorScheme.error) }

        // Carte du solde + bouton deposer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    tr("Solde disponible", "Balans disponib"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "%,.2f HTG".format(vm.solde),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    BoutonWallet(
                        texte = tr("Déposer", "Depoze"),
                        icone = Icons.Default.Add,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.ouvrirMoyens("DEPOT") },
                    )
                    Spacer(Modifier.width(12.dp))
                    BoutonWallet(
                        texte = tr("Retirer", "Retire"),
                        icone = Icons.AutoMirrored.Filled.CompareArrows,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.ouvrirMoyens("RETRAIT") },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Cotisations a payer (depuis le solde)
        Text(
            tr("Cotisations à payer", "Kotizasyon pou peye") + " (${aPayer.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(12.dp))
        if (aPayer.isEmpty()) {
            CarteVide(
                icone = Icons.Default.Receipt,
                message = "Aucune cotisation à payer.\nVous êtes à jour ! 🎉",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                aPayer.forEach { cotisation ->
                    CarteCotisation(
                        cotisation = cotisation,
                        nomSol = vm.sols.find { it.id == cotisation.solId }?.nom ?: "Sol",
                        onPayer = { vm.demanderPaiement(cotisation) },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Historique des mouvements du portefeuille
        Text(
            tr("Historique des transactions", "Istwa tranzaksyon") + " (${transactions.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Spacer(Modifier.height(12.dp))
        if (transactions.isEmpty()) {
            CarteVide(
                icone = Icons.AutoMirrored.Filled.CompareArrows,
                message = "Aucun mouvement pour le moment.\nCommencez par un dépôt.",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                transactions.forEach { t -> CarteTransaction(t) }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
    }
}

/** Ligne d'un mouvement du portefeuille (depot, cotisation, gain). */
@Composable
private fun CarteTransaction(t: com.sol.app.data.TransactionPortefeuilleResponse) {
    val estCredit = t.sens.equals("CREDIT", ignoreCase = true)
    val couleur = if (estCredit) Color(0xFF1B8A4E) else MaterialTheme.colorScheme.error
    val signe = if (estCredit) "+" else "−"
    val titre = when (t.type.uppercase()) {
        "DEPOT" -> "Dépôt"
        "COTISATION" -> "Cotisation"
        "GAIN_MAIN" -> "Gain de la main"
        "RETRAIT" -> "Retrait"
        else -> t.type
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titre, fontWeight = FontWeight.SemiBold)
                Text(
                    t.description ?: (t.dateCreation?.take(10) ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$signe${t.montant.toLong()} HTG",
                    fontWeight = FontWeight.Bold,
                    color = couleur,
                )
                Text(
                    "Solde : ${t.soldeApres.toLong()} HTG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Bouton d'action du portefeuille (Déposer / Retirer). */
@Composable
private fun BoutonWallet(
    texte: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Icon(icone, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(texte, fontWeight = FontWeight.Bold)
    }
}

/** Dialogue informatif « fonctionnalité bientôt disponible ». */
@Composable
private fun DialogueBientot(texte: String, onFermer: () -> Unit) {
    AlertDialog(
        onDismissRequest = onFermer,
        shape = RoundedCornerShape(20.dp),
        icon = { Text("🚧", fontSize = 28.sp) },
        title = { Text(tr("Bientôt disponible", "Byento disponib"), fontWeight = FontWeight.Bold) },
        text = { Text(texte, textAlign = TextAlign.Center) },
        confirmButton = {
            TextButton(onClick = onFermer) {
                Text(tr("J'ai compris", "Mwen konprann"), fontWeight = FontWeight.Bold)
            }
        },
    )
}

/** Choix du membre avec qui démarrer une discussion privée. */
@Composable
private fun DialogueChoixMembre(
    membres: List<com.sol.app.data.MembreInfo>,
    onChoisir: (com.sol.app.data.MembreInfo) -> Unit,
    onAnnuler: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text(tr("Discuter avec…", "Pale ak…"), fontWeight = FontWeight.Bold) },
        text = {
            if (membres.isEmpty()) {
                Text(tr(
                    "Aucun autre membre pour l'instant.",
                    "Poko gen lòt manm.",
                ))
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    membres.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onChoisir(m) }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!m.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = Network.BASE_URL.trimEnd('/') + m.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(38.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                m.nom ?: "Membre",
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                            )
                            Text("›", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onAnnuler) { Text(tr("Fermer", "Fèmen")) }
        },
    )
}

// ---------- Dialogue : creer un Sol (Manman sol) ----------

@Composable
private fun DialogueCreerSol(
    onValider: (CreerSolRequest) -> Unit,
    onAnnuler: () -> Unit,
) {
    var nom by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var places by remember { mutableStateOf("") }
    var frequence by remember { mutableStateOf("MENSUELLE") }
    var dateDebut by remember { mutableStateOf("") }

    val dateValide = Regex("""\d{4}-\d{2}-\d{2}""").matches(dateDebut)
    val formulaireValide = nom.isNotBlank() &&
        (montant.toDoubleOrNull() ?: 0.0) > 0 &&
        (places.toIntOrNull() ?: 0) >= 2 && dateValide

    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Créer un Sol", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = nom, onValueChange = { nom = it },
                    label = { Text("Nom du Sol") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Description (facultatif)") }, singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = montant, onValueChange = { montant = it },
                    label = { Text("Cotisation (HTG)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = places, onValueChange = { places = it },
                    label = { Text("Nombre de places (min. 2)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Fréquence",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = frequence == "HEBDOMADAIRE",
                        onClick = { frequence = "HEBDOMADAIRE" },
                        label = { Text("Hebdomadaire") },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(10.dp))
                    FilterChip(
                        selected = frequence == "MENSUELLE",
                        onClick = { frequence = "MENSUELLE" },
                        label = { Text("Mensuelle") },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = dateDebut, onValueChange = { dateDebut = it },
                    label = { Text("Date de début (AAAA-MM-JJ)") }, singleLine = true,
                    isError = dateDebut.isNotBlank() && !dateValide,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = formulaireValide,
                onClick = {
                    onValider(
                        CreerSolRequest(
                            nom = nom.trim(),
                            description = description.trim().ifBlank { null },
                            nombreMaxMembres = places.toInt(),
                            montantCotisation = montant.toDouble(),
                            frequence = frequence,
                            dateDebut = dateDebut,
                        )
                    )
                },
            ) { Text("Créer", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}

// ---------- Carte d'une cotisation (transactions recentes) ----------

@Composable
private fun CarteCotisation(
    cotisation: CotisationResponse,
    nomSol: String,
    onPayer: () -> Unit,
) {
    val estValidee = cotisation.statut.equals("VALIDE", ignoreCase = true)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(nomSol, fontWeight = FontWeight.SemiBold)
                Text(
                    "Échéance : ${cotisation.dateEcheance ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${cotisation.montantAttendu.toLong()} HTG",
                    fontWeight = FontWeight.Bold,
                )
                if (estValidee) {
                    Text(
                        tr("✓ Payée", "✓ Peye"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B8A4E),
                    )
                } else {
                    TextButton(
                        onClick = onPayer,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 8.dp, vertical = 0.dp,
                        ),
                    ) {
                        Text(tr("Payer", "Peye"), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------- Dialogue : payer une cotisation depuis le portefeuille ----------

@Composable
private fun DialoguePayerCotisation(
    cotisation: CotisationResponse,
    nomSol: String,
    solde: Double,
    onPayer: () -> Unit,
    onDeposer: () -> Unit,
    onAnnuler: () -> Unit,
) {
    val montant = cotisation.montantAttendu
    val soldeSuffisant = solde >= montant

    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text(tr("Payer ma cotisation", "Peye kotizasyon mwen"), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Sol : $nomSol")
                Spacer(Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(tr("Montant", "Montan"), modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${montant.toLong()} HTG", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(tr("Votre solde", "Balans ou"), modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${solde.toLong()} HTG",
                        fontWeight = FontWeight.Bold,
                        color = if (soldeSuffisant) Color(0xFF1B8A4E)
                                else MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    if (soldeSuffisant)
                        tr(
                            "Le montant sera prélevé sur votre portefeuille.",
                            "Y ap wete montan an nan pòtfèy ou.",
                        )
                    else
                        tr(
                            "Solde insuffisant : déposez d'abord de l'argent sur votre portefeuille.",
                            "Balans pa ase : depoze lajan nan pòtfèy ou anvan.",
                        ),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (soldeSuffisant) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            if (soldeSuffisant) {
                TextButton(onClick = onPayer) {
                    Text(tr("Payer depuis mon solde", "Peye ak balans mwen"), fontWeight = FontWeight.Bold)
                }
            } else {
                TextButton(onClick = onDeposer) {
                    Text(tr("Déposer de l'argent", "Depoze lajan"), fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text(tr("Annuler", "Anile")) }
        },
    )
}

// ---------- Dialogue : ouvrir un tour (Manman sol) ----------

@Composable
private fun DialogueOuvrirTour(
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var date by remember { mutableStateOf("") }
    val dateValide = Regex("""\d{4}-\d{2}-\d{2}""").matches(date)
    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Ouvrir un tour", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "L'ouverture d'un tour génère automatiquement une cotisation pour chaque membre du Sol.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date prévue (AAAA-MM-JJ)") },
                    singleLine = true,
                    isError = date.isNotBlank() && !dateValide,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = dateValide,
                onClick = { onValider(date) },
            ) { Text("Ouvrir le tour", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}

// ---------- Dialogue rejoindre ----------

/**
 * Decode un QR code present dans une image de la galerie (zxing, hors-ligne).
 * Renvoie le texte du QR (le code d'invitation) ou null si rien n'est trouve.
 */
private fun decoderQrDepuisImage(context: android.content.Context, uri: Uri): String? {
    return try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source) { decodeur, _, _ ->
                decodeur.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decodeur.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        val argb = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        val largeur = argb.width
        val hauteur = argb.height
        val pixels = IntArray(largeur * hauteur)
        argb.getPixels(pixels, 0, largeur, 0, 0, largeur, hauteur)
        val source = RGBLuminanceSource(largeur, hauteur, pixels)
        val binaire = BinaryBitmap(HybridBinarizer(source))
        MultiFormatReader().decode(binaire).text
    } catch (_: Throwable) {
        null
    }
}

@Composable
private fun DialogueRejoindre(
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    val contexte = LocalContext.current

    // Scanner de QR code : le contenu du QR est le code d'invitation.
    val lanceurScan = rememberLauncherForActivityResult(ScanContract()) { resultat ->
        resultat.contents?.let { code = it.trim().uppercase() }
    }

    // Import d'un QR code depuis la galerie : on decode l'image choisie.
    val lanceurGalerie = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val texte = decoderQrDepuisImage(contexte, uri)
            if (texte != null) {
                code = texte.trim().uppercase()
            } else {
                Toast.makeText(
                    contexte,
                    tr("Aucun QR code trouvé dans l'image.", "Pa jwenn QR kòd nan foto a."),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text(tr("Rejoindre un Sol", "Antre nan yon Sòl"), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    tr(
                        "Saisissez le code d'invitation, ou scannez le QR code du Sol.",
                        "Ekri kòd envitasyon an, oswa eskane QR kòd Sòl la.",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(tr("Code d'invitation", "Kòd envitasyon")) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                // Separateur "ou"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        tr("  ou  ", "  oswa  "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        lanceurScan.launch(
                            ScanOptions()
                                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                .setPrompt(
                                    tr(
                                        "Placez le QR code du Sol dans le cadre",
                                        "Mete QR kòd Sòl la nan kad la",
                                    )
                                )
                                .setBeepEnabled(false)
                                .setOrientationLocked(false)
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(tr("Scanner un QR code", "Eskane yon QR kòd"), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { lanceurGalerie.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tr("Importer depuis la galerie", "Enpòte depi galri a"),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = code.isNotBlank(), onClick = { onValider(code) }) {
                Text(tr("Rejoindre", "Antre"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text(tr("Annuler", "Anile")) }
        },
    )
}

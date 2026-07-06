package com.sol.app.ui.home

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
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sol.app.R
import com.sol.app.data.CotisationResponse
import com.sol.app.data.CreerSolRequest
import com.sol.app.data.MembreSolResponse
import com.sol.app.data.PaiementResponse
import com.sol.app.data.Session
import com.sol.app.data.SolResponse
import com.sol.app.data.tr
import com.sol.app.ui.theme.SolViolet

@Composable
fun HomeScreen(
    onDeconnexion: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    var onglet by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { vm.chargerTout() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
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
                        onClick = { onglet = index },
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
        },
        floatingActionButton = {
            if (onglet == 1) {
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
            when (onglet) {
                0 -> OngletAccueil(vm, onVoirTontines = { onglet = 1 })
                1 -> OngletTontines(vm)
                2 -> OngletTransferts(vm)
                3 -> ProfilScreen(onDeconnexion)
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

    vm.solDetail?.let { sol ->
        DialogueDetailSol(
            sol = sol,
            membres = vm.membresDetail,
            paiements = vm.paiementsEnAttente,
            enChargement = vm.chargementMembres,
            onDemarrer = { vm.demarrerCycle(sol.id) },
            onOuvrirTour = { vm.ouvrirDialogueTour() },
            onValiderPaiement = { vm.validerPaiement(it) },
            onRejeterPaiement = { vm.rejeterPaiement(it) },
            onQuitter = { vm.quitterSol(sol.id) },
            onFermer = { vm.fermerDetail() },
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

    if (vm.dialogueTourOuvert) {
        DialogueOuvrirTour(
            onValider = { date -> vm.ouvrirTour(date) },
            onAnnuler = { vm.fermerDialogueTour() },
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
private fun OngletAccueil(vm: HomeViewModel, onVoirTontines: () -> Unit) {
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

        // Carte solde total
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    tr("Solde Total d'Épargne", "Total Ekonomi ou"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "%,.2f HTG".format(vm.soldeTotal),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    CourbeActivite()
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (vm.cotisations.isEmpty()) "Vos cotisations apparaîtront ici"
                        else "${vm.cotisations.size} cotisation(s) au total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Détails de l'Activité ›",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
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

/**
 * Calendrier mensuel : affiche tout le mois et marque d'un point les jours
 * ou une cotisation est due (dates cles). Le jour courant est mis en evidence.
 * Fleches pour naviguer d'un mois a l'autre.
 */
@Composable
private fun CarteCalendrier(vm: HomeViewModel) {
    var moisAffiche by remember { mutableStateOf(java.time.YearMonth.now()) }
    val aujourdHui = java.time.LocalDate.now()

    val joursCles = vm.cotisations
        .mapNotNull { c ->
            try {
                c.dateEcheance?.take(10)?.let { java.time.LocalDate.parse(it) }
            } catch (_: Throwable) {
                null
            }
        }
        .filter { java.time.YearMonth.from(it) == moisAffiche }
        .map { it.dayOfMonth }
        .toSet()

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
                                estCle = joursCles.contains(jour),
                            )
                        } else {
                            Box(modifier = Modifier.weight(1f).height(40.dp))
                        }
                    }
                }
            }

            if (joursCles.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        tr("Jour de cotisation", "Jou kotizasyon"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.JourCalendrier(jour: Int, estAujourdHui: Boolean, estCle: Boolean) {
    val couleurTexte = when {
        estAujourdHui -> Color.White
        estCle -> MaterialTheme.colorScheme.primary
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
                fontWeight = if (estAujourdHui || estCle) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (estCle && !estAujourdHui) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
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
                        onClick = {
                            vm.montrerBientot(
                                "Le dépôt d'argent arrive bientôt, avec l'intégration " +
                                    "sécurisée de Mon Cash. 💳"
                            )
                        },
                    )
                    Spacer(Modifier.width(12.dp))
                    BoutonWallet(
                        texte = tr("Retirer", "Retire"),
                        icone = Icons.AutoMirrored.Filled.CompareArrows,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            vm.montrerBientot(
                                "Le retrait d'argent arrive bientôt, avec l'intégration " +
                                    "sécurisée de Mon Cash. 💳"
                            )
                        },
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

// ---------- Dialogue : detail d'un Sol (membres, actions) ----------

@Composable
private fun DialogueDetailSol(
    sol: SolResponse,
    membres: List<MembreSolResponse>,
    paiements: List<PaiementResponse>,
    enChargement: Boolean,
    onDemarrer: () -> Unit,
    onOuvrirTour: () -> Unit,
    onValiderPaiement: (String) -> Unit,
    onRejeterPaiement: (String) -> Unit,
    onQuitter: () -> Unit,
    onFermer: () -> Unit,
) {
    val estMamanSol = sol.mamanSolId == Session.utilisateurId

    AlertDialog(
        onDismissRequest = onFermer,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column {
                Text(sol.nom, fontWeight = FontWeight.Bold)
                BadgeStatut(sol.statut)
            }
        },
        text = {
            Column {
                Text("Cotisation : ${sol.montantCotisation.toLong()} HTG · ${sol.frequence}")
                Text("Places : ${sol.nombreMaxMembres}")
                Text(
                    "Code d'invitation : ${sol.codeInvitation}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(14.dp))
                Text("Membres", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                when {
                    enChargement -> CircularProgressIndicator(
                        modifier = Modifier.size(22.dp), strokeWidth = 2.dp,
                    )
                    membres.isEmpty() -> Text(
                        "Aucun membre pour le moment.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    ) {
                        membres.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "${m.ordrePassage ?: "–"}.",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.width(28.dp),
                                )
                                Text(
                                    m.nomComplet ?: "Membre",
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    m.statutMembre,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // Section Manman sol : paiements a valider.
                if (estMamanSol && paiements.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Paiements à valider (${paiements.size})",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(6.dp))
                    paiements.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${(p.montantPaye ?: 0.0).toLong()} HTG",
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    "Réf : ${p.referenceTransaction ?: "—"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { onValiderPaiement(p.id) }) {
                                Text("✓ Valider", color = Color(0xFF1B8A4E))
                            }
                            TextButton(onClick = { onRejeterPaiement(p.id) }) {
                                Text("✗", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (estMamanSol && sol.statut.equals("OUVERT", ignoreCase = true)) {
                TextButton(onClick = onDemarrer) {
                    Text("Démarrer le cycle", fontWeight = FontWeight.Bold)
                }
            }
            if (estMamanSol && sol.statut.equals("EN_COURS", ignoreCase = true)) {
                TextButton(onClick = onOuvrirTour) {
                    Text("Ouvrir un tour", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onQuitter) {
                    Text("Quitter le Sol", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onFermer) { Text("Fermer") }
            }
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

@Composable
private fun DialogueRejoindre(
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Rejoindre un Sol", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Saisissez le code d'invitation partagé par la Manman Sol.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code d'invitation") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onValider(code) }) {
                Text("Rejoindre", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}

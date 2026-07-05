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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
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
                    Triple("Accueil", Icons.Default.Home, 0),
                    Triple("Mes Tontines", Icons.Default.Groups, 1),
                    Triple("Transferts", Icons.AutoMirrored.Filled.CompareArrows, 2),
                    Triple("Profil", Icons.Default.Person, 3),
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
                        text = { Text("Créer un Sol", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { vm.ouvrirDialogueCreer() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                    )
                    Spacer(Modifier.height(10.dp))
                    ExtendedFloatingActionButton(
                        text = { Text("Rejoindre", fontWeight = FontWeight.SemiBold) },
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
                2 -> OngletAVenir(
                    titre = "Transferts",
                    message = "Les transferts entre membres arrivent bientôt. 🚧",
                )
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
            onValider = { ref -> vm.payerCotisation(ref) },
            onAnnuler = { vm.annulerPaiement() },
        )
    }

    if (vm.dialogueTourOuvert) {
        DialogueOuvrirTour(
            onValider = { date -> vm.ouvrirTour(date) },
            onAnnuler = { vm.fermerDialogueTour() },
        )
    }
}

// ---------- Onglet 1 : ACCUEIL (tableau de bord) ----------

@Composable
private fun OngletAccueil(vm: HomeViewModel, onVoirTontines: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fond decoratif avec vagues violettes.
        Image(
            painter = painterResource(R.drawable.home_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

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
                    "Bonjour,",
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
                    "Solde Total d'Épargne",
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
        EnTeteSection("Mes Tontines Actives", "Voir tout", onVoirTontines)
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

        // Transactions recentes : les cotisations, payables directement.
        EnTeteSection("Transactions Récentes", "Voir tout") { }
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
        // Encre sombre fixe : la zone centrale du fond est toujours claire.
        Text(
            titre,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF221A4E),
        )
        TextButton(onClick = onAction) {
            Text(action, color = SolViolet)
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

// ---------- Onglet 2 : MES TONTINES ----------

@Composable
private fun OngletTontines(vm: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Mes Tontines",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "${vm.sols.size} tontine(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

// ---------- Onglet 3 : TRANSFERTS (a venir) ----------

@Composable
private fun OngletAVenir(titre: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ShowChart,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(14.dp))
        Text(titre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
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
                        "✓ Payée",
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
                        Text("Payer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------- Dialogue : payer une cotisation (Mon Cash) ----------

@Composable
private fun DialoguePayerCotisation(
    cotisation: CotisationResponse,
    nomSol: String,
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var reference by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAnnuler,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Payer ma cotisation", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Sol : $nomSol")
                Text(
                    "Montant : ${cotisation.montantAttendu.toLong()} HTG",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Effectuez le dépôt via Mon Cash, puis saisissez la référence de la transaction comme justificatif.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Référence Mon Cash") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = reference.isNotBlank(),
                onClick = { onValider(reference) },
            ) { Text("Déclarer le paiement", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
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

package com.sol.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sol.app.data.Session
import com.sol.app.data.SolResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDeconnexion: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    var dialogueOuvert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.chargerMesSols() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bonjour, ${Session.nomComplet ?: "Membre"}") },
                actions = {
                    TextButton(onClick = {
                        Session.deconnecter()
                        onDeconnexion()
                    }) { Text("Deconnexion") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Rejoindre un Sol") },
                icon = { Text("+") },
                onClick = { dialogueOuvert = true },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Mes Sols",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))

            vm.messageSucces?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            }
            vm.erreur?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            when {
                vm.enChargement -> Box_Centre { CircularProgressIndicator() }
                vm.sols.isEmpty() -> Box_Centre {
                    Text(
                        "Vous ne participez a aucun Sol pour le moment.\nRejoignez-en un avec un code d'invitation.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.sols) { sol -> CarteSol(sol) }
                }
            }
        }
    }

    if (dialogueOuvert) {
        DialogueRejoindre(
            enCours = false,
            onValider = { code ->
                vm.rejoindre(code) { dialogueOuvert = false }
            },
            onAnnuler = { dialogueOuvert = false },
        )
    }
}

@Composable
private fun Box_Centre(contenu: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { contenu() }
}

@Composable
private fun CarteSol(sol: SolResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(sol.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sol.statut, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(6.dp))
            Text("Cotisation : ${sol.montantCotisation} HTG - ${sol.frequence}")
            Text("Places : ${sol.nombreMaxMembres}")
            Text("Code : ${sol.codeInvitation}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DialogueRejoindre(
    enCours: Boolean,
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Rejoindre un Sol") },
        text = {
            Column {
                Text("Saisissez le code d'invitation partage par la Manman sol.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code d'invitation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onValider(code) }, enabled = !enCours) { Text("Rejoindre") }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}

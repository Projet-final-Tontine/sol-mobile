package com.sol.app.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sol.app.data.Network
import com.sol.app.data.TransfertDetailResponse
import com.sol.app.data.TransfertHistoriqueItem
import com.sol.app.data.messageErreur
import com.sol.app.data.tr
import kotlinx.coroutines.launch

private val VIOLET_H = Color(0xFF3A22A8)
private val VERT_H = Color(0xFF1B8A4E)

/** Un filtre disponible : (code envoyé au backend, libellé affiché). */
private data class FiltreTransfert(val code: String, val libelle: String)

/**
 * Historique des transferts : liste de cartes (sens ↑/↓, montant coloré,
 * statut), filtres (Tous / Envoyés / Reçus / période), recherche, et ouverture
 * du détail d'une transaction. [onFermer] revient à l'écran précédent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoriqueTransfertsScreen(onFermer: () -> Unit) {
    val scope = rememberCoroutineScope()

    val filtres = listOf(
        FiltreTransfert("TOUS", tr("Tous", "Tout")),
        FiltreTransfert("ENVOYES", tr("Envoyés", "Voye")),
        FiltreTransfert("RECUS", tr("Reçus", "Resevwa")),
        FiltreTransfert("AUJOURDHUI", tr("Aujourd'hui", "Jodi a")),
        FiltreTransfert("SEMAINE", tr("Semaine", "Semèn")),
        FiltreTransfert("MOIS", tr("Mois", "Mwa")),
    )

    var filtreActif by remember { mutableStateOf("TOUS") }
    var recherche by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<TransfertHistoriqueItem>>(emptyList()) }
    var enChargement by remember { mutableStateOf(true) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var detailId by remember { mutableStateOf<String?>(null) }

    fun charger() {
        enChargement = true; erreur = null
        scope.launch {
            try {
                items = Network.api.historiqueTransferts(
                    filtre = filtreActif,
                    recherche = recherche.ifBlank { null },
                )
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enChargement = false
            }
        }
    }

    LaunchedEffect(filtreActif, recherche) { charger() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Barre supérieure
            Row(
                modifier = Modifier.fillMaxWidth().background(VIOLET_H)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onFermer) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("Retour", "Retounen"),
                        tint = Color.White)
                }
                Text(tr("Historique des transferts", "Istwa transfè"),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = recherche,
                    onValueChange = { recherche = it },
                    label = { Text(tr("Rechercher (nom, @username, référence)", "Chèche")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    filtres.forEach { f ->
                        FilterChip(
                            selected = filtreActif == f.code,
                            onClick = { filtreActif = f.code },
                            label = { Text(f.libelle) },
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            when {
                enChargement -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VIOLET_H)
                }
                erreur != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(erreur!!, color = MaterialTheme.colorScheme.error)
                }
                items.isEmpty() -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(tr("Aucun transfert pour ce filtre.", "Pa gen transfè pou filtè sa a."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        CarteHistorique(item, onClick = { detailId = item.id })
                    }
                }
            }
        }
    }

    detailId?.let { id ->
        DetailTransfertScreen(transfertId = id, onFermer = { detailId = null })
    }
}

@Composable
private fun CarteHistorique(item: TransfertHistoriqueItem, onClick: () -> Unit) {
    val estEnvoye = item.sens.equals("ENVOYE", ignoreCase = true)
    val couleurMontant = if (estEnvoye) MaterialTheme.colorScheme.error else VERT_H
    val signe = if (estEnvoye) "−" else "+"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar + pastille de sens
            Box {
                if (!item.autrePhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = Network.BASE_URL.trimEnd('/') + item.autrePhotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(46.dp).clip(CircleShape),
                    )
                } else {
                    Box(
                        modifier = Modifier.size(46.dp)
                            .background(VIOLET_H.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Default.Person, contentDescription = null, tint = VIOLET_H) }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(18.dp)
                        .background(couleurMontant, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (estEnvoye) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.autreNom ?: item.autreUsername?.let { "@$it" } ?: "—",
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    (if (estEnvoye) tr("À ", "Bay ") else tr("De ", "Soti ")) +
                        (item.autreUsername?.let { "@$it" } ?: ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                item.date?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$signe%,.2f %s".format(item.montant, item.devise),
                    fontWeight = FontWeight.Bold, color = couleurMontant)
                Text(statutLisible(item.statut), style = MaterialTheme.typography.bodySmall,
                    color = couleurStatut(item.statut), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Détail complet d'un transfert (plein écran superposé). */
@Composable
fun DetailTransfertScreen(transfertId: String, onFermer: () -> Unit) {
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<TransfertDetailResponse?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(transfertId) {
        scope.launch {
            try {
                detail = Network.api.detailTransfert(transfertId)
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(VIOLET_H)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onFermer) {
                    Icon(Icons.Default.Close, contentDescription = tr("Fermer", "Fèmen"), tint = Color.White)
                }
                Text(tr("Détail du transfert", "Detay transfè a"),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            when {
                erreur != null -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(erreur!!, color = MaterialTheme.colorScheme.error)
                }
                detail == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VIOLET_H)
                }
                else -> {
                    val d = detail!!
                    val estEnvoye = d.sens.equals("ENVOYE", ignoreCase = true)
                    Column(
                        modifier = Modifier.fillMaxSize()
                            .verticalScroll(rememberScrollState()).padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (if (estEnvoye) "−" else "+") + "%,.2f %s".format(d.montant, d.devise),
                            fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                            color = if (estEnvoye) MaterialTheme.colorScheme.error else VERT_H,
                        )
                        Box(
                            modifier = Modifier.padding(top = 6.dp)
                                .background(couleurStatut(d.statut).copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 14.dp, vertical = 5.dp),
                        ) {
                            Text(statutLisible(d.statut), color = couleurStatut(d.statut),
                                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(20.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                LigneDetail(tr("Expéditeur", "Moun ki voye"),
                                    (d.expediteurNom ?: "—") + (d.expediteurUsername?.let { "  @$it" } ?: ""))
                                LigneDetail(tr("Bénéficiaire", "Benefisyè"),
                                    (d.beneficiaireNom ?: "—") + (d.beneficiaireUsername?.let { "  @$it" } ?: ""))
                                LigneDetail(tr("Montant", "Montan"), "%,.2f %s".format(d.montant, d.devise))
                                LigneDetail(tr("Frais", "Frè"), "%,.2f %s".format(d.frais, d.devise))
                                LigneDetail(tr("Date", "Dat"), d.date ?: "—")
                                LigneDetail(tr("N° de confirmation", "Nº konfimasyon"), d.reference)
                                LigneDetail(tr("ID transaction", "ID tranzaksyon"), d.transactionId)
                                d.methodeAuth?.let { LigneDetail(tr("Authentification", "Otantifikasyon"), it) }
                                d.message?.takeIf { it.isNotBlank() }?.let {
                                    LigneDetail(tr("Message", "Mesaj"), it)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LigneDetail(label: String, valeur: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(valeur, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.4f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

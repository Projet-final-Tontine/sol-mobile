package com.sol.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sol.app.data.MessageResponse
import com.sol.app.data.Network
import com.sol.app.data.Session
import com.sol.app.data.tr
import kotlinx.coroutines.delay

/** "2026-07-06T12:16:34" -> "12:16". */
private fun heure(iso: String?): String = try {
    iso?.substring(11, 16) ?: ""
} catch (_: Throwable) {
    ""
}

/**
 * Ecran de discussion (chat de groupe d'un Sol ou chat prive).
 * Les messages se rafraichissent automatiquement toutes les 2 secondes tant que
 * l'ecran est ouvert : ressenti quasi-instantane.
 */
@Composable
fun ChatScreen(
    vm: HomeViewModel,
    cible: ChatCible,
    onFermer: () -> Unit,
) {
    val etatListe = rememberLazyListState()
    var saisie by remember { mutableStateOf("") }
    val messages = vm.messagesChat

    // Rafraichissement automatique toutes les 2 s.
    LaunchedEffect(cible.solId, cible.autreId) {
        while (true) {
            vm.rafraichirMessages()
            delay(2000)
        }
    }
    // Defilement automatique vers le dernier message.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) etatListe.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ---------- En-tete ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onFermer) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = tr("Retour", "Tounen"),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (cible.estGroupe) Icons.Default.Groups else Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    cible.titre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                )
                Text(
                    if (cible.estGroupe) tr("Chat du groupe", "Chat gwoup la")
                    else tr("Message privé", "Mesaj prive"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }

        // ---------- Messages ----------
        Box(modifier = Modifier.weight(1f)) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("💬", fontSize = 40.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        tr("Aucun message pour l'instant.\nÉcrivez le premier !",
                            "Poko gen mesaj.\nEkri premye a !"),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    state = etatListe,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                ) {
                    items(messages, key = { it.id }) { message ->
                        BulleMessage(
                            message = message,
                            estMoi = message.expediteurId == Session.utilisateurId,
                            afficherNom = cible.estGroupe,
                        )
                    }
                }
            }
        }

        // ---------- Saisie ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = saisie,
                onValueChange = { saisie = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(tr("Votre message…", "Mesaj ou…")) },
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            )
            Spacer(Modifier.width(8.dp))
            val actif = saisie.isNotBlank() && !vm.envoiEnCours
            IconButton(
                onClick = {
                    if (actif) {
                        vm.envoyerMessage(saisie)
                        saisie = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (actif) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    ),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = tr("Envoyer", "Voye"),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

/** Bulle d'un message : à droite (violet) si c'est moi, à gauche sinon. */
@Composable
private fun BulleMessage(
    message: MessageResponse,
    estMoi: Boolean,
    afficherNom: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (estMoi) Arrangement.End else Arrangement.Start,
    ) {
        // Avatar de l'autre (a gauche).
        if (!estMoi) {
            if (!message.expediteurPhoto.isNullOrBlank()) {
                AsyncImage(
                    model = Network.BASE_URL.trimEnd('/') + message.expediteurPhoto,
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (estMoi) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp),
        ) {
            if (afficherNom && !estMoi) {
                Text(
                    message.expediteurNom ?: "Membre",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 6.dp, bottom = 2.dp),
                )
            }
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (estMoi) 16.dp else 4.dp,
                            bottomEnd = if (estMoi) 4.dp else 16.dp,
                        )
                    )
                    .background(
                        if (estMoi) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    message.contenu,
                    color = if (estMoi) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                )
            }
            Text(
                heure(message.dateEnvoi),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

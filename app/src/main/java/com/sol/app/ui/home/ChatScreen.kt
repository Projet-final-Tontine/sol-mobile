package com.sol.app.ui.home

import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
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

/** Lit le fichier choisi et l'envoie comme piece jointe (image ou document). */
private fun envoyerUri(context: Context, uri: Uri, type: String, vm: HomeViewModel) {
    val octets = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return
    vm.envoyerPieceJointe(octets, nomFichier(context, uri), type)
}

/** Recupere le nom d'affichage d'un fichier a partir de son Uri. */
private fun nomFichier(context: Context, uri: Uri): String {
    var nom = "piece"
    context.contentResolver.query(uri, null, null, null, null)?.use { c ->
        val idx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && c.moveToFirst()) c.getString(idx)?.let { nom = it }
    }
    return nom
}

/** Ouvre une URL (image ou document) dans l'application adaptee du telephone. */
private fun ouvrirUrl(context: Context, url: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (_: Throwable) {
        // Aucune application pour ouvrir ce lien : on ignore silencieusement.
    }
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

    val contexte = LocalContext.current
    var menuJoindre by remember { mutableStateOf(false) }
    // Selecteur d'image : envoie la photo choisie comme piece jointe.
    val choisirImage = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { envoyerUri(contexte, it, "IMAGE", vm) } }
    // Selecteur de document (tout type de fichier).
    val choisirDoc = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { envoyerUri(contexte, it, "DOCUMENT", vm) } }

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
            // Bouton pièce jointe : photo ou document.
            Box {
                IconButton(
                    onClick = { menuJoindre = true },
                    enabled = !vm.envoiEnCours,
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = tr("Joindre", "Mete yon pyès"),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                DropdownMenu(expanded = menuJoindre, onDismissRequest = { menuJoindre = false }) {
                    DropdownMenuItem(
                        text = { Text(tr("📷  Photo", "📷  Foto")) },
                        onClick = { menuJoindre = false; choisirImage.launch("image/*") },
                    )
                    DropdownMenuItem(
                        text = { Text(tr("📄  Document", "📄  Dokiman")) },
                        onClick = { menuJoindre = false; choisirDoc.launch("*/*") },
                    )
                }
            }
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
            val contexte = LocalContext.current
            val couleurTexte = if (estMoi) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
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
                Column {
                    val urlComplet = message.pieceJointeUrl
                        ?.let { Network.BASE_URL.trimEnd('/') + it }
                    when (message.typePiece?.uppercase()) {
                        "IMAGE" -> if (urlComplet != null) {
                            AsyncImage(
                                model = urlComplet,
                                contentDescription = tr("Image jointe", "Foto"),
                                modifier = Modifier
                                    .size(width = 200.dp, height = 200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { ouvrirUrl(contexte, urlComplet) },
                                contentScale = ContentScale.Crop,
                            )
                            if (message.contenu.isNotBlank()) Spacer(Modifier.height(6.dp))
                        }

                        "DOCUMENT" -> if (urlComplet != null) {
                            val ext = message.pieceJointeUrl!!.substringAfterLast('.', "").uppercase()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { ouvrirUrl(contexte, urlComplet) }
                                    .padding(vertical = 2.dp),
                            ) {
                                Text("📄", fontSize = 24.sp)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        tr("Document", "Dokiman") +
                                            if (ext.isNotBlank()) " ($ext)" else "",
                                        color = couleurTexte,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                    )
                                    Text(
                                        tr("Toucher pour ouvrir", "Peze pou ouvri"),
                                        color = couleurTexte.copy(alpha = 0.75f),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                    )
                                }
                            }
                            if (message.contenu.isNotBlank()) Spacer(Modifier.height(6.dp))
                        }
                    }
                    if (message.contenu.isNotBlank()) {
                        Text(
                            message.contenu,
                            color = couleurTexte,
                            fontSize = 15.sp,
                        )
                    }
                }
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

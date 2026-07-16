package com.sol.app.ui.home

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sol.app.data.Network
import com.sol.app.data.ReleveResponse
import com.sol.app.data.messageErreur
import com.sol.app.data.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Écran « Mon Relevé de Fiabilité Financière ».
 * Affiche le score calculé par le serveur, l'historique de paiement, un QR code
 * de vérification, et permet de générer un PDF officiel à présenter à une banque.
 */
@Composable
fun EcranReleve(onFermer: () -> Unit) {
    val contexte = LocalContext.current
    val scope = rememberCoroutineScope()

    var releve by remember { mutableStateOf<ReleveResponse?>(null) }
    var erreur by remember { mutableStateOf<String?>(null) }
    var enCours by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            releve = Network.api.monReleve()
        } catch (e: Throwable) {
            erreur = messageErreur(e)
        } finally {
            enCours = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // En-tête violet.
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
                    contentDescription = tr("Retour", "Retounen"),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(
                tr("Mon Relevé de Fiabilité", "Relve Fyabilite mwen"),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }

        when {
            enCours -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            erreur != null -> Text(
                erreur ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(24.dp),
            )

            releve != null -> ContenuReleve(
                releve = releve!!,
                message = message,
                onPartager = {
                    scope.launch {
                        message = null
                        val r = releve!!
                        val uri = withContext(Dispatchers.IO) {
                            val qr = genererQrReleve(urlVerification(r), 400)
                            enregistrerPdfReleve(contexte, r, qr)
                        }
                        if (uri != null) {
                            partagerPdf(contexte, uri)
                            message = tr("PDF enregistré dans Téléchargements ✅", "PDF anrejistre nan Downloads ✅")
                        } else {
                            message = tr(
                                "Impossible de générer le PDF (Android 10+ requis).",
                                "Pa ka fè PDF la (Android 10+ obligatwa).",
                            )
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun ContenuReleve(
    releve: ReleveResponse,
    message: String?,
    onPartager: () -> Unit,
) {
    val couleur = couleurNote(releve.note)
    Column(modifier = Modifier.padding(20.dp)) {

        // Carte score.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(couleur.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${releve.scoreGlobal}",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = couleur,
                        )
                        Text("/ 100", fontSize = 13.sp, color = couleur.copy(alpha = 0.8f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(couleur, RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Text(
                        tr("Note", "Nòt") + " ${releve.note}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    releve.niveau,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    releve.nomComplet ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Détail des statistiques.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                LigneReleve(tr("Membre depuis", "Manm depi"), releve.membreDepuis ?: "—")
                LigneReleve(tr("Nombre de sols", "Kantite sòl"), "${releve.nbSols}")
                LigneReleve(
                    tr("Total cotisé", "Total kotize"),
                    "${releve.totalCotise.toLong()} HTG",
                )
                LigneReleve(tr("Cotisations évaluées", "Kotizasyon evalye"), "${releve.nbCotisations}")
                LigneReleve(tr("Payées à temps", "Peye alè"), "${releve.nbATemps}", Color(0xFF1B8A4E))
                LigneReleve(tr("En retard", "An reta"), "${releve.nbRetards}", Color(0xFFFB8C00))
                LigneReleve(tr("Défauts", "Defo"), "${releve.nbDefauts}", Color(0xFFC62828))
            }
        }

        Spacer(Modifier.height(14.dp))

        // QR de vérification.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        tr("Vérification officielle", "Verifikasyon ofisyèl"),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(Modifier.height(10.dp))
                val qr = remember(releve.reference) {
                    genererQrReleve(urlVerification(releve), 320).asImageBitmap()
                }
                Image(bitmap = qr, contentDescription = "QR", modifier = Modifier.size(170.dp))
                Spacer(Modifier.height(8.dp))
                Text(
                    releve.reference,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    tr(
                        "Une banque scanne ce code pour confirmer votre relevé.",
                        "Yon bank eskane kòd sa a pou konfime relve ou.",
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Button(
            onClick = onPartager,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                tr("Télécharger / Partager le PDF", "Telechaje / Pataje PDF la"),
                fontWeight = FontWeight.SemiBold,
            )
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.primary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun LigneReleve(libelle: String, valeur: String, couleur: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(libelle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            valeur,
            fontWeight = FontWeight.SemiBold,
            color = couleur ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ---------------------------------------------------------------- utilitaires

/** Couleur associée à la note du relevé. */
private fun couleurNote(note: String): Color = when (note) {
    "A" -> Color(0xFF1B8A4E)
    "B" -> Color(0xFF2E7D32)
    "C" -> Color(0xFFFB8C00)
    "D" -> Color(0xFFC62828)
    else -> Color(0xFF6B7280)
}

/** URL de vérification pointant vers le serveur joignable (même hôte que l'API). */
private fun urlVerification(r: ReleveResponse): String =
    Network.BASE_URL.trimEnd('/') + "/api/releve/verifier/" + r.reference

/** Génère le QR code (zxing, hors ligne). */
private fun genererQrReleve(texte: String, taille: Int): Bitmap {
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
 * Construit le PDF officiel du relevé et l'enregistre dans Téléchargements
 * (MediaStore, Android 10+). Renvoie l'URI du fichier, ou null si non supporté.
 */
private fun enregistrerPdfReleve(context: Context, r: ReleveResponse, qr: Bitmap): Uri? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null

    val largeur = 595
    val hauteur = 842 // A4 en points (72 dpi)
    val doc = PdfDocument()
    val page = doc.startPage(PdfDocument.PageInfo.Builder(largeur, hauteur, 1).create())
    val c: Canvas = page.canvas
    val p = Paint(Paint.ANTI_ALIAS_FLAG)

    // En-tête violet.
    p.color = android.graphics.Color.parseColor("#3A22A8")
    c.drawRect(0f, 0f, largeur.toFloat(), 90f, p)
    p.color = android.graphics.Color.WHITE
    p.textSize = 24f
    p.isFakeBoldText = true
    c.drawText("SOL EN LIGNE", 40f, 48f, p)
    p.textSize = 13f
    p.isFakeBoldText = false
    c.drawText("Certificat de Fiabilité Financière", 40f, 72f, p)

    // Score + note.
    val couleur = android.graphics.Color.parseColor(
        when (r.note) {
            "A", "B" -> "#1B8A4E"; "C" -> "#FB8C00"; "D" -> "#C62828"; else -> "#6B7280"
        }
    )
    p.color = couleur
    p.textSize = 46f
    p.isFakeBoldText = true
    c.drawText("${r.scoreGlobal}/100", 40f, 160f, p)
    p.textSize = 16f
    c.drawText("Note ${r.note} — ${r.niveau}", 40f, 188f, p)

    // QR en haut à droite.
    c.drawBitmap(Bitmap.createScaledBitmap(qr, 130, 130, false), largeur - 170f, 105f, null)

    // Corps : statistiques.
    p.color = android.graphics.Color.BLACK
    p.isFakeBoldText = false
    p.textSize = 14f
    var y = 250f
    fun ligne(label: String, valeur: String) {
        p.color = android.graphics.Color.parseColor("#6B7280")
        c.drawText(label, 40f, y, p)
        p.color = android.graphics.Color.BLACK
        c.drawText(valeur, 320f, y, p)
        y += 30f
    }
    ligne("Titulaire", r.nomComplet ?: "—")
    ligne("Membre depuis", r.membreDepuis ?: "—")
    ligne("Nombre de sols", "${r.nbSols}")
    ligne("Total cotisé", "${r.totalCotise.toLong()} HTG")
    ligne("Cotisations évaluées", "${r.nbCotisations}")
    ligne("Payées à temps", "${r.nbATemps}")
    ligne("En retard", "${r.nbRetards}")
    ligne("Défauts de paiement", "${r.nbDefauts}")

    // Pied : référence + vérification.
    y += 20f
    p.color = android.graphics.Color.parseColor("#3A22A8")
    p.textSize = 12f
    c.drawText("Référence : ${r.reference}", 40f, y, p)
    c.drawText("Vérifiez l'authenticité : ${urlVerification(r)}", 40f, y + 22f, p)
    p.color = android.graphics.Color.parseColor("#9CA3AF")
    c.drawText("Document généré par SOL EN LIGNE — infalsifiable (SHA-256).", 40f, y + 44f, p)

    doc.finishPage(page)

    val valeurs = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, "Releve_${r.reference}.pdf")
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

/** Ouvre le sélecteur de partage pour le PDF (URI MediaStore, sans FileProvider). */
private fun partagerPdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, "Partager le relevé")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

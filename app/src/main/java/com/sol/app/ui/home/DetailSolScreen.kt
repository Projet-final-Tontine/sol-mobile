package com.sol.app.ui.home

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sol.app.R
import com.sol.app.data.EtatCotisation
import com.sol.app.data.MembreInfo
import com.sol.app.data.Network
import com.sol.app.data.Session
import com.sol.app.data.SolResponse
import com.sol.app.data.tr
import kotlin.math.roundToInt

// ----- Palette de l'ecran (cartes sombres posees sur le fond image) -----
private val CarteSombre = Color(0xE6191233)      // legerement translucide
private val CarteInterne = Color(0xFF241C48)
private val BordureCarte = Color(0x552C2452)
private val VioletVif = Color(0xFF8B6CF8)
private val VioletDoux = Color(0xFFC5B5FF)
private val TexteBlanc = Color(0xFFF6F3FF)
private val TexteMuet = Color(0xFFA79FC8)
private val VertOk = Color(0xFF35D07F)
private val OrangeAttente = Color(0xFFF5A623)
private val RougeRetard = Color(0xFFFF5A6E)
private val MagentaAnneau = Color(0xFFE040FB)
private val BleuAnneau = Color(0xFF4FC3F7)

private val MOIS_LONGS = listOf(
    "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
    "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre",
)

private fun dateLongue(iso: String?): String? = try {
    iso?.take(10)?.let {
        val d = java.time.LocalDate.parse(it)
        "${d.dayOfMonth} ${MOIS_LONGS[d.monthValue - 1]} ${d.year}"
    }
} catch (_: Throwable) {
    null
}

private fun joursRestants(iso: String?): Long? = try {
    iso?.take(10)?.let {
        java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.now(), java.time.LocalDate.parse(it)
        )
    }
} catch (_: Throwable) {
    null
}

/** Genere le QR code du code d'invitation (zxing, sans reseau). */
private fun genererQr(texte: String, taille: Int): Bitmap {
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
 * Ecran plein « Détail du Sol », version premium :
 * espacements constants (16 dp), textes sur une seule ligne (ellipse),
 * statistiques en grille 2x2, anneau de progression anime, cartes aerees.
 * Fond : image `welcome_bg` (vagues violettes) recouverte d'un voile sombre.
 */
@Composable
fun EcranDetailSol(
    vm: HomeViewModel,
    sol: SolResponse,
    onFermer: () -> Unit,
    onVoirCalendrier: () -> Unit,
) {
    val detail = vm.detailComplet
    val estMamanSol = sol.mamanSolId == Session.utilisateurId
    val contexte = LocalContext.current
    val pressePapiers = LocalClipboardManager.current

    var menuOuvert by remember { mutableStateOf(false) }
    var historiqueOuvert by remember { mutableStateOf(false) }
    var voirTousMembres by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fond : vagues violettes (image deja presente dans le projet).
        Image(
            painter = painterResource(R.drawable.welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        // Voile sombre pour garder les cartes lisibles sur le centre clair du fond.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC120C28), Color(0xE60D0920))
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(10.dp))

            // ================= EN-TETE =================
            Row(verticalAlignment = Alignment.CenterVertically) {
                BoutonEntete(Icons.AutoMirrored.Filled.ArrowBack, tr("Retour", "Tounen"), onFermer)
                Spacer(Modifier.width(12.dp))
                Text(
                    tr("Détail du Sol", "Detay Sòl la"),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TexteBlanc,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                BoutonEntete(Icons.Default.Share, tr("Partager", "Pataje")) {
                    val partage = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            tr(
                                "Rejoins mon Sol « ${sol.nom} » sur Tontine Numérique ! Code d'invitation : ${sol.codeInvitation}",
                                "Vin antre nan Sòl mwen « ${sol.nom} » sou Tontine Numérique ! Kòd envitasyon : ${sol.codeInvitation}",
                            ),
                        )
                    }
                    contexte.startActivity(Intent.createChooser(partage, sol.nom))
                }
                Spacer(Modifier.width(10.dp))
                Box {
                    BoutonEntete(Icons.Default.MoreVert, "Menu") { menuOuvert = true }
                    DropdownMenu(expanded = menuOuvert, onDismissRequest = { menuOuvert = false }) {
                        if (estMamanSol && sol.statut.equals("OUVERT", true)) {
                            DropdownMenuItem(
                                text = { Text(tr("Démarrer le cycle", "Kòmanse sik la")) },
                                onClick = { menuOuvert = false; vm.demarrerCycle(sol.id) },
                            )
                        }
                        if (estMamanSol && sol.statut.equals("EN_COURS", true)) {
                            DropdownMenuItem(
                                text = { Text(tr("Ouvrir un tour", "Ouvri yon tou")) },
                                onClick = { menuOuvert = false; vm.ouvrirDialogueTour() },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(tr("Quitter le Sol", "Kite Sòl la"), color = RougeRetard) },
                            onClick = { menuOuvert = false; vm.quitterSol(sol.id) },
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================= CARTE IDENTITE =================
            CarteDetail {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(VioletVif),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.Groups,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        PastilleStatut(sol.statut)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            sol.nom,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TexteBlanc,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    // QR code compact
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val qr = remember(sol.codeInvitation) { genererQr(sol.codeInvitation, 200) }
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(5.dp),
                        ) {
                            Image(
                                bitmap = qr.asImageBitmap(),
                                contentDescription = "QR code",
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tr("Scanner", "Eskane"),
                            fontSize = 9.sp,
                            color = TexteMuet,
                            maxLines = 1,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = BordureCarte)
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("Code d'invitation", "Kòd envitasyon"),
                        fontSize = 13.sp,
                        color = TexteMuet,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        sol.codeInvitation,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletDoux,
                        letterSpacing = 2.sp,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarteInterne)
                            .clickable {
                                pressePapiers.setText(AnnotatedString(sol.codeInvitation))
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = tr("Copier", "Kopye"),
                            tint = VioletDoux,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================= STATISTIQUES 2 x 2 =================
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CarteStat(
                    Icons.Default.AccountBalanceWallet,
                    "${sol.montantCotisation.toLong()} HTG",
                    tr("Cotisation", "Kotizasyon"),
                    Modifier.weight(1f),
                )
                CarteStat(
                    Icons.Default.DateRange,
                    if (sol.frequence.equals("HEBDOMADAIRE", true))
                        tr("Hebdomadaire", "Chak semèn") else tr("Mensuelle", "Chak mwa"),
                    tr("Fréquence", "Frekans"),
                    Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CarteStat(
                    Icons.Default.Groups,
                    "${detail?.nombreMembres ?: 0} / ${sol.nombreMaxMembres}",
                    tr("Membres", "Manm"),
                    Modifier.weight(1f),
                )
                CarteStat(
                    Icons.Default.Timelapse,
                    "${detail?.tourCourant?.numero ?: detail?.toursJoues ?: 0} / ${detail?.totalTours ?: sol.nombreMaxMembres}",
                    tr("Tours", "Tou"),
                    Modifier.weight(1f),
                )
            }

            // ================= SANTE DU SOL =================
            detail?.sante?.let { sante ->
                Spacer(Modifier.height(16.dp))
                CarteDetail {
                    val (couleur, libelle, emoji) = when (sante.niveau.uppercase()) {
                        "EXCELLENT" -> Triple(VertOk, tr("Excellent", "Ekselan"), "🟢")
                        "MOYEN" -> Triple(OrangeAttente, tr("Moyen", "Mwayen"), "🟡")
                        else -> Triple(RougeRetard, tr("Risqué", "Riske"), "🔴")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tr("Santé du Sol", "Sante Sòl la"),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TexteBlanc,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(couleur.copy(alpha = 0.18f))
                                .border(1.dp, couleur.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            Text(
                                "$emoji $libelle",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = couleur,
                                maxLines = 1,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // Jauge horizontale animee.
                    val progressionSante by animateFloatAsState(
                        targetValue = sante.score / 100f,
                        animationSpec = tween(durationMillis = 800),
                        label = "sante",
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF2A2350)),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressionSante)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(couleur),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${sante.score}% " + tr(
                            "des cotisations réglées à temps",
                            "kotizasyon peye alè",
                        ),
                        fontSize = 12.sp,
                        color = TexteMuet,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================= PROGRESSION DU CYCLE =================
            CarteDetail {
                val total = detail?.totalTours ?: sol.nombreMaxMembres
                val courant = detail?.tourCourant?.numero ?: detail?.toursJoues ?: 0
                val pourcentage =
                    if (total <= 0) 0 else ((courant.toFloat() / total) * 100).roundToInt()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("Progression du cycle", "Pwogresyon sik la"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TexteBlanc,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(VioletVif.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            "$pourcentage% " + tr("terminé", "fini"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VioletDoux,
                            maxLines = 1,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Grand compteur + stepper horizontal (chemin des tours).
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$courant",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TexteBlanc,
                        maxLines = 1,
                    )
                    Text(
                        " / $total " + tr("tours", "tou"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TexteMuet,
                        maxLines = 1,
                        modifier = Modifier.padding(bottom = 7.dp),
                    )
                }

                Spacer(Modifier.height(18.dp))

                StepperCycle(
                    total = total,
                    courant = courant,
                    clotures = detail?.toursJoues ?: 0,
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    LegendePoint(MagentaAnneau, tr("Terminé", "Fini"))
                    Spacer(Modifier.width(16.dp))
                    LegendePoint(BleuAnneau, tr("En cours", "K ap fèt"))
                    Spacer(Modifier.width(16.dp))
                    LegendePoint(Color(0xFF4A4370), tr("À venir", "K ap vini"))
                }

                Spacer(Modifier.height(18.dp))

                // Position + prochain paiement, cote a cote, memes proportions.
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    detail?.maPosition?.let { pos ->
                        CartePosition(
                            ordre = pos.ordre,
                            total = pos.total,
                            date = dateLongue(pos.datePrevue),
                            estimee = pos.dateEstimee,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    detail?.tourCourant?.let { t ->
                        CarteProchainPaiement(
                            beneficiaire = t.beneficiaireNom,
                            date = dateLongue(t.datePrevue),
                            jours = joursRestants(t.datePrevue),
                            onVoirCalendrier = onVoirCalendrier,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================= MEMBRES DU SOL =================
            CarteDetail {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        tr("Membres du Sol", "Manm Sòl la"),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TexteBlanc,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        if (voirTousMembres) tr("Réduire", "Redui")
                        else tr("Voir tout", "Wè tout") + " ›",
                        color = VioletDoux,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { voirTousMembres = !voirTousMembres }
                            .padding(4.dp),
                    )
                }
                Text(
                    tr("Ordre de passage du cycle", "Lòd pasaj sik la"),
                    fontSize = 12.sp,
                    color = TexteMuet,
                    maxLines = 1,
                )
                Spacer(Modifier.height(12.dp))

                when {
                    vm.chargementMembres -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = VioletVif) }

                    detail == null || detail.membres.isEmpty() -> Text(
                        tr("Aucun membre pour le moment.", "Poko gen manm."),
                        color = TexteMuet,
                    )

                    else -> {
                        val etatParMembre = detail.etatCotisations.associateBy { it.utilisateurId }
                        val liste =
                            if (voirTousMembres) detail.membres else detail.membres.take(5)
                        liste.forEachIndexed { index, membre ->
                            LigneMembreSol(
                                membre = membre,
                                etat = etatParMembre[membre.utilisateurId],
                                montantParDefaut = sol.montantCotisation,
                            )
                            if (index < liste.lastIndex) Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ================= TUILES D'ACTION =================
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TuileAction(
                    Icons.Default.Chat, VioletVif, tr("Chat du Sol", "Chat Sòl"),
                    Modifier.weight(1f),
                ) { vm.ouvrirChatGroupe(sol) }
                TuileAction(
                    Icons.Default.Lock, Color(0xFF3B82F6), tr("Chat privé", "Chat prive"),
                    Modifier.weight(1f),
                ) { vm.ouvrirSelecteurPrive() }
                TuileAction(
                    Icons.Default.Notifications, OrangeAttente, tr("Notifs", "Notif"),
                    Modifier.weight(1f),
                ) {
                    vm.montrerBientot(
                        tr("Les notifications arrivent bientôt. 🔔",
                            "Notifikasyon yo ap vini byento. 🔔"),
                    )
                }
                TuileAction(
                    Icons.Default.Description, Color(0xFF2DD4BF), tr("Journal", "Jounal"),
                    Modifier.weight(1f),
                ) { historiqueOuvert = true }
            }

            // ================= MANMAN SOL =================
            if (estMamanSol && vm.paiementsEnAttente.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                CarteDetail {
                    Text(
                        tr("Paiements à valider", "Peman pou valide") +
                            " (${vm.paiementsEnAttente.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletDoux,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(8.dp))
                    vm.paiementsEnAttente.forEach { p ->
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
                                    color = TexteBlanc,
                                    maxLines = 1,
                                )
                                Text(
                                    "Réf : ${p.referenceTransaction ?: "—"}",
                                    fontSize = 11.sp,
                                    color = TexteMuet,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(onClick = { vm.validerPaiement(p.id) }) {
                                Text("✓ " + tr("Valider", "Valide"), color = VertOk, maxLines = 1)
                            }
                            TextButton(onClick = { vm.rejeterPaiement(p.id) }) {
                                Text("✗", color = RougeRetard)
                            }
                        }
                    }
                }
            }

            if (estMamanSol &&
                (sol.statut.equals("OUVERT", true) || sol.statut.equals("EN_COURS", true))
            ) {
                Spacer(Modifier.height(16.dp))
                val ouvert = sol.statut.equals("OUVERT", true)
                Button(
                    onClick = {
                        if (ouvert) vm.demarrerCycle(sol.id) else vm.ouvrirDialogueTour()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletVif),
                ) {
                    Text(
                        if (ouvert) tr("Démarrer le cycle", "Kòmanse sik la")
                        else tr("Ouvrir un tour", "Ouvri yon tou"),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ================= DIALOGUE JOURNAL DU SOL =================
    if (historiqueOuvert) {
        AlertDialog(
            onDismissRequest = { historiqueOuvert = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("📝 " + tr("Journal du Sol", "Jounal Sòl la"), fontWeight = FontWeight.Bold)
            },
            text = {
                val evenements = detail?.journal.orEmpty()
                if (evenements.isEmpty()) {
                    Text(
                        tr(
                            "Aucun événement pour le moment. Le journal se remplit automatiquement : adhésions, paiements, distributions…",
                            "Poko gen evènman. Jounal la ap ranpli otomatikman : adezyon, peman, distribisyon…",
                        )
                    )
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        evenements.forEach { e ->
                            val (emoji, texte) = when (e.type.uppercase()) {
                                "ADHESION" -> "🤝" to (e.acteurNom ?: "Membre") +
                                    " " + tr("a rejoint le Sol", "antre nan Sòl la")
                                "PAIEMENT" -> "✅" to (e.acteurNom ?: "Membre") +
                                    " " + tr("a payé", "peye") +
                                    (e.montant?.let { " ${it.toLong()} HTG" } ?: "")
                                "MAIN" -> "👑" to (e.acteurNom ?: "Membre") +
                                    " " + tr("a reçu la main", "resevwa men an") +
                                    (e.montant?.let { " (${it.toLong()} HTG)" } ?: "")
                                "TOUR_OUVERT" -> "🔵" to tr("Tour", "Tou") +
                                    " ${e.acteurNom ?: ""} " + tr("ouvert", "ouvri")
                                "RETARD" -> "🔴" to (e.acteurNom ?: "Membre") +
                                    " " + tr("est en retard", "an reta") +
                                    (e.montant?.let { " (${it.toLong()} HTG)" } ?: "")
                                else -> "•" to (e.acteurNom ?: "")
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Text(emoji, fontSize = 16.sp, modifier = Modifier.width(28.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        texte,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        dateLongue(e.date) ?: "—",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { historiqueOuvert = false }) {
                    Text(tr("Fermer", "Fèmen"))
                }
            },
        )
    }
}

// ------------------------------------------------------------------
// Composants internes
// ------------------------------------------------------------------

@Composable
private fun CarteDetail(contenu: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CarteSombre)
            .border(1.dp, BordureCarte, RoundedCornerShape(24.dp))
            .padding(18.dp),
    ) { contenu() }
}

@Composable
private fun BoutonEntete(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CarteSombre)
            .border(1.dp, BordureCarte, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icone, contentDescription = description, tint = TexteBlanc, modifier = Modifier.size(20.dp))
    }
}

/** Badge de statut horizontal (jamais ecrase). */
@Composable
private fun PastilleStatut(statut: String) {
    val (texte, fond, encre) = when (statut.uppercase()) {
        "EN_COURS" -> Triple(tr("En cours", "K ap fèt"), VioletVif.copy(alpha = 0.28f), VioletDoux)
        "OUVERT" -> Triple(tr("Ouvert", "Ouvè"), VertOk.copy(alpha = 0.22f), VertOk)
        "TERMINE" -> Triple(tr("Terminé", "Fini"), TexteMuet.copy(alpha = 0.22f), TexteMuet)
        else -> Triple(statut.lowercase(), TexteMuet.copy(alpha = 0.22f), TexteMuet)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(fond)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            texte,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = encre,
            maxLines = 1,
        )
    }
}

/** Petite carte statistique (grille 2x2) : icone mise en valeur + valeur + label. */
@Composable
private fun CarteStat(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    valeur: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CarteSombre)
            .border(1.dp, BordureCarte, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .defaultMinSize(minHeight = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(VioletVif.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = VioletDoux, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                valeur,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TexteBlanc,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                label,
                fontSize = 11.sp,
                color = TexteMuet,
                maxLines = 1,
            )
        }
    }
}

/**
 * Stepper horizontal du cycle : une pastille numerotee par tour, reliees par
 * une barre qui se remplit (degrade magenta -> bleu, animee). Le tour en cours
 * est mis en avant (couronne + anneau lumineux). Design fintech moderne.
 */
@Composable
private fun StepperCycle(total: Int, courant: Int, clotures: Int) {
    val n = total.coerceIn(1, 12)
    val progression = if (n <= 1) (if (courant >= 1) 1f else 0f)
    else ((courant - 1).coerceIn(0, n - 1)).toFloat() / (n - 1)
    val progressionAnimee by animateFloatAsState(
        targetValue = progression,
        animationSpec = tween(durationMillis = 800),
        label = "stepper",
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // 1) La barre de fond + la barre remplie, centrees verticalement.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
                .align(Alignment.Center),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF2A2350)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressionAnimee)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(listOf(MagentaAnneau, BleuAnneau))
                    ),
            )
        }

        // 2) Les pastilles numerotees, reparties sur toute la largeur.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (i in 1..n) {
                val etatFait = i < courant || i <= clotures
                val etatCourant = i == courant
                val (fond, encre) = when {
                    etatFait -> MagentaAnneau to Color.White
                    etatCourant -> BleuAnneau to Color.White
                    else -> Color(0xFF39325E) to TexteMuet
                }
                Box(contentAlignment = Alignment.Center) {
                    // Anneau lumineux sous la pastille du tour en cours.
                    if (etatCourant) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BleuAnneau.copy(alpha = 0.25f)),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(if (etatCourant) 34.dp else 30.dp)
                            .clip(CircleShape)
                            .background(fond)
                            .border(3.dp, Color(0xFF14102A), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("$i", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = encre)
                    }
                    if (etatCourant) {
                        Text(
                            "👑",
                            fontSize = 15.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-20).dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendePoint(couleur: Color, texte: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(couleur),
        )
        Spacer(Modifier.width(5.dp))
        Text(texte, fontSize = 11.sp, color = TexteMuet, maxLines = 1)
    }
}

/** Carte « Votre position » : le chiffre en vedette, jamais de retour a la ligne. */
@Composable
private fun CartePosition(
    ordre: Int,
    total: Int,
    date: String?,
    estimee: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CarteInterne)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(VioletVif),
                contentAlignment = Alignment.Center,
            ) { Text("👑", fontSize = 16.sp) }
            Spacer(Modifier.width(10.dp))
            Text(
                tr("Votre position", "Pozisyon ou"),
                fontSize = 11.sp,
                color = TexteMuet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                if (ordre == 1) "1ᵉʳ" else "${ordre}ᵉ",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TexteBlanc,
                maxLines = 1,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                tr("sur $total", "sou $total"),
                fontSize = 13.sp,
                color = TexteMuet,
                maxLines = 1,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        Text(
            tr("bénéficiaire", "benefisyè"),
            fontSize = 12.sp,
            color = TexteMuet,
            maxLines = 1,
        )
        if (date != null) {
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = BordureCarte)
            Spacer(Modifier.height(8.dp))
            Text(
                tr("Votre tour est prévu le", "Tou pa ou prevwa"),
                fontSize = 11.sp,
                color = TexteMuet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                date + if (estimee) " *" else "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VioletDoux,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Carte « Prochain paiement » (verte) : bénéficiaire, date, accès calendrier. */
@Composable
private fun CarteProchainPaiement(
    beneficiaire: String?,
    date: String?,
    jours: Long?,
    onVoirCalendrier: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF122E20))
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(VertOk),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                tr("Prochain bénéficiaire", "Pwochen benefisyè"),
                fontSize = 11.sp,
                color = TexteMuet,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(8.dp))
        // Le nom du prochain beneficiaire, mis en avant.
        Text(
            beneficiaire ?: "—",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = VertOk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = BordureCarte)
        Spacer(Modifier.height(6.dp))
        Text(
            tr("Prochain paiement", "Pwochen peman"),
            fontSize = 11.sp,
            color = TexteMuet,
            maxLines = 1,
        )
        Text(
            date ?: "—",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TexteBlanc,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        jours?.let { j ->
            Text(
                when {
                    j > 0 -> tr("Il reste $j jours", "Rete $j jou")
                    j == 0L -> tr("C'est aujourd'hui !", "Se jodi a !")
                    else -> tr("En retard de ${-j} jours", "An reta ${-j} jou")
                },
                fontSize = 11.sp,
                color = if (j < 0) RougeRetard else VertOk,
                maxLines = 1,
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onVoirCalendrier,
            colors = ButtonDefaults.buttonColors(containerColor = VertOk),
            shape = RoundedCornerShape(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 10.dp, vertical = 8.dp,
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                tr("Voir le calendrier", "Wè kalandriye a"),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

/**
 * Ligne d'un membre : numero, avatar, nom (une seule ligne, ellipse),
 * pastille « Vous » sous le nom, montant et etat alignes a droite.
 */
@Composable
private fun LigneMembreSol(
    membre: MembreInfo,
    etat: EtatCotisation?,
    montantParDefaut: Double,
) {
    val estMoi = membre.utilisateurId == Session.utilisateurId

    val enRetard = etat != null && !etat.statut.equals("VALIDE", true) && run {
        try {
            etat.dateEcheance?.take(10)
                ?.let { java.time.LocalDate.parse(it).isBefore(java.time.LocalDate.now()) }
                ?: false
        } catch (_: Throwable) {
            false
        }
    }
    val (texteEtat, couleurEtat) = when {
        etat == null -> "—" to TexteMuet
        etat.statut.equals("VALIDE", true) -> tr("Payé ✓", "Peye ✓") to VertOk
        etat.statut.equals("REJETE", true) -> tr("Rejeté ✗", "Rejte ✗") to RougeRetard
        enRetard -> tr("En retard !", "An reta !") to RougeRetard
        else -> tr("En attente", "An atant") to OrangeAttente
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (estMoi) Modifier
                    .background(VioletVif.copy(alpha = 0.14f))
                    .border(1.dp, VioletVif.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                else Modifier.background(CarteInterne.copy(alpha = 0.45f))
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Numero d'ordre
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (estMoi) VioletVif else Color(0xFF39325E)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${membre.ordre ?: "–"}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Spacer(Modifier.width(10.dp))

        // Avatar
        if (!membre.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = Network.BASE_URL.trimEnd('/') + membre.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF39325E)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = TexteMuet,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))

        // Nom sur UNE ligne + pastille « Vous » dessous : jamais d'ecrasement.
        Column(modifier = Modifier.weight(1f)) {
            Text(
                membre.nom ?: "Membre",
                fontSize = 14.sp,
                fontWeight = if (estMoi) FontWeight.Bold else FontWeight.Medium,
                color = TexteBlanc,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (estMoi) {
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(VioletVif.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        tr("Vous", "Ou menm"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VioletDoux,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.width(10.dp))

        // Montant + badge d'etat, alignes a droite.
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${(etat?.montant ?: montantParDefaut).toLong()} HTG",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TexteBlanc,
                maxLines = 1,
            )
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(couleurEtat.copy(alpha = 0.16f))
                    .border(1.dp, couleurEtat.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            ) {
                Text(
                    texteEtat,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = couleurEtat,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Tuile d'action (chat, notifications, historique). */
@Composable
private fun TuileAction(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    couleur: Color,
    titre: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CarteSombre)
            .border(1.dp, BordureCarte, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(couleur.copy(alpha = 0.24f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icone, contentDescription = null, tint = couleur, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(7.dp))
        Text(
            titre,
            fontSize = 10.5.sp,
            color = TexteBlanc,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

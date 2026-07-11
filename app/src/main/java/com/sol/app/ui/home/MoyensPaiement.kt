package com.sol.app.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sol.app.data.tr

// ============================================================================
//  MOYENS DE PAIEMENT  —  selecteur pour Deposer / Retirer.
//
//  Les liens vers MonCash, NatCash et les cartes ne sont pas encore actifs :
//  ils affichent « bientot disponible ». La « fiche de paie / recu » permet
//  deja de televerser une preuve de depot bancaire (en attente de confirmation).
//
//  LOGOS : ce sont des marques deposees, non incluses dans le code. Depose les
//  fichiers officiels dans res/drawable avec ces noms exacts et ils apparaitront
//  automatiquement (sinon un repere colore s'affiche a la place) :
//     logo_moncash · logo_natcash · logo_visa · logo_mastercard
// ============================================================================

private val FondMoyens = Color(0xF21A1338)
private val CarteMoyen = Color(0xFF241C48)
private val TexteMoyen = Color(0xFFF6F3FF)
private val TexteMoyenDoux = Color(0xFFB7AEDC)
private val AccentMoyen = Color(0xFF8B6CF8)

/** Un moyen de paiement affiche dans le selecteur. */
private data class MoyenPaiement(
    val nom: String,
    val sousTitre: String,
    val nomLogo: String?,     // nom du drawable officiel (ou null pour l'icone)
    val couleur: Color,       // couleur de marque (repere de secours)
    val emoji: String,        // repere de secours si le logo n'est pas fourni
    val estFiche: Boolean = false,
)

private fun moyensDepot(): List<MoyenPaiement> = listOf(
    MoyenPaiement("MonCash", tr("Portefeuille mobile", "Pòtfèy mobil"), "logo_moncash", Color(0xFFC8102E), "📱"),
    MoyenPaiement("NatCash", tr("Portefeuille mobile", "Pòtfèy mobil"), "logo_natcash", Color(0xFF009639), "📱"),
    MoyenPaiement("Visa", tr("Carte bancaire", "Kat bankè"), "logo_visa", Color(0xFF1A1F71), "💳"),
    MoyenPaiement("Mastercard", tr("Carte bancaire", "Kat bankè"), "logo_mastercard", Color(0xFFEB001B), "💳"),
    MoyenPaiement(
        tr("Fiche de paie / reçu", "Fich pèman / resi"),
        tr("Dépôt en banque — à confirmer", "Depo labank — pou konfime"),
        null, AccentMoyen, "🧾", estFiche = true,
    ),
)

private fun moyensRetrait(): List<MoyenPaiement> = listOf(
    MoyenPaiement("MonCash", tr("Portefeuille mobile", "Pòtfèy mobil"), "logo_moncash", Color(0xFFC8102E), "📱"),
    MoyenPaiement("NatCash", tr("Portefeuille mobile", "Pòtfèy mobil"), "logo_natcash", Color(0xFF009639), "📱"),
    MoyenPaiement(
        tr("Compte bancaire", "Kont labank"),
        tr("Virement — bientôt", "Vireman — byento"),
        null, Color(0xFF3B82F6), "🏦",
    ),
)

/**
 * Selecteur de moyen de paiement (Depot ou Retrait). Appelle [onBientot] avec
 * le nom du moyen pour les options pas encore actives, ou [onFiche] pour la
 * fiche de paie.
 */
@Composable
fun DialogueMoyensPaiement(
    type: String,                       // "DEPOT" ou "RETRAIT"
    onBientot: (String) -> Unit,
    onFiche: () -> Unit,
    onFermer: () -> Unit,
) {
    val estDepot = type == "DEPOT"
    val moyens = if (estDepot) moyensDepot() else moyensRetrait()

    Dialog(onDismissRequest = onFermer, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(FondMoyens)
                .padding(18.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (estDepot) tr("Déposer de l'argent", "Depoze lajan")
                        else tr("Retirer de l'argent", "Retire lajan"),
                        color = TexteMoyen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        tr("Choisissez un moyen", "Chwazi yon mwayen"),
                        color = TexteMoyenDoux,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onFermer) {
                    Icon(Icons.Default.Close, contentDescription = tr("Fermer", "Fèmen"), tint = TexteMoyenDoux)
                }
            }

            Spacer(Modifier.height(12.dp))

            moyens.forEach { moyen ->
                LigneMoyen(moyen) {
                    if (moyen.estFiche) onFiche() else onBientot(moyen.nom)
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

/** Une ligne du selecteur : logo + nom + sous-titre + fleche. */
@Composable
private fun LigneMoyen(moyen: MoyenPaiement, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CarteMoyen)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoMoyen(moyen)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                moyen.nom,
                color = TexteMoyen,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                moyen.sousTitre,
                color = TexteMoyenDoux,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = TexteMoyenDoux,
        )
    }
}

/**
 * Logo du moyen : cherche le drawable officiel par son nom (fourni par toi).
 * S'il n'existe pas, affiche un repere colore avec un emoji — le code compile
 * donc sans les logos, et ils apparaissent des que tu les ajoutes.
 */
@Composable
private fun LogoMoyen(moyen: MoyenPaiement) {
    val contexte = LocalContext.current
    val idLogo = remember(moyen.nomLogo) {
        moyen.nomLogo?.let {
            contexte.resources.getIdentifier(it, "drawable", contexte.packageName)
        } ?: 0
    }
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (idLogo != 0) Color.White else moyen.couleur),
        contentAlignment = Alignment.Center,
    ) {
        if (idLogo != 0) {
            Image(
                painter = painterResource(id = idLogo),
                contentDescription = moyen.nom,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(moyen.emoji, fontSize = 20.sp)
        }
    }
}

/**
 * Fiche de paie / recu : explique la marche a suivre pour un depot bancaire,
 * puis permet de televerser une photo de la preuve (en attente de confirmation).
 */
@Composable
fun DialogueFichePaie(
    enCours: Boolean,
    onEnvoyer: (ByteArray) -> Unit,
    onFermer: () -> Unit,
) {
    val contexte = LocalContext.current
    val choisirFichier = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val octets = contexte.contentResolver.openInputStream(it)?.use { flux ->
                flux.readBytes()
            }
            if (octets != null) onEnvoyer(octets)
        }
    }

    Dialog(onDismissRequest = onFermer, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .background(FondMoyens)
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧾", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    tr("Dépôt par fiche de paie", "Depo ak fich pèman"),
                    color = TexteMoyen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onFermer) {
                    Icon(Icons.Default.Close, contentDescription = tr("Fermer", "Fèmen"), tint = TexteMoyenDoux)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                tr(
                    "1. Rends-toi dans ta banque et effectue le dépôt/virement sur le compte du Sol.\n" +
                        "2. Prends en photo ta fiche de paie ou ton reçu de dépôt.\n" +
                        "3. Envoie-la ici : elle sera vérifiée et confirmée par l'administrateur.",
                    "1. Ale labank ou fè depo/vireman an sou kont Sòl la.\n" +
                        "2. Pran yon foto fich pèman ou oswa resi depo a.\n" +
                        "3. Voye l isit la : administratè a ap verifye epi konfime l.",
                ),
                color = TexteMoyenDoux,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(16.dp))

            if (enCours) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = AccentMoyen) }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentMoyen)
                        .clickable { choisirFichier.launch("image/*") }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tr("Choisir la fiche / le reçu", "Chwazi fich / resi a"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

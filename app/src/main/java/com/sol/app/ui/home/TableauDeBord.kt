package com.sol.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sol.app.data.tr

private val CarteDoux = Color(0x1FFFFFFF)   // blanc translucide (sur fond violet)
private val TexteDoux = Color(0xCCFFFFFF)
private val Accent = Color(0xFF8B6CF8)
private val VertGain = Color(0xFF34D399)

/**
 * Section « Mon activité » de l'accueil : indicateurs clés, projection de la
 * prochaine « main » à recevoir, et vraie courbe d'épargne. Les valeurs
 * viennent du serveur ({@code /api/tableau-de-bord}).
 */
@Composable
fun SectionTableauDeBord(vm: HomeViewModel) {
    val tb = vm.tableauDeBord ?: return

    Text(
        tr("Mon Activité", "Aktivite Mwen"),
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
    )
    Spacer(Modifier.height(12.dp))

    // Rangée de 3 indicateurs.
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CarteIndicateur(
            titre = tr("Épargné", "Ekonomize"),
            valeur = montantCourt(tb.totalCotise),
            emoji = "💰",
            modifier = Modifier.weight(1f),
        )
        CarteIndicateur(
            titre = tr("À recevoir", "Pou resevwa"),
            valeur = montantCourt(tb.totalARecevoir),
            emoji = "🎯",
            accent = VertGain,
            modifier = Modifier.weight(1f),
        )
        CarteIndicateur(
            titre = tr("Sols actifs", "Sòl aktif"),
            valeur = tb.nbSolsActifs.toString(),
            emoji = "👥",
            modifier = Modifier.weight(1f),
        )
    }

    // Projection : prochaine main à recevoir.
    tb.prochaineMain?.let { main ->
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF0F5132), Color(0xFF157347)))
                )
                .padding(18.dp),
        ) {
            Text(
                tr("Ta prochaine main 🎉", "Pwochen men ou 🎉"),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tr("Tu recevras ", "W ap resevwa ") + "%,.0f HTG".format(main.montant),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
            )
            Text(
                (main.solNom ?: "") +
                    (main.date?.let { " · " + tr("le ", "nan ") + dateCourte(it) } ?: ""),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
            )
        }
    }

    // Courbe d'épargne (vraies données).
    if (tb.epargne.size >= 2) {
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CarteDoux)
                .padding(16.dp),
        ) {
            Text(
                tr("Évolution de ton épargne", "Evolisyon ekonomi ou"),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(12.dp))
            CourbeEpargne(tb.epargne.map { it.cumul })
        }
    }
}

@Composable
private fun CarteIndicateur(
    titre: String,
    valeur: String,
    emoji: String,
    modifier: Modifier = Modifier,
    accent: Color = Color.White,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CarteDoux)
            .padding(vertical = 14.dp, horizontal = 12.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text(valeur, color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
        Text(titre, color = TexteDoux, fontSize = 12.sp)
    }
}

/** Trace la courbe cumulée de l'épargne (ligne + aire dégradée). */
@Composable
private fun CourbeEpargne(valeurs: List<Double>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
    ) {
        val max = (valeurs.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
        val n = valeurs.size
        val pasX = size.width / (n - 1)
        val padBas = 6f
        fun y(v: Double) = (size.height - padBas) - (v / max).toFloat() * (size.height - padBas * 2)

        val ligne = Path()
        val aire = Path()
        valeurs.forEachIndexed { i, v ->
            val x = i * pasX
            val yy = y(v)
            if (i == 0) {
                ligne.moveTo(x, yy)
                aire.moveTo(x, size.height)
                aire.lineTo(x, yy)
            } else {
                ligne.lineTo(x, yy)
                aire.lineTo(x, yy)
            }
        }
        aire.lineTo((n - 1) * pasX, size.height)
        aire.close()

        drawPath(
            aire,
            brush = Brush.verticalGradient(
                listOf(Accent.copy(alpha = 0.35f), Color.Transparent)
            ),
        )
        drawPath(ligne, color = Accent, style = Stroke(width = 6f))
        // Point final mis en evidence.
        drawCircle(
            color = VertGain,
            radius = 8f,
            center = Offset((n - 1) * pasX, y(valeurs.last())),
        )
    }
}

/** 12500.0 -> "12.5k" ; 800.0 -> "800". */
private fun montantCourt(v: Double): String = when {
    v >= 1000 -> "%.1fk".format(v / 1000)
    else -> "%.0f".format(v)
}

/** "2026-08-12" -> "12/08/2026". */
private fun dateCourte(iso: String): String {
    val p = iso.take(10).split("-")
    return if (p.size == 3) "${p[2]}/${p[1]}/${p[0]}" else iso
}

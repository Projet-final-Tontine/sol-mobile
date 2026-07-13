package com.sol.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
//  ELEMENTS VISUELS PARTAGES des ecrans Connexion / Inscription.
//  Champs arrondis harmonises, bouton degrade, titres de section.
// ============================================================================

/** Couleurs harmonisees des champs de saisie (bordure violette au focus). */
@Composable
fun couleursChampAuth(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
)

/**
 * Bouton principal avec un degrade violet, un coin bien arrondi et un
 * indicateur de chargement integre. Grise automatiquement quand desactive.
 */
@Composable
fun BoutonDegrade(
    texte: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enChargement: Boolean = false,
) {
    val forme = RoundedCornerShape(16.dp)
    val actif = enabled && !enChargement
    val fond = if (actif) {
        Brush.horizontalGradient(listOf(Color(0xFF7C5CF0), Color(0xFF5B3FD6), Color(0xFF3A22A8)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFB4A9E3), Color(0xFFB4A9E3)))
    }
    Button(
        onClick = onClick,
        enabled = actif,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = forme,
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fond, forme),
            contentAlignment = Alignment.Center,
        ) {
            if (enChargement) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(texte, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/** Petit titre de section avec une pastille emoji (formulaire d'inscription). */
@Composable
fun TitreSectionAuth(emoji: String, titre: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(emoji, fontSize = 16.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(
            titre,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Barre d'accent decorative sous un titre (degrade violet). */
@Composable
fun AccentTitre(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(46.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF7C5CF0), Color(0xFF3A22A8)))
            ),
    )
}

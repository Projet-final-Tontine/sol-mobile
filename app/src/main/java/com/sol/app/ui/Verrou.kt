package com.sol.app.ui

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.sol.app.R
import com.sol.app.data.tr

// ============================================================================
//  VERROUILLAGE DE L'APP  —  empreinte / visage / code du telephone.
//
//  Utilise BiometricPrompt d'AndroidX. Sur Android 11+ (API 30+), le code
//  (PIN / schema / mot de passe) du telephone sert de secours automatique.
//  En dessous, seule la biometrie est proposee (avec un bouton Annuler).
// ============================================================================

/** Authentificateurs autorises selon la version d'Android. */
private fun authentificateurs(): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    } else {
        BiometricManager.Authenticators.BIOMETRIC_WEAK
    }

/** Vrai si l'appareil peut verrouiller (biometrie et/ou code disponible). */
fun peutUtiliserVerrou(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(authentificateurs()) ==
        BiometricManager.BIOMETRIC_SUCCESS

/** Ouvre la fenetre systeme de deverrouillage (empreinte / visage / code). */
fun declencherDeverrouillage(activity: FragmentActivity, onSucces: () -> Unit) {
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSucces()
            }
        },
    )
    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Sol en ligne")
        .setSubtitle(tr("Déverrouillez pour continuer", "Debloke pou kontinye"))
        .setAllowedAuthenticators(authentificateurs())
    // Le bouton negatif est obligatoire SANS code du telephone (Android < 11).
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
        builder.setNegativeButtonText(tr("Annuler", "Anile"))
    }
    try {
        prompt.authenticate(builder.build())
    } catch (_: Throwable) {
        // Configuration biometrique invalide : on laisse passer pour ne pas bloquer.
        onSucces()
    }
}

/**
 * Ecran de verrouillage : affiche a l'ouverture de l'app quand l'utilisateur a
 * active le verrou. Declenche automatiquement la demande biometrique.
 */
@Composable
fun EcranVerrou(onDeverrouille: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var essai by remember { mutableIntStateOf(0) }

    LaunchedEffect(essai) {
        if (activity != null) {
            declencherDeverrouillage(activity, onSucces = onDeverrouille)
        } else {
            // Pas d'activite compatible : on ne bloque pas l'utilisateur.
            onDeverrouille()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xCC3A22A8), Color(0xF2140A38)))
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                tr("Application verrouillée", "App la bloke"),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tr(
                    "Déverrouillez avec votre empreinte, votre visage ou le code de votre téléphone.",
                    "Debloke ak anprint ou, figi ou oswa kòd telefòn ou.",
                ),
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = { essai++ },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f)),
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color.White)
                Spacer(Modifier.size(8.dp))
                Text(tr("Déverrouiller", "Debloke"), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

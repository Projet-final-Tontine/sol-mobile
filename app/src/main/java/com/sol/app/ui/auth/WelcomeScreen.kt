package com.sol.app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sol.app.R
import com.sol.app.ui.theme.SolViolet

/** Page de bienvenue : logo, slogan et acces vers Connexion ou Inscription. */
@Composable
fun WelcomeScreen(
    onConnexion: () -> Unit,
    onInscription: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // La moitie haute de l'ecran est reservee a l'illustration
        // (tirelire) de l'image de fond, quel que soit le telephone.
        val hauteurIllustration = maxHeight * 0.53f

        Image(
            painter = painterResource(R.drawable.welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(hauteurIllustration))

            // Logo + nom de l'application, cote a cote.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo Tontine Numerique",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Tontine Numérique",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF221A4E),
                    )
                    Text(
                        text = "Simplifier l'épargne collective",
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Bienvenue ! 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SolViolet,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(start = 30.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("Rejoignez une communauté solidaire\net gérez votre épargne en toute ")
                    withStyle(SpanStyle(color = SolViolet, fontWeight = FontWeight.SemiBold)) {
                        append("simplicité")
                    }
                    append(".")
                },
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF3F3D56),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(26.dp))

            Button(
                onClick = onConnexion,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SolViolet),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = null)
                    Spacer(Modifier.weight(1f))
                    Text("Se connecter", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }
            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onInscription,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SolViolet),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.PersonAddAlt, contentDescription = null)
                    Spacer(Modifier.weight(1f))
                    Text("Créer un compte", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

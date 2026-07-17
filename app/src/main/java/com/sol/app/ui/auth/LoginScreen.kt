package com.sol.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sol.app.R
import com.sol.app.data.Session

@Composable
fun LoginScreen(
    onConnecte: () -> Unit,
    onAllerInscription: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    var telephone by rememberSaveable { mutableStateOf(Session.identifiantMemorise ?: "") }
    var motDePasse by rememberSaveable { mutableStateOf("") }
    var motDePasseVisible by rememberSaveable { mutableStateOf(false) }
    var seSouvenir by rememberSaveable { mutableStateOf(Session.identifiantMemorise != null) }

    // --- Flux « Continuer avec Google » (Google Sign-In -> Firebase -> backend) ---
    val contexte = LocalContext.current
    val clientGoogle = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(contexte.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(contexte, options)
    }
    val lanceurGoogle = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { resultat ->
        try {
            val compte = GoogleSignIn.getSignedInAccountFromIntent(resultat.data)
                .getResult(ApiException::class.java)
            val idTokenGoogle = compte.idToken
            if (idTokenGoogle == null) {
                vm.signalerErreur("Impossible d'obtenir le jeton Google.")
                return@rememberLauncherForActivityResult
            }
            // Échange le jeton Google contre une session Firebase, puis récupère
            // le jeton d'identité Firebase à envoyer au backend.
            val credential = GoogleAuthProvider.getCredential(idTokenGoogle, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { tache ->
                    if (tache.isSuccessful) {
                        FirebaseAuth.getInstance().currentUser
                            ?.getIdToken(true)
                            ?.addOnCompleteListener { t ->
                                val jeton = t.result?.token
                                if (jeton != null) vm.connexionGoogle(jeton, onConnecte)
                                else vm.signalerErreur("Jeton Firebase manquant.")
                            }
                    } else {
                        vm.signalerErreur("Échec de la connexion Google.")
                    }
                }
        } catch (e: ApiException) {
            vm.signalerErreur("Connexion Google annulée.")
        }
    }
    val lancerGoogle: () -> Unit = {
        // Déconnexion préalable pour toujours afficher le sélecteur de compte.
        clientGoogle.signOut().addOnCompleteListener {
            lanceurGoogle.launch(clientGoogle.signInIntent)
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
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC3A22A8),
                            Color(0xE6140A38),
                        )
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo dans un anneau lumineux (halo translucide + lisere blanc).
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo SOL",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(Modifier.height(16.dp))

            Text(
                text = "SOL EN LIGNE",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 3.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Votre tontine numérique 🇭🇹",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xCCFFFFFF),
            )

            Spacer(Modifier.height(36.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Connexion",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    AccentTitre(Modifier.padding(top = 6.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Heureux de vous revoir ! 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(22.dp))

                    vm.erreur?.let {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ) {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    OutlinedTextField(
                        value = telephone,
                        onValueChange = { telephone = it },
                        label = { Text("Téléphone ou email") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(14.dp),
                        colors = couleursChampAuth(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = motDePasse,
                        onValueChange = { motDePasse = it },
                        label = { Text("Mot de passe") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { motDePasseVisible = !motDePasseVisible }) {
                                Icon(
                                    if (motDePasseVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = if (motDePasseVisible) "Masquer" else "Afficher",
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (motDePasseVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(14.dp),
                        colors = couleursChampAuth(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(4.dp))
                    // « Se souvenir » et « Mot de passe oublié ? » sur la meme ligne.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = seSouvenir,
                            onCheckedChange = { seSouvenir = it },
                        )
                        Text(
                            "Se souvenir de moi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { }) {
                            Text(
                                "Mot de passe oublié ?",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    BoutonDegrade(
                        texte = "Se connecter",
                        enChargement = vm.enChargement,
                        onClick = {
                            Session.identifiantMemorise = if (seSouvenir) telephone.trim() else null
                            vm.connexion(telephone, motDePasse, onConnecte)
                        },
                    )

                    Spacer(Modifier.height(16.dp))

                    // Séparateur "ou" (à l'intérieur de la carte).
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        )
                        Text(
                            "  ou  ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Bouton « Continuer avec Google ».
                    OutlinedButton(
                        onClick = lancerGoogle,
                        enabled = !vm.enChargement,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text(
                            "G",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF4285F4),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Continuer avec Google", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Separateur « ou » puis invitation a creer un compte.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.35f),
                )
                Text(
                    "  ou  ",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color.White.copy(alpha = 0.35f),
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = onAllerInscription,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            ) {
                Text("Créer un compte", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

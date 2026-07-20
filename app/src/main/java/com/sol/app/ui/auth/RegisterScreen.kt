package com.sol.app.ui.auth

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sol.app.R
import com.sol.app.data.InscriptionRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onInscrit: () -> Unit,
    onConnecte: () -> Unit,
    onDejaInscrit: (String) -> Unit,
    onRetour: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    val contexteRegister = LocalContext.current
    var nom by rememberSaveable { mutableStateOf("") }
    var prenom by rememberSaveable { mutableStateOf("") }
    var sexe by rememberSaveable { mutableStateOf("M") }
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var usernameDispo by remember { mutableStateOf<Boolean?>(null) }
    var usernameMsg by remember { mutableStateOf("") }
    var dateNaissance by rememberSaveable { mutableStateOf("") }
    var isoPays by rememberSaveable { mutableStateOf("HT") }
    var indicatif by rememberSaveable { mutableStateOf("+509") }
    var telephone by rememberSaveable { mutableStateOf("") }
    var motDePasse by rememberSaveable { mutableStateOf("") }
    var motDePasseVisible by rememberSaveable { mutableStateOf(false) }

    var accepteConditions by rememberSaveable { mutableStateOf(false) }
    var acceptePolitique by rememberSaveable { mutableStateOf(false) }
    var accepteVerification by rememberSaveable { mutableStateOf(false) }

    var montrerCalendrier by remember { mutableStateOf(false) }
    var montrerChoixPays by remember { mutableStateOf(false) }

    // Regles de validation : le bouton reste desactive tant que tout n'est pas bon.
    val emailValide = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val telephoneValide = telephone.filter { it.isDigit() }.length >= 8
    val formulaireValide = nom.isNotBlank() && prenom.isNotBlank() &&
        emailValide && dateNaissance.isNotBlank() && telephoneValide &&
        usernameDispo == true &&
        motDePasse.length >= 8 && accepteConditions && acceptePolitique

    // Vérification en temps réel de la disponibilité du username (anti-rebond).
    LaunchedEffect(username) {
        val u = username.trim()
        if (u.length < 4) {
            usernameDispo = null
            usernameMsg = ""
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(450)
        try {
            val r = com.sol.app.data.Network.api.usernameDisponible(u)
            usernameDispo = r.disponible
            usernameMsg = r.message
        } catch (_: Throwable) {
            usernameDispo = null
            usernameMsg = ""
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Le meme fond design que la page de connexion.
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
                .padding(horizontal = 20.dp),
        ) {
        Spacer(Modifier.height(40.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onRetour,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Créer un compte",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Text(
                    text = "Rejoignez la communauté SOL 🤝",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        ) {
        Column(modifier = Modifier.padding(20.dp)) {

        vm.erreur?.let {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(14.dp))
        }

        // ----- Identite -----
        TitreSectionAuth("👤", "Identité")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            supportingText = {
                Text("* Entrez votre nom tel qu'il apparaît sur votre passeport ou votre pièce d'identité officielle.")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = couleursChampAuth(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = prenom,
            onValueChange = { prenom = it },
            label = { Text("Prénom") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            supportingText = {
                Text("* Entrez votre prénom tel qu'il apparaît sur votre passeport ou votre pièce d'identité officielle.")
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = couleursChampAuth(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Sexe (requis par le serveur)
        Text(
            "Sexe",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            FilterChip(
                selected = sexe == "M",
                onClick = { sexe = "M" },
                label = { Text("Masculin") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
            Spacer(Modifier.width(12.dp))
            FilterChip(
                selected = sexe == "F",
                onClick = { sexe = "F" },
                label = { Text("Féminin") },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
        Spacer(Modifier.height(10.dp))

        // Date de naissance via calendrier
        OutlinedTextField(
            value = dateNaissance,
            onValueChange = { },
            readOnly = true,
            label = { Text("Date de naissance") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
            supportingText = {
                Text("* Sélectionnez votre date de naissance à l'aide du calendrier ; elle doit correspondre à votre pièce d'identité.")
            },
            trailingIcon = {
                IconButton(onClick = { montrerCalendrier = true }) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = "Ouvrir le calendrier",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = couleursChampAuth(),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        )

        // ----- Contact -----
        TitreSectionAuth("📞", "Contact")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adresse e-mail") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            isError = email.isNotBlank() && !emailValide,
            supportingText = {
                if (email.isNotBlank() && !emailValide) {
                    Text("Adresse e-mail invalide.", color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            colors = couleursChampAuth(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Username public unique (@...) avec vérification en temps réel.
        OutlinedTextField(
            value = username,
            onValueChange = { username = it.trim() },
            label = { Text("Nom d'utilisateur (@username)") },
            leadingIcon = { Text("@", fontSize = 18.sp) },
            singleLine = true,
            isError = usernameDispo == false,
            supportingText = {
                when (usernameDispo) {
                    true -> Text("✓ Username disponible", color = Color(0xFF1B8A4E))
                    false -> Text(usernameMsg.ifBlank { "Ce username est déjà utilisé." },
                        color = MaterialTheme.colorScheme.error)
                    null -> Text("4 à 20 caractères, commence par une lettre (lettres, chiffres, _ ou .).")
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = couleursChampAuth(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Telephone : indicatif pays + numero
        Text(
            "Numéro de téléphone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { montrerChoixPays = true },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(56.dp),
            ) {
                Text("${drapeauEmoji(isoPays)} $indicatif", fontSize = 16.sp)
            }
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = telephone,
                onValueChange = { telephone = it },
                label = { Text("Numéro") },
                singleLine = true,
                isError = telephone.isNotBlank() && !telephoneValide,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(14.dp),
                colors = couleursChampAuth(),
                modifier = Modifier.weight(1f),
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        )

        // ----- Securite -----
        TitreSectionAuth("🔐", "Sécurité")
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = motDePasse,
            onValueChange = { motDePasse = it },
            label = { Text("Mot de passe") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            supportingText = { Text("8 caractères minimum.") },
            singleLine = true,
            visualTransformation = if (motDePasseVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { motDePasseVisible = !motDePasseVisible }) {
                    Icon(
                        if (motDePasseVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = null,
                    )
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = couleursChampAuth(),
            modifier = Modifier.fillMaxWidth(),
        )

        // Jauge visuelle de solidite du mot de passe.
        BarreForceMotDePasse(motDePasse)

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 14.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        )

        // ----- Conditions -----
        TitreSectionAuth("✅", "Conditions")
        Spacer(Modifier.height(4.dp))
        CaseACocher(
            coche = accepteConditions,
            onChange = { accepteConditions = it },
            texte = "J'ai lu et j'accepte les Conditions d'utilisation.",
        )
        CaseACocher(
            coche = acceptePolitique,
            onChange = { acceptePolitique = it },
            texte = "J'ai lu et j'accepte la Politique de confidentialité.",
        )
        CaseACocher(
            coche = accepteVerification,
            onChange = { accepteVerification = it },
            texte = "J'accepte que mes informations personnelles soient utilisées pour " +
                "vérifier mon identité conformément à la réglementation en vigueur. (facultatif)",
        )

        Spacer(Modifier.height(20.dp))

        BoutonDegrade(
            texte = "Créer un compte",
            enabled = formulaireValide,
            enChargement = vm.enChargement,
            onClick = {
                vm.inscription(
                    InscriptionRequest(
                        nom = nom.trim(),
                        prenom = prenom.trim(),
                        sexe = sexe,
                        telephone = indicatif + telephone.filter { it.isDigit() },
                        email = email.trim(),
                        username = username.trim(),
                        adresse = "",
                        dateNaissance = dateNaissance,
                        motDePasse = motDePasse,
                    ),
                    onSucces = onInscrit,
                    onEmailDejaUtilise = { emailExistant ->
                        Toast.makeText(
                            contexteRegister,
                            "Ce compte existe déjà. Connectez-vous.",
                            Toast.LENGTH_LONG,
                        ).show()
                        onDejaInscrit(emailExistant)
                    },
                )
            },
        )

        Spacer(Modifier.height(14.dp))

        // Séparateur "ou" + connexion Google (crée le compte ou connecte).
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

        Spacer(Modifier.height(14.dp))

        BoutonGoogle(vm = vm, onConnecte = onConnecte)

        TextButton(
            onClick = onRetour,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text("Déjà un compte ? Se connecter", color = MaterialTheme.colorScheme.primary)
        }
        } // fin du contenu de la carte
        } // fin de la carte

        Spacer(Modifier.height(28.dp))
        } // fin de la colonne defilante
    } // fin du fond

    if (montrerChoixPays) {
        DialogueChoixPays(
            onChoisir = { pays ->
                isoPays = pays.iso
                indicatif = pays.indicatif
                montrerChoixPays = false
            },
            onFermer = { montrerChoixPays = false },
        )
    }

    if (montrerCalendrier) {
        val etatCalendrier = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { montrerCalendrier = false },
            confirmButton = {
                TextButton(onClick = {
                    etatCalendrier.selectedDateMillis?.let { millis ->
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        format.timeZone = TimeZone.getTimeZone("UTC")
                        dateNaissance = format.format(Date(millis))
                    }
                    montrerCalendrier = false
                }) { Text("Valider") }
            },
            dismissButton = {
                TextButton(onClick = { montrerCalendrier = false }) { Text("Annuler") }
            },
        ) {
            DatePicker(state = etatCalendrier)
        }
    }
}

@Composable
private fun DialogueChoixPays(
    onChoisir: (Pays) -> Unit,
    onFermer: () -> Unit,
) {
    var recherche by remember { mutableStateOf("") }
    val paysFiltres = LISTE_PAYS.filter {
        recherche.isBlank() ||
            it.nom.contains(recherche, ignoreCase = true) ||
            it.indicatif.contains(recherche)
    }

    AlertDialog(
        onDismissRequest = onFermer,
        shape = RoundedCornerShape(20.dp),
        title = { Text("Choisir un pays", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = recherche,
                    onValueChange = { recherche = it },
                    label = { Text("Rechercher un pays") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                LazyColumn(modifier = Modifier.height(380.dp)) {
                    items(paysFiltres) { pays ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChoisir(pays) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                        ) {
                            Text(drapeauEmoji(pays.iso), fontSize = 22.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                pays.nom,
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                            )
                            Text(
                                pays.indicatif,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onFermer) { Text("Fermer") }
        },
    )
}

/**
 * Jauge de solidite du mot de passe : 4 segments colores + libelle.
 * Rouge = faible, orange = moyen, vert = bon / excellent.
 */
@Composable
private fun BarreForceMotDePasse(motDePasse: String) {
    if (motDePasse.isEmpty()) return
    val score = forceMotDePasse(motDePasse)
    val (libelle, couleur) = when {
        score <= 1 -> "Faible" to Color(0xFFE53935)
        score == 2 -> "Moyen" to Color(0xFFFB8C00)
        score == 3 -> "Bon" to Color(0xFF43A047)
        else -> "Excellent" to Color(0xFF1B8A4E)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
    ) {
        repeat(4) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (i < score) couleur
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
            )
            if (i < 3) Spacer(Modifier.width(6.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(libelle, fontSize = 12.sp, color = couleur, fontWeight = FontWeight.SemiBold)
    }
}

/** Score de 0 a 4 : longueur, longueur forte, lettres+chiffres, caractere special. */
private fun forceMotDePasse(motDePasse: String): Int {
    var score = 0
    if (motDePasse.length >= 8) score++
    if (motDePasse.length >= 12) score++
    if (motDePasse.any { it.isDigit() } && motDePasse.any { it.isLetter() }) score++
    if (motDePasse.any { !it.isLetterOrDigit() }) score++
    return score
}

@Composable
private fun CaseACocher(
    coche: Boolean,
    onChange: (Boolean) -> Unit,
    texte: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        Checkbox(
            checked = coche,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
            ),
        )
        Text(
            text = texte,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

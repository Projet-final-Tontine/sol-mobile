package com.sol.app.ui.auth

import android.util.Patterns
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    onRetour: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    var nom by rememberSaveable { mutableStateOf("") }
    var prenom by rememberSaveable { mutableStateOf("") }
    var sexe by rememberSaveable { mutableStateOf("M") }
    var email by rememberSaveable { mutableStateOf("") }
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
        motDePasse.length >= 8 && accepteConditions && acceptePolitique

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
            IconButton(onClick = onRetour) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White,
                )
            }
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Créer un compte",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
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
        OutlinedTextField(
            value = nom,
            onValueChange = { nom = it },
            label = { Text("Nom") },
            supportingText = {
                Text("* Entrez votre nom tel qu'il apparaît sur votre passeport ou votre pièce d'identité officielle.")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = prenom,
            onValueChange = { prenom = it },
            label = { Text("Prénom") },
            supportingText = {
                Text("* Entrez votre prénom tel qu'il apparaît sur votre passeport ou votre pièce d'identité officielle.")
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
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
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Adresse e-mail") },
            singleLine = true,
            isError = email.isNotBlank() && !emailValide,
            supportingText = {
                if (email.isNotBlank() && !emailValide) {
                    Text("Adresse e-mail invalide.", color = MaterialTheme.colorScheme.error)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))

        // Date de naissance via calendrier
        OutlinedTextField(
            value = dateNaissance,
            onValueChange = { },
            readOnly = true,
            label = { Text("Date de naissance") },
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
            shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(14.dp))

        OutlinedTextField(
            value = motDePasse,
            onValueChange = { motDePasse = it },
            label = { Text("Mot de passe") },
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
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // ----- Conditions -----
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

        Button(
            onClick = {
                vm.inscription(
                    InscriptionRequest(
                        nom = nom.trim(),
                        prenom = prenom.trim(),
                        sexe = sexe,
                        telephone = indicatif + telephone.filter { it.isDigit() },
                        email = email.trim(),
                        adresse = "",
                        cinNif = "",
                        dateNaissance = dateNaissance,
                        motDePasse = motDePasse,
                    ),
                    onInscrit,
                )
            },
            enabled = formulaireValide && !vm.enChargement,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            if (vm.enChargement) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Créer un compte", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

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

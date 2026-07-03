package com.sol.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sol.app.data.InscriptionRequest

@Composable
fun RegisterScreen(
    onInscrit: () -> Unit,
    onRetour: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    var nom by rememberSaveable { mutableStateOf("") }
    var prenom by rememberSaveable { mutableStateOf("") }
    var sexe by rememberSaveable { mutableStateOf("M") }
    var telephone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var adresse by rememberSaveable { mutableStateOf("") }
    var cinNif by rememberSaveable { mutableStateOf("") }
    var dateNaissance by rememberSaveable { mutableStateOf("") }
    var motDePasse by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = "Creer un compte",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(20.dp))

        vm.erreur?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        Champ("Nom", nom) { nom = it }
        Champ("Prenom", prenom) { prenom = it }
        Champ("Sexe (M / F)", sexe) { sexe = it }
        Champ("Telephone", telephone, KeyboardType.Phone) { telephone = it }
        Champ("Email", email, KeyboardType.Email) { email = it }
        Champ("Adresse", adresse) { adresse = it }
        Champ("CIN / NIF", cinNif) { cinNif = it }
        Champ("Date de naissance (AAAA-MM-JJ)", dateNaissance) { dateNaissance = it }
        Champ("Mot de passe", motDePasse, KeyboardType.Password, motDePasse = true) { motDePasse = it }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                vm.inscription(
                    InscriptionRequest(
                        nom = nom, prenom = prenom, sexe = sexe, telephone = telephone.trim(),
                        email = email.trim(), adresse = adresse, cinNif = cinNif,
                        dateNaissance = dateNaissance, motDePasse = motDePasse,
                    ),
                    onInscrit,
                )
            },
            enabled = !vm.enChargement,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (vm.enChargement) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("S'inscrire")
            }
        }
        Spacer(Modifier.height(6.dp))
        TextButton(onClick = onRetour) {
            Text("Deja un compte ? Se connecter")
        }
    }
}

@Composable
private fun Champ(
    label: String,
    valeur: String,
    clavier: KeyboardType = KeyboardType.Text,
    motDePasse: Boolean = false,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = valeur,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = clavier),
        visualTransformation = if (motDePasse) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    )
}

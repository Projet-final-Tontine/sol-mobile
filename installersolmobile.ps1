# Script d'installation des fichiers de l'application SOL EN LIGNE (mobile)
# A executer depuis la racine du projet sol-mobile

$ErrorActionPreference = 'Stop'

# Verification qu'on est au bon endroit
if (-not (Test-Path 'settings.gradle.kts')) {
    Write-Host 'ERREUR : lancez ce script depuis la racine du projet sol-mobile.' -ForegroundColor Red
    exit 1
}

# Creation des dossiers
New-Item -ItemType Directory -Force -Path 'app/src/main/java/com/sol/app/data' | Out-Null
New-Item -ItemType Directory -Force -Path 'app/src/main/java/com/sol/app/ui/auth' | Out-Null
New-Item -ItemType Directory -Force -Path 'app/src/main/java/com/sol/app/ui/home' | Out-Null

Write-Host '[1/14] gradle/libs.versions.toml'
@'
[versions]
agp = "9.1.1"
coreKtx = "1.18.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
lifecycleRuntimeKtx = "2.8.6"
activityCompose = "1.9.3"
kotlin = "2.2.10"
composeBom = "2024.09.00"
retrofit = "2.11.0"
okhttp = "4.12.0"
coroutines = "1.8.1"
navigationCompose = "2.8.5"
lifecycleViewmodelCompose = "2.8.6"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
'@ | Set-Content -Encoding UTF8 'gradle/libs.versions.toml'

Write-Host '[2/14] app/build.gradle.kts'
@'
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sol.app"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.sol.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Reseau : appels a l'API backend
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
'@ | Set-Content -Encoding UTF8 'app/build.gradle.kts'

Write-Host '[3/14] app/src/main/AndroidManifest.xml'
@'
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".App"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:usesCleartextTraffic="true"
        android:theme="@style/Theme.Sol">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.Sol">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
'@ | Set-Content -Encoding UTF8 'app/src/main/AndroidManifest.xml'

Write-Host '[4/14] app/src/main/java/com/sol/app/MainActivity.kt'
@'
package com.sol.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sol.app.data.Session
import com.sol.app.ui.auth.LoginScreen
import com.sol.app.ui.auth.RegisterScreen
import com.sol.app.ui.home.HomeScreen
import com.sol.app.ui.theme.SolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val depart = if (Session.estConnecte) "home" else "login"

    NavHost(navController = nav, startDestination = depart) {
        composable("login") {
            LoginScreen(
                onConnecte = {
                    nav.navigate("home") { popUpTo("login") { inclusive = true } }
                },
                onAllerInscription = { nav.navigate("register") },
            )
        }
        composable("register") {
            RegisterScreen(
                onInscrit = { nav.popBackStack() },
                onRetour = { nav.popBackStack() },
            )
        }
        composable("home") {
            HomeScreen(
                onDeconnexion = {
                    nav.navigate("login") { popUpTo(0) }
                },
            )
        }
    }
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/MainActivity.kt'

Write-Host '[5/14] app/src/main/java/com/sol/app/App.kt'
@'
package com.sol.app

import android.app.Application

/** Classe Application : donne un acces global au contexte (pour la session). */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/App.kt'

Write-Host '[6/14] app/src/main/java/com/sol/app/data/Models.kt'
@'
package com.sol.app.data

// Modeles correspondant aux DTO echanges avec l'API backend.

data class InscriptionRequest(
    val nom: String,
    val prenom: String,
    val sexe: String,
    val telephone: String,
    val email: String,
    val adresse: String,
    val cinNif: String,
    val dateNaissance: String, // format AAAA-MM-JJ
    val motDePasse: String,
    val role: String = "MEMBRE",
)

data class ConnexionRequest(
    val telephone: String,
    val motDePasse: String,
)

data class UtilisateurResponse(
    val id: String,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String,
    val photoUrl: String?,
    val role: String,
    val statut: String,
)

data class ConnexionResponse(
    val token: String,
    val utilisateur: UtilisateurResponse,
)

data class SolResponse(
    val id: String,
    val nom: String,
    val description: String?,
    val codeInvitation: String,
    val nombreMaxMembres: Int,
    val montantCotisation: Double,
    val frequence: String,
    val statut: String,
    val mamanSolId: String,
)

data class RejoindreRequest(
    val codeInvitation: String,
)

data class MembreSolResponse(
    val id: String,
    val utilisateurId: String,
    val solId: String,
    val ordrePassage: Int?,
    val statutMembre: String,
)
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/data/Models.kt'

Write-Host '[7/14] app/src/main/java/com/sol/app/data/ApiService.kt'
@'
package com.sol.app.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Points d'acces de l'API backend utilises par l'application mobile. */
interface ApiService {

    @POST("api/auth/inscription")
    suspend fun inscription(@Body body: InscriptionRequest): UtilisateurResponse

    @POST("api/auth/connexion")
    suspend fun connexion(@Body body: ConnexionRequest): ConnexionResponse

    @GET("api/sols/mes-sols")
    suspend fun mesSols(): List<SolResponse>

    @POST("api/sols/rejoindre")
    suspend fun rejoindre(@Body body: RejoindreRequest): MembreSolResponse
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/data/ApiService.kt'

Write-Host '[8/14] app/src/main/java/com/sol/app/data/Session.kt'
@'
package com.sol.app.data

import android.content.Context
import com.sol.app.App

/** Stocke le jeton JWT et le nom de l'utilisateur connecte (SharedPreferences). */
object Session {

    private val prefs by lazy {
        App.instance.getSharedPreferences("sol_session", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString("token", null)
        set(value) = prefs.edit().apply {
            if (value == null) remove("token") else putString("token", value)
        }.apply()

    var nomComplet: String?
        get() = prefs.getString("nom", null)
        set(value) = prefs.edit().putString("nom", value).apply()

    val estConnecte: Boolean
        get() = token != null

    fun deconnecter() {
        prefs.edit().clear().apply()
    }
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/data/Session.kt'

Write-Host '[9/14] app/src/main/java/com/sol/app/data/Network.kt'
@'
package com.sol.app.data

import com.google.gson.JsonParser
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

/** Configuration reseau : Retrofit + jeton JWT automatique. */
object Network {

    // 10.0.2.2 correspond au "localhost" de la machine hote vu depuis l'emulateur Android.
    // A remplacer par l'adresse IP du serveur lors d'un test sur telephone reel.
    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val authInterceptor = Interceptor { chain ->
        val requete = chain.request().newBuilder()
        Session.token?.let { requete.addHeader("Authorization", "Bearer $it") }
        chain.proceed(requete.build())
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        )
        .build()

    val api: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}

/** Transforme une exception reseau/metier en message lisible pour l'utilisateur. */
fun messageErreur(e: Throwable): String = when (e) {
    is HttpException -> {
        val corps = e.response()?.errorBody()?.string()
        try {
            JsonParser.parseString(corps).asJsonObject.get("message").asString
        } catch (_: Exception) {
            "Erreur ${e.code()}"
        }
    }
    is IOException -> "Impossible de joindre le serveur. Verifiez votre connexion."
    else -> e.message ?: "Une erreur est survenue."
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/data/Network.kt'

Write-Host '[10/14] app/src/main/java/com/sol/app/ui/auth/AuthViewModel.kt'
@'
package com.sol.app.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.ConnexionRequest
import com.sol.app.data.InscriptionRequest
import com.sol.app.data.Network
import com.sol.app.data.Session
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch

/** Gere la connexion et l'inscription. */
class AuthViewModel : ViewModel() {

    var enChargement by mutableStateOf(false)
        private set
    var erreur by mutableStateOf<String?>(null)
        private set

    fun connexion(telephone: String, motDePasse: String, onSucces: () -> Unit) {
        if (telephone.isBlank() || motDePasse.isBlank()) {
            erreur = "Veuillez remplir tous les champs."
            return
        }
        lancer {
            val rep = Network.api.connexion(ConnexionRequest(telephone.trim(), motDePasse))
            Session.token = rep.token
            Session.nomComplet = "${rep.utilisateur.prenom} ${rep.utilisateur.nom}"
            onSucces()
        }
    }

    fun inscription(req: InscriptionRequest, onSucces: () -> Unit) {
        if (req.nom.isBlank() || req.prenom.isBlank() || req.telephone.isBlank() ||
            req.email.isBlank() || req.motDePasse.isBlank()
        ) {
            erreur = "Veuillez remplir les champs obligatoires."
            return
        }
        lancer {
            Network.api.inscription(req)
            onSucces()
        }
    }

    fun effacerErreur() {
        erreur = null
    }

    private fun lancer(bloc: suspend () -> Unit) {
        erreur = null
        enChargement = true
        viewModelScope.launch {
            try {
                bloc()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enChargement = false
            }
        }
    }
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/ui/auth/AuthViewModel.kt'

Write-Host '[11/14] app/src/main/java/com/sol/app/ui/auth/LoginScreen.kt'
@'
package com.sol.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onConnecte: () -> Unit,
    onAllerInscription: () -> Unit,
    vm: AuthViewModel = viewModel(),
) {
    var telephone by rememberSaveable { mutableStateOf("") }
    var motDePasse by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "SOL EN LIGNE",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Connectez-vous a votre compte",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))

        vm.erreur?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        OutlinedTextField(
            value = telephone,
            onValueChange = { telephone = it },
            label = { Text("Telephone") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = motDePasse,
            onValueChange = { motDePasse = it },
            label = { Text("Mot de passe") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { vm.connexion(telephone, motDePasse, onConnecte) },
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
                Text("Se connecter")
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onAllerInscription) {
            Text("Pas encore de compte ? S'inscrire")
        }
    }
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/ui/auth/LoginScreen.kt'

Write-Host '[12/14] app/src/main/java/com/sol/app/ui/auth/RegisterScreen.kt'
@'
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
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/ui/auth/RegisterScreen.kt'

Write-Host '[13/14] app/src/main/java/com/sol/app/ui/home/HomeViewModel.kt'
@'
package com.sol.app.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sol.app.data.Network
import com.sol.app.data.RejoindreRequest
import com.sol.app.data.SolResponse
import com.sol.app.data.messageErreur
import kotlinx.coroutines.launch

/** Gere l'ecran d'accueil du membre : liste de ses Sols et adhesion par code. */
class HomeViewModel : ViewModel() {

    var sols by mutableStateOf<List<SolResponse>>(emptyList())
        private set
    var enChargement by mutableStateOf(false)
        private set
    var erreur by mutableStateOf<String?>(null)
        private set
    var messageSucces by mutableStateOf<String?>(null)
        private set

    fun chargerMesSols() {
        erreur = null
        enChargement = true
        viewModelScope.launch {
            try {
                sols = Network.api.mesSols()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                enChargement = false
            }
        }
    }

    fun rejoindre(code: String, onTermine: () -> Unit) {
        if (code.isBlank()) {
            erreur = "Veuillez saisir un code d'invitation."
            return
        }
        erreur = null
        messageSucces = null
        viewModelScope.launch {
            try {
                Network.api.rejoindre(RejoindreRequest(code.trim().uppercase()))
                messageSucces = "Vous avez rejoint le Sol avec succes !"
                chargerMesSols()
            } catch (e: Throwable) {
                erreur = messageErreur(e)
            } finally {
                onTermine()
            }
        }
    }

    fun effacerMessages() {
        erreur = null
        messageSucces = null
    }
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/ui/home/HomeViewModel.kt'

Write-Host '[14/14] app/src/main/java/com/sol/app/ui/home/HomeScreen.kt'
@'
package com.sol.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sol.app.data.Session
import com.sol.app.data.SolResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onDeconnexion: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    var dialogueOuvert by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.chargerMesSols() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bonjour, ${Session.nomComplet ?: "Membre"}") },
                actions = {
                    TextButton(onClick = {
                        Session.deconnecter()
                        onDeconnexion()
                    }) { Text("Deconnexion") }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Rejoindre un Sol") },
                icon = { Text("+") },
                onClick = { dialogueOuvert = true },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Mes Sols",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))

            vm.messageSucces?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
            }
            vm.erreur?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp))
            }

            when {
                vm.enChargement -> Box_Centre { CircularProgressIndicator() }
                vm.sols.isEmpty() -> Box_Centre {
                    Text(
                        "Vous ne participez a aucun Sol pour le moment.\nRejoignez-en un avec un code d'invitation.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(vm.sols) { sol -> CarteSol(sol) }
                }
            }
        }
    }

    if (dialogueOuvert) {
        DialogueRejoindre(
            enCours = false,
            onValider = { code ->
                vm.rejoindre(code) { dialogueOuvert = false }
            },
            onAnnuler = { dialogueOuvert = false },
        )
    }
}

@Composable
private fun Box_Centre(contenu: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { contenu() }
}

@Composable
private fun CarteSol(sol: SolResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(sol.nom, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sol.statut, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(6.dp))
            Text("Cotisation : ${sol.montantCotisation} HTG - ${sol.frequence}")
            Text("Places : ${sol.nombreMaxMembres}")
            Text("Code : ${sol.codeInvitation}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DialogueRejoindre(
    enCours: Boolean,
    onValider: (String) -> Unit,
    onAnnuler: () -> Unit,
) {
    var code by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAnnuler,
        title = { Text("Rejoindre un Sol") },
        text = {
            Column {
                Text("Saisissez le code d'invitation partage par la Manman sol.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Code d'invitation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onValider(code) }, enabled = !enCours) { Text("Rejoindre") }
        },
        dismissButton = {
            TextButton(onClick = onAnnuler) { Text("Annuler") }
        },
    )
}
'@ | Set-Content -Encoding UTF8 'app/src/main/java/com/sol/app/ui/home/HomeScreen.kt'

Write-Host ''
Write-Host 'TERMINE : 14 fichiers installes avec succes !' -ForegroundColor Green
Write-Host 'Ouvrez Android Studio puis : Sync Now, ensuite Build > Make Project.' -ForegroundColor Green
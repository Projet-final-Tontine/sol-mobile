package com.sol.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sol.app.data.Session
import com.sol.app.ui.EcranVerrou
import com.sol.app.ui.auth.LoginScreen
import com.sol.app.ui.auth.RegisterScreen
import com.sol.app.ui.auth.WelcomeScreen
import com.sol.app.ui.home.HomeScreen
import com.sol.app.ui.theme.SolTheme

// FragmentActivity (au lieu de ComponentActivity) : requis par BiometricPrompt.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolTheme {
                // Verrou a l'ouverture si l'utilisateur l'a active et est connecte.
                var deverrouille by rememberSaveable {
                    mutableStateOf(!(Session.estConnecte && Session.verrouillageActif))
                }
                if (deverrouille) {
                    AppNavigation()
                } else {
                    EcranVerrou(onDeverrouille = { deverrouille = true })
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val depart = if (Session.estConnecte) "home" else "welcome"

    NavHost(navController = nav, startDestination = depart) {
        composable("welcome") {
            WelcomeScreen(
                onConnexion = { nav.navigate("login") },
                onInscription = { nav.navigate("register") },
            )
        }
        composable("login") {
            LoginScreen(
                onConnecte = {
                    nav.navigate("home") { popUpTo(0) }
                },
                onAllerInscription = { nav.navigate("register") },
            )
        }
        composable("register") {
            RegisterScreen(
                onInscrit = {
                    nav.navigate("login") { popUpTo("welcome") }
                },
                onRetour = { nav.popBackStack() },
            )
        }
        composable("home") {
            HomeScreen(
                onDeconnexion = {
                    nav.navigate("welcome") { popUpTo(0) }
                },
            )
        }
    }
}

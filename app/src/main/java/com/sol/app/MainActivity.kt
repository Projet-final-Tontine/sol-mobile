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
import com.sol.app.ui.auth.WelcomeScreen
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

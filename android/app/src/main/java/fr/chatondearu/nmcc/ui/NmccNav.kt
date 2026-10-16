package fr.chatondearu.nmcc.ui

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.chatondearu.nmcc.ui.screens.HomeScreen
import fr.chatondearu.nmcc.ui.screens.LoginScreen
import fr.chatondearu.nmcc.ui.screens.RulesScreen
import fr.chatondearu.nmcc.ui.screens.SettingsScreen
import fr.chatondearu.nmcc.ui.screens.StatsScreen

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
fun NmccNav(activity: Activity) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = route in setOf("home", "rules", "stats", "settings")

    val tabs = listOf(
        Tab("home", "Accueil", Icons.Filled.Home),
        Tab("rules", "Règles", Icons.AutoMirrored.Filled.Rule),
        Tab("stats", "Statistiques", Icons.Filled.BarChart),
        Tab("settings", "Paramètres", Icons.Filled.Settings),
    )

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = { nav.navigate(tab.route) { launchSingleTop = true } },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding),
        ) {
            composable("login") {
                LoginScreen(
                    onLoggedIn = {
                        nav.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                )
            }
            composable("home") { HomeScreen(activity = activity) }
            composable("rules") { RulesScreen() }
            composable("stats") { StatsScreen() }
            composable("settings") {
                SettingsScreen(
                    onOpenLogin = { nav.navigate("login") },
                    onSignedOut = {
                        nav.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

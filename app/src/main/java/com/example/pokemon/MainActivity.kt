package com.example.pokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("splash") }

    LaunchedEffect(Unit) {
        delay(2000)
        currentScreen = "main_menu"
    }

    when (currentScreen) {
        "splash" -> SplashScreen()
        "main_menu" -> MainMenuScreen(
            onNewGame = { currentScreen = "starter_selection" },
            onContinue = { /* пізніше */ }
        )
        "starter_selection" -> StarterSelectionScreen(
            onStarterSelected = { currentScreen = "battle" }
        )
        "battle" -> BattleScreen()
    }
}

@Composable
fun SplashScreen() {
    // TODO
}

@Composable
fun MainMenuScreen(onNewGame: () -> Unit, onContinue: () -> Unit) {
    // TODO
}

@Composable
fun StarterSelectionScreen(onStarterSelected: () -> Unit) {
    // TODO
}

@Composable
fun BattleScreen() {
    // TODO
}
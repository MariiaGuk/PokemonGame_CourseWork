package com.example.pokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.pokemon.logic.pokemons.PokemonSpecies
import com.example.pokemon.ui.screens.BattleScreen
import com.example.pokemon.ui.screens.MainMenuScreen
import com.example.pokemon.ui.screens.SplashScreen
import com.example.pokemon.ui.screens.StarterSelectionScreen
import com.example.pokemon.ui.theme.ChimeralisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            ChimeralisTheme {
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
    var selectedStarter by remember { mutableStateOf<PokemonSpecies?>(null) }

    when (currentScreen) {
        "splash" -> SplashScreen(onFinished = { currentScreen = "main_menu" })
        "main_menu" -> MainMenuScreen(
            onNewGame = { currentScreen = "starter_selection" },
            onContinue = { /* пізніше */ }
        )
        "starter_selection" -> StarterSelectionScreen(
            onStarterSelected = { starter ->
                selectedStarter = starter
                currentScreen = "battle"
            },
            onBack = { currentScreen = "main_menu" }
        )
        "battle" -> BattleScreen()
    }
}

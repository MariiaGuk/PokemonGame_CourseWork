package com.example.chimeralis

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
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.screens.BattleScreen
import com.example.chimeralis.ui.screens.MainMenuScreen
import com.example.chimeralis.ui.screens.SplashScreen
import com.example.chimeralis.ui.screens.StarterSelectionScreen
import com.example.chimeralis.ui.screens.TrainerNameScreen
import com.example.chimeralis.ui.screens.WorldScreen
import com.example.chimeralis.ui.theme.ChimeralisTheme

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
                    AppNavigation(onExitGame = ::finish)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(onExitGame: () -> Unit) {
    var currentScreen by remember { mutableStateOf("splash") }
    var selectedStarter by remember { mutableStateOf<ChimeraSpecies?>(null) }
    var trainerName by remember { mutableStateOf("") }
    var wildEncounter by remember { mutableStateOf<ChimeraSpecies?>(null) }

    when (currentScreen) {
        "splash" -> SplashScreen(onFinished = { currentScreen = "main_menu" })
        "main_menu" -> MainMenuScreen(
            onNewGame = { currentScreen = "trainer_name" },
            onContinue = { /* пізніше */ }
        )
        "trainer_name" -> TrainerNameScreen(
            onNameConfirmed = { name ->
                trainerName = name
                currentScreen = "starter_selection"
            },
            onBack = { currentScreen = "main_menu" }
        )
        "starter_selection" -> StarterSelectionScreen(
            onStarterSelected = { starter ->
                selectedStarter = starter
                currentScreen = "world"
            },
            onBack = { currentScreen = "trainer_name" }
        )
        "world" -> WorldScreen(
            starter = selectedStarter,
            onBackToMainMenu = { currentScreen = "main_menu" },
            onExitGame = onExitGame,
            onWildEncounter = { wildSpecies ->
                wildEncounter = wildSpecies
                currentScreen = "battle"
            }
        )
        "battle" -> BattleScreen(
            playerSpecies = selectedStarter,
            wildSpecies = wildEncounter ?: ChimeraSpecies.Sylvhorn,
            onRun = { currentScreen = "world" }
        )
    }
}

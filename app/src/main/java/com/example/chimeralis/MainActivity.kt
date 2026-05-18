package com.example.chimeralis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chimeralis.data.GameSave
import com.example.chimeralis.data.GameSaveStore
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.screens.BattleScreen
import com.example.chimeralis.ui.screens.ContinueScreen
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
    val context = LocalContext.current
    val saveStore = remember(context) { GameSaveStore(context.applicationContext) }

    var currentScreen by remember { mutableStateOf("splash") }
    var selectedStarter by remember { mutableStateOf<ChimeraSpecies?>(null) }
    var starterNickname by remember { mutableStateOf("") }
    var trainerName by remember { mutableStateOf("") }
    var playerColumn by remember { mutableIntStateOf(1) }
    var playerRow by remember { mutableIntStateOf(1) }
    var lastSavedColumn by remember { mutableIntStateOf(1) }
    var lastSavedRow by remember { mutableIntStateOf(1) }
    var trainerNameError by remember { mutableStateOf<String?>(null) }
    var wildEncounter by remember { mutableStateOf<ChimeraSpecies?>(null) }
    var saves by remember { mutableStateOf(saveStore.loadAll()) }
    val hasUnsavedChanges = playerColumn != lastSavedColumn || playerRow != lastSavedRow

    fun saveCurrentGame(column: Int = playerColumn, row: Int = playerRow) {
        val starter = selectedStarter ?: return
        if (trainerName.isBlank()) return

        saveStore.save(
            GameSave(
                trainerName = trainerName,
                starterSpecies = starter,
                starterNickname = starterNickname.ifBlank { starter.battleName() },
                playerColumn = column,
                playerRow = row,
                updatedAt = System.currentTimeMillis()
            )
        )
        lastSavedColumn = column
        lastSavedRow = row
        saves = saveStore.loadAll()
    }

    when (currentScreen) {
        "splash" -> SplashScreen(onFinished = { currentScreen = "main_menu" })
        "main_menu" -> MainMenuScreen(
            onNewGame = {
                trainerName = ""
                trainerNameError = null
                selectedStarter = null
                starterNickname = ""
                playerColumn = 1
                playerRow = 1
                lastSavedColumn = 1
                lastSavedRow = 1
                currentScreen = "trainer_name"
            },
            onContinue = {
                saves = saveStore.loadAll()
                currentScreen = "continue"
            },
            onExitGame = onExitGame
        )
        "continue" -> ContinueScreen(
            saves = saves,
            onLoad = { save ->
                trainerName = save.trainerName
                selectedStarter = save.starterSpecies
                starterNickname = save.starterNickname
                playerColumn = save.playerColumn
                playerRow = save.playerRow
                lastSavedColumn = save.playerColumn
                lastSavedRow = save.playerRow
                wildEncounter = null
                currentScreen = "world"
            },
            onDelete = { save ->
                saveStore.delete(save.trainerName)
                saves = saveStore.loadAll()
            },
            onBack = { currentScreen = "main_menu" }
        )
        "trainer_name" -> TrainerNameScreen(
            onNameConfirmed = { name ->
                if (saveStore.hasSaveForTrainer(name)) {
                    trainerNameError = "A trainer with this name already exists."
                } else {
                    trainerName = name
                    trainerNameError = null
                    currentScreen = "starter_selection"
                }
            },
            onBack = {
                trainerNameError = null
                currentScreen = "main_menu"
            },
            errorMessage = trainerNameError,
            onNameEdited = { trainerNameError = null }
        )
        "starter_selection" -> StarterSelectionScreen(
            onStarterSelected = { starter, nickname ->
                selectedStarter = starter
                starterNickname = nickname
                playerColumn = 1
                playerRow = 1
                lastSavedColumn = 1
                lastSavedRow = 1
                saveCurrentGame(column = 1, row = 1)
                currentScreen = "world"
            },
            onBack = { currentScreen = "trainer_name" }
        )
        "world" -> WorldScreen(
            starter = selectedStarter,
            initialPlayerColumn = playerColumn,
            initialPlayerRow = playerRow,
            hasUnsavedChanges = hasUnsavedChanges,
            onPlayerPositionChanged = { column, row ->
                playerColumn = column
                playerRow = row
            },
            onSaveGame = { column, row ->
                playerColumn = column
                playerRow = row
                saveCurrentGame(column, row)
            },
            onBackToMainMenu = {
                currentScreen = "main_menu"
            },
            onExitGame = onExitGame,
            onWildEncounter = { wildSpecies ->
                wildEncounter = wildSpecies
                currentScreen = "battle"
            }
        )
        "battle" -> BattleScreen(
            playerSpecies = selectedStarter,
            playerName = starterNickname.ifBlank { null },
            wildSpecies = wildEncounter ?: ChimeraSpecies.Sylvhorn,
            onRun = { currentScreen = "world" }
        )
    }
}

private fun ChimeraSpecies.battleName(): String = when (this) {
    ChimeraSpecies.Sunflare -> "Sunflare"
    ChimeraSpecies.Solflare -> "Solflare"
    ChimeraSpecies.Solignis -> "Solignis"
    ChimeraSpecies.Sylvhorn -> "Sylvhorn"
    ChimeraSpecies.Aquantis -> "Aquantis"
}

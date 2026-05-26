package com.example.chimeralis.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.chimeralis.audio.GameMusic
import com.example.chimeralis.audio.GameSoundEffects
import com.example.chimeralis.data.GameSaveStore
import com.example.chimeralis.ui.overlays.LocationTransitionOverlay
import com.example.chimeralis.ui.screens.onboarding.SplashScreen
import com.example.chimeralis.ui.screens.world.WorldField
import com.example.chimeralis.ui.screens.world.locations.TownInterior

@Composable
fun AppNavigation(onExitGame: () -> Unit) {
    val context = LocalContext.current
    val saveStore = remember(context) { GameSaveStore(context.applicationContext) }
    val session = rememberGameSessionState(saveStore)
    val transition = rememberGameTransitionState()

    with(session) {
        LaunchedEffect(currentScreen, isScreenTransitionRunning) {
            if (!isScreenTransitionRunning) {
                musicScreen = currentScreen
            }
        }

        GameMusic(
            currentScreen = musicScreen,
            enabled = musicEnabled && !isBattleResultMusicSuppressed,
            volume = musicVolume
        )
        GameSoundEffects(
            enabled = soundEnabled,
            volume = soundVolume
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    GameScreen.Splash -> SplashScreen(
                        onFinished = { currentScreen = GameScreen.MainMenu }
                    )
                    GameScreen.MainMenu -> MainMenuRoute(onExitGame)
                    GameScreen.Continue -> ContinueRoute()
                    GameScreen.TrainerName -> TrainerNameRoute()
                    GameScreen.StarterSelection -> StarterSelectionRoute()
                    GameScreen.LavaField -> WorldRoute(
                        screen = GameScreen.LavaField,
                        field = WorldField.Lava,
                        transition = transition,
                        context = context,
                        onExitGame = onExitGame
                    )
                    GameScreen.GrassField -> WorldRoute(
                        screen = GameScreen.GrassField,
                        field = WorldField.Grass,
                        transition = transition,
                        context = context,
                        onExitGame = onExitGame
                    )
                    GameScreen.ChimeraCenterInterior -> TownInteriorRoute(
                        interior = TownInterior.ChimeraCenter,
                        transition = transition,
                        context = context,
                        onExitGame = onExitGame
                    )
                    GameScreen.ChimeraStoreInterior -> TownInteriorRoute(
                        interior = TownInterior.ChimeraStore,
                        transition = transition,
                        context = context,
                        onExitGame = onExitGame
                    )
                    GameScreen.Battle -> BattleRoute(transition)
                }
            }

            LocationTransitionOverlay(alpha = transition.whiteAlpha.value)
        }
    }
}

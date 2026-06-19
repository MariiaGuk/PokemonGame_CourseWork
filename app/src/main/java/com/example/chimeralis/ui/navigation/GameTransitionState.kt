package com.example.chimeralis.ui.navigation

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.R
import com.example.chimeralis.ui.navigation.session.GameSessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Represents the game transition state. */
class GameTransitionState(
    private val context: Context,
    private val scope: CoroutineScope
) {
    val whiteAlpha = Animatable(0f)
    val battleZoomScale = Animatable(1f)

    /**
     * Handles transition to behavior.
     *
     * @param screen The screen value used by this operation.
     * @param session The session value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun transitionTo(
        screen: GameScreen,
        session: GameSessionState
    ) {
        if (session.isScreenTransitionRunning) return

        scope.launch {
            val isLocationTransition = session.currentScreen.isLocation && screen.isLocation
            session.isScreenTransitionRunning = true
            session.worldInputLockKey++
            if (isLocationTransition) {
                GameSoundPlayer.play(context, R.raw.start_transition)
            }

            if (screen == GameScreen.Battle) {
                session.musicScreen = GameScreen.Battle
                delay(420)
                battleZoomScale.animateTo(
                    targetValue = 1.13f,
                    animationSpec = tween(durationMillis = 105)
                )
                battleZoomScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 90)
                )
                val whiteFlash = launch {
                    whiteAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 980)
                    )
                }
                battleZoomScale.animateTo(
                    targetValue = 1.8f,
                    animationSpec = tween(durationMillis = 650)
                )
                whiteFlash.join()
            } else {
                whiteAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = if (isLocationTransition) {
                            LocationTransitionCoverMillis
                        } else {
                            DefaultTransitionCoverMillis
                        }
                    )
                )
            }

            if (screen != GameScreen.Battle) {
                session.musicScreen = screen
            }
            session.currentScreen = screen
            battleZoomScale.snapTo(1f)
            if (isLocationTransition) {
                GameSoundPlayer.play(context, R.raw.end_transition)
            }
            whiteAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = if (isLocationTransition) {
                        LocationTransitionRevealMillis
                    } else {
                        DefaultTransitionRevealMillis
                    }
                )
            )
            if (screen != GameScreen.Battle) {
                GameSoundPlayer.stopBattleResultSounds()
                session.isBattleResultMusicSuppressed = false
            }
            session.isScreenTransitionRunning = false
        }
    }
}

private const val LocationTransitionCoverMillis = 2320
private const val LocationTransitionRevealMillis = 2000
private const val DefaultTransitionCoverMillis = 980
private const val DefaultTransitionRevealMillis = 1180

/**
 * Remembers the remember game transition state state.
 *
 * @return The resulting GameTransitionState value.
 */
@Composable
fun rememberGameTransitionState(): GameTransitionState {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    return remember(context, scope) {
        GameTransitionState(
            context = context.applicationContext,
            scope = scope
        )
    }
}

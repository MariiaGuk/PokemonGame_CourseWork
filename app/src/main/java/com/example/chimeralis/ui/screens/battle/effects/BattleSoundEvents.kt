package com.example.chimeralis.ui.screens.battle.effects

import android.content.Context
import androidx.annotation.RawRes
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.R
import com.example.chimeralis.ui.screens.battle.components.EvolutionRevealMillis
import kotlinx.coroutines.delay

/** Maps battle log messages and scripted battle moments to sound playback. */
internal class BattleSoundEventHandler(
    private val context: Context,
    private val onBattleResultSoundStarted: () -> Unit
) {

    /**
     * Plays the raw sound emitted by animation playback.
     *
     * @param soundResId The sound res id value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun playAnimationSound(@RawRes soundResId: Int) {
        GameSoundPlayer.play(context, soundResId)
    }

    /**
     * Plays the sound associated with one battle log message, if any.
     *
     * @param message The message value used by this operation.
     * @param isLevelUpMessage Flag that controls or describes is level up message.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    fun playLogMessageSound(message: String, isLevelUpMessage: Boolean) {
        battleSoundEventFor(message, isLevelUpMessage)?.let(::playEvent)
    }

    /**
     * Plays the evolution reveal sound sequence without changing evolution state.
     *
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    suspend fun playEvolutionRevealSounds() {
        GameSoundPlayer.stopBattleResultSounds()
        GameSoundPlayer.play(context, R.raw.chimera_evolution)
        delay(EvolutionRevealMillis)
        GameSoundPlayer.stop(R.raw.chimera_evolution)
        GameSoundPlayer.play(context, R.raw.chimera_evolved)
    }

    /**
     * Plays one mapped battle sound event.
     *
     * @param event The event value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    private fun playEvent(event: BattleSoundEvent) {
        if (event.startsBattleResult) {
            onBattleResultSoundStarted()
        }
        GameSoundPlayer.play(context, event.soundResId)
    }
}

/**
 * Returns the sound event mapped to a battle log message.
 *
 * @param message The message value used by this operation.
 * @param isLevelUpMessage Flag that controls or describes is level up message.
 * @return The resolved battle sound event value, or null when it is unavailable.
 */
private fun battleSoundEventFor(
    message: String,
    isLevelUpMessage: Boolean
): BattleSoundEvent? {
    return when {
        message == "Got away safely!" -> BattleSoundEvent(R.raw.ran_away)
        message == "You won!" -> BattleSoundEvent(
            soundResId = R.raw.battle_victory,
            startsBattleResult = true
        )
        message == "You lost!" -> BattleSoundEvent(
            soundResId = R.raw.battle_loss,
            startsBattleResult = true
        )
        message.startsWith("Gotcha!") && message.endsWith("was caught!") -> BattleSoundEvent(
            soundResId = R.raw.caught_a_chimera,
            startsBattleResult = true
        )
        isLevelUpMessage -> BattleSoundEvent(R.raw.level_up)
        else -> null
    }
}

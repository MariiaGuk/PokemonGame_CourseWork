package com.example.chimeralis.ui.screens.battle.effects

import android.content.Context
import androidx.annotation.RawRes
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.R
import com.example.chimeralis.ui.screens.battle.components.EvolutionRevealMillis
import kotlinx.coroutines.delay

/** Describes one sound that should be played for a battle event. */
internal data class BattleSoundEvent(
    @param:RawRes val soundResId: Int,
    val startsBattleResult: Boolean = false
)

/** Maps battle log messages and scripted battle moments to sound playback. */
internal class BattleSoundEventHandler(
    private val context: Context,
    private val onBattleResultSoundStarted: () -> Unit
) {

    /** Plays the raw sound emitted by animation playback. */
    fun playAnimationSound(@RawRes soundResId: Int) {
        GameSoundPlayer.play(context, soundResId)
    }

    /** Plays the sound associated with one battle log message, if any. */
    fun playLogMessageSound(message: String, isLevelUpMessage: Boolean) {
        battleSoundEventFor(message, isLevelUpMessage)?.let(::playEvent)
    }

    /** Plays the evolution reveal sound sequence without changing evolution state. */
    suspend fun playEvolutionRevealSounds() {
        GameSoundPlayer.stopBattleResultSounds()
        GameSoundPlayer.play(context, R.raw.chimera_evolution)
        delay(EvolutionRevealMillis)
        GameSoundPlayer.stop(R.raw.chimera_evolution)
        GameSoundPlayer.play(context, R.raw.chimera_evolved)
    }

    /** Plays one mapped battle sound event. */
    private fun playEvent(event: BattleSoundEvent) {
        if (event.startsBattleResult) {
            onBattleResultSoundStarted()
        }
        GameSoundPlayer.play(context, event.soundResId)
    }
}

/** Returns the sound event mapped to a battle log message. */
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

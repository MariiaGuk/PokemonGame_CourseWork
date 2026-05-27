package com.example.chimeralis.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.chimeralis.R

/** Provides game sound player behavior. */
object GameSoundPlayer {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<Int, Int>()
    private val activeStreams = mutableMapOf<Int, MutableList<Int>>()
    private val loadedSounds = mutableSetOf<Int>()
    private val pendingSounds = mutableSetOf<Int>()
    private var isEnabled: Boolean = true
    private var volume: Float = 1f

    /** Handles initialize behavior. */
    fun initialize(context: Context) {
        if (soundPool != null) return

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
            .also { pool ->
                pool.setOnLoadCompleteListener { loadedPool, sampleId, status ->
                    if (status != 0) return@setOnLoadCompleteListener
                    loadedSounds.add(sampleId)
                    if (isEnabled && pendingSounds.remove(sampleId)) {
                        loadedPool.play(sampleId, volume, volume, 1, 0, 1f)
                    }
                }
            }

        preload(context, R.raw.button_click)
        preload(context, R.raw.save_game)
        preload(context, R.raw.ran_away)
        preload(context, R.raw.level_up)
        preload(context, R.raw.attack_sound)
        preload(context, R.raw.chimera_faint)
        preload(context, R.raw.battle_loss)
        preload(context, R.raw.battle_victory)
        preload(context, R.raw.caught_a_chimera)
        preload(context, R.raw.start_transition)
        preload(context, R.raw.end_transition)
    }

    /** Handles play behavior. */
    fun play(context: Context, @RawRes soundResId: Int, force: Boolean = false) {
        if ((!isEnabled && !force) || volume <= 0f) return

        initialize(context.applicationContext)

        val pool = soundPool ?: return
        val soundId = soundIds[soundResId] ?: preload(context, soundResId)

        if (soundId in loadedSounds) {
            val streamId = pool.play(soundId, volume, volume, 1, 0, 1f)
            activeStreams.getOrPut(soundResId) { mutableListOf() }.add(streamId)
        } else {
            pendingSounds.add(soundId)
        }
    }

    /** Handles stop behavior. */
    fun stop(@RawRes soundResId: Int) {
        val pool = soundPool ?: return
        activeStreams.remove(soundResId)?.forEach(pool::stop)
    }

    /** Handles stop battle result sounds behavior. */
    fun stopBattleResultSounds() {
        stop(R.raw.battle_victory)
        stop(R.raw.battle_loss)
        stop(R.raw.caught_a_chimera)
    }

    /** Handles configure behavior. */
    fun configure(enabled: Boolean, soundVolume: Float) {
        isEnabled = enabled
        volume = soundVolume.coerceIn(0f, 1f)
        if (!isEnabled) {
            pendingSounds.clear()
        }
    }

    /** Handles release behavior. */
    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        activeStreams.clear()
        loadedSounds.clear()
        pendingSounds.clear()
    }

    /** Handles preload behavior. */
    private fun preload(context: Context, @RawRes soundResId: Int): Int {
        val existingId = soundIds[soundResId]
        if (existingId != null) return existingId

        val soundId = soundPool?.load(context.applicationContext, soundResId, 1) ?: 0
        soundIds[soundResId] = soundId
        return soundId
    }
}

/** Renders the game sound effects UI. */
@Composable
fun GameSoundEffects(
    enabled: Boolean = true,
    volume: Float = 1f
) {
    val context = LocalContext.current

    DisposableEffect(enabled, volume) {
        GameSoundPlayer.configure(enabled, volume)

        onDispose {}
    }

    DisposableEffect(Unit) {
        GameSoundPlayer.initialize(context.applicationContext)

        onDispose {
            GameSoundPlayer.release()
        }
    }
}

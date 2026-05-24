package com.example.chimeralis.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.chimeralis.R

object GameSoundPlayer {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<Int, Int>()
    private val loadedSounds = mutableSetOf<Int>()
    private val pendingSounds = mutableSetOf<Int>()

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
                    if (pendingSounds.remove(sampleId)) {
                        loadedPool.play(sampleId, 1f, 1f, 1, 0, 1f)
                    }
                }
            }

        preload(context, R.raw.button_click)
        preload(context, R.raw.save_game)
        preload(context, R.raw.ran_away)
        preload(context, R.raw.level_up)
        preload(context, R.raw.attack_sound)
        preload(context, R.raw.dying_sound)
    }

    fun play(context: Context, @RawRes soundResId: Int) {
        initialize(context.applicationContext)

        val pool = soundPool ?: return
        val soundId = soundIds[soundResId] ?: preload(context, soundResId)

        if (soundId in loadedSounds) {
            pool.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            pendingSounds.add(soundId)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        loadedSounds.clear()
        pendingSounds.clear()
    }

    private fun preload(context: Context, @RawRes soundResId: Int): Int {
        val existingId = soundIds[soundResId]
        if (existingId != null) return existingId

        val soundId = soundPool?.load(context.applicationContext, soundResId, 1) ?: 0
        soundIds[soundResId] = soundId
        return soundId
    }
}

@Composable
fun GameSoundEffects() {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        GameSoundPlayer.initialize(context.applicationContext)

        onDispose {
            GameSoundPlayer.release()
        }
    }
}

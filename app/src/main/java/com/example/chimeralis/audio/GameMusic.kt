package com.example.chimeralis.audio

import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.chimeralis.R
import com.example.chimeralis.ui.navigation.GameScreen

/** Plays the looping music for the active screen. */
@Composable
fun GameMusic(
    currentScreen: GameScreen,
    enabled: Boolean,
    volume: Float
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val musicResId = currentScreen.musicResId()
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(musicResId, enabled) {
        if (!enabled) {
            mediaPlayer = null
            onDispose {}
        } else {
            val player = MediaPlayer.create(context.applicationContext, musicResId).apply {
                isLooping = true
                setVolume(volume, volume)
                start()
            }

            mediaPlayer = player

            onDispose {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
                if (mediaPlayer === player) {
                    mediaPlayer = null
                }
            }
        }
    }

    LaunchedEffect(mediaPlayer, enabled, volume) {
        mediaPlayer?.setVolume(
            if (enabled) volume.coerceIn(0f, 1f) else 0f,
            if (enabled) volume.coerceIn(0f, 1f) else 0f
        )
    }

    DisposableEffect(lifecycleOwner, mediaPlayer, enabled) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> mediaPlayer?.pause()
                Lifecycle.Event.ON_RESUME -> if (enabled) mediaPlayer?.start()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

/** Selects the music resource for a screen. */
private fun GameScreen.musicResId(): Int {
    return when (this) {
        GameScreen.LavaField -> R.raw.lava_field_theme
        GameScreen.GrassField -> R.raw.grass_field_theme
        GameScreen.ChimeraCenterInterior,
        GameScreen.ChimeraStoreInterior -> R.raw.chimera_center_theme
        GameScreen.Battle -> R.raw.battle_theme
        else -> R.raw.main_menu_theme
    }
}

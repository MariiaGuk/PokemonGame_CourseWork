package com.example.chimeralis.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily
import androidx.compose.foundation.shape.RoundedCornerShape


@Composable
fun MainMenuScreen(
    musicEnabled: Boolean = true,
    musicVolume: Float = 1f,
    soundEnabled: Boolean = true,
    soundVolume: Float = 1f,
    encounterChance: Float = 0.22f,
    onMusicEnabledChanged: (Boolean) -> Unit = {},
    onMusicVolumeChanged: (Float) -> Unit = {},
    onSoundEnabledChanged: (Boolean) -> Unit = {},
    onSoundVolumeChanged: (Float) -> Unit = {},
    onEncounterChanceChanged: (Float) -> Unit = {},
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onExitGame: () -> Unit
) {
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuButton(text = "New Game", onClick = onNewGame)
            MenuButton(text = "Continue", onClick = onContinue)
            MenuButton(text = "Settings", onClick = { showSettings = true })
            MenuButton(text = "Exit", onClick = { showExitConfirmation = true })
        }

        if (showSettings) {
            MainMenuSettingsOverlay(
                musicEnabled = musicEnabled,
                musicVolume = musicVolume,
                soundEnabled = soundEnabled,
                soundVolume = soundVolume,
                encounterChance = encounterChance,
                onMusicEnabledChanged = onMusicEnabledChanged,
                onMusicVolumeChanged = onMusicVolumeChanged,
                onSoundEnabledChanged = onSoundEnabledChanged,
                onSoundVolumeChanged = onSoundVolumeChanged,
                onEncounterChanceChanged = onEncounterChanceChanged,
                onBack = { showSettings = false }
            )
        }

        if (showExitConfirmation) {
            MainMenuExitConfirmation(
                onConfirm = onExitGame,
                onCancel = { showExitConfirmation = false }
            )
        }
    }
}

@Composable
private fun MainMenuSettingsOverlay(
    musicEnabled: Boolean,
    musicVolume: Float,
    soundEnabled: Boolean,
    soundVolume: Float,
    encounterChance: Float,
    onMusicEnabledChanged: (Boolean) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onSoundVolumeChanged: (Float) -> Unit,
    onEncounterChanceChanged: (Float) -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.72f))
                .border(1.dp, colors.primary.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = "Settings",
                color = colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            GameSettingsPanel(
                musicEnabled = musicEnabled,
                musicVolume = musicVolume,
                soundEnabled = soundEnabled,
                soundVolume = soundVolume,
                encounterChance = encounterChance,
                onMusicEnabledChanged = onMusicEnabledChanged,
                onMusicVolumeChanged = onMusicVolumeChanged,
                onSoundEnabledChanged = onSoundEnabledChanged,
                onSoundVolumeChanged = onSoundVolumeChanged,
                onEncounterChanceChanged = onEncounterChanceChanged
            )

            MenuButton(text = "Back", onClick = onBack)
        }
    }
}

@Composable
private fun MainMenuExitConfirmation(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.72f))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Are You Sure?",
                color = colors.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = "Exit the game?",
                color = colors.onSurface,
                fontSize = 12.sp,
                fontFamily = CinzelFamily
            )

            MenuButton(text = "Yes", onClick = onConfirm)
            MenuButton(text = "Cancel", onClick = onCancel)
        }
    }
}

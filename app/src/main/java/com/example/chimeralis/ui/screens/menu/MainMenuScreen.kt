package com.example.chimeralis.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily

/**
 * Renders the main menu screen UI.
 *
 * @param musicEnabled The music enabled value used by this operation.
 * @param musicVolume The music volume value used by this operation.
 * @param soundEnabled The sound enabled value used by this operation.
 * @param soundVolume The sound volume value used by this operation.
 * @param encounterChance The encounter chance value used by this operation.
 * @param onMusicEnabledChanged Callback invoked when music enabled changed occurs.
 * @param onMusicVolumeChanged Callback invoked when music volume changed occurs.
 * @param onSoundEnabledChanged Callback invoked when sound enabled changed occurs.
 * @param onSoundVolumeChanged Callback invoked when sound volume changed occurs.
 * @param onEncounterChanceChanged Callback invoked when encounter chance changed occurs.
 * @param onNewGame Callback invoked when new game occurs.
 * @param onContinue Callback invoked when continue occurs.
 * @param onExitGame Callback invoked when exit game occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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
    var showAbout by remember { mutableStateOf(false) }

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

        MainMenuInfoButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp),
            onClick = { showAbout = true }
        )

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

        if (showAbout) {
            MainMenuAboutOverlay(onBack = { showAbout = false })
        }
    }
}

/**
 * Renders the main menu information button.
 *
 * @param modifier Compose modifier applied to the rendered component.
 * @param onClick Callback invoked when click occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun MainMenuInfoButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(48.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = {
                        GameSoundPlayer.play(context, R.raw.button_click)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = colors.background.copy(alpha = if (isPressed) 0.88f else 0.72f)
            )
            drawCircle(
                color = colors.primary.copy(alpha = 0.86f),
                style = Stroke(width = 2.5f)
            )
        }
        Text(
            text = "i",
            color = colors.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            fontFamily = CinzelFamily
        )
    }
}

/**
 * Renders the main menu settings overlay UI.
 *
 * @param musicEnabled The music enabled value used by this operation.
 * @param musicVolume The music volume value used by this operation.
 * @param soundEnabled The sound enabled value used by this operation.
 * @param soundVolume The sound volume value used by this operation.
 * @param encounterChance The encounter chance value used by this operation.
 * @param onMusicEnabledChanged Callback invoked when music enabled changed occurs.
 * @param onMusicVolumeChanged Callback invoked when music volume changed occurs.
 * @param onSoundEnabledChanged Callback invoked when sound enabled changed occurs.
 * @param onSoundVolumeChanged Callback invoked when sound volume changed occurs.
 * @param onEncounterChanceChanged Callback invoked when encounter chance changed occurs.
 * @param onBack Callback invoked when back occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the application and author information overlay.
 *
 * @param onBack Callback invoked when back occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun MainMenuAboutOverlay(
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
                .background(colors.surface.copy(alpha = 0.78f))
                .border(1.dp, colors.primary.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "About Chimeralis",
                color = colors.primary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = "Chimeralis is a creature-collection Android game with exploration, turn-based battles, items, saves, and evolutions.",
                color = colors.onSurface,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                fontFamily = CinzelFamily
            )

            Text(
                text = "Course project: Object-Oriented Programming\nAuthor: Гук М.О., КС-24\nBuilt with Kotlin and Jetpack Compose",
                color = colors.onSurface,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontFamily = CinzelFamily
            )

            MenuButton(text = "Back", onClick = onBack)
        }
    }
}

/**
 * Renders the main menu exit confirmation UI.
 *
 * @param onConfirm Callback invoked when confirm occurs.
 * @param onCancel Callback invoked when cancel occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

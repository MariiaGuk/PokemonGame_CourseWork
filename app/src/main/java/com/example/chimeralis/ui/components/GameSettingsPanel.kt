package com.example.chimeralis.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlin.math.roundToInt

/** Renders the game settings panel UI. */
@Composable
fun GameSettingsPanel(
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        SettingsToggleSlider(
            title = "Music",
            enabled = musicEnabled,
            value = musicVolume,
            onEnabledChanged = onMusicEnabledChanged,
            onValueChanged = onMusicVolumeChanged
        )

        SettingsToggleSlider(
            title = "Sound Effects",
            enabled = soundEnabled,
            value = soundVolume,
            onEnabledChanged = onSoundEnabledChanged,
            onValueChanged = onSoundVolumeChanged
        )

        CompactGameplaySettings(
            encounterChance = encounterChance,
            onEncounterChanceChanged = onEncounterChanceChanged
        )
    }
}

/** Renders the settings toggle slider UI. */
@Composable
private fun SettingsToggleSlider(
    title: String,
    enabled: Boolean,
    value: Float,
    onEnabledChanged: (Boolean) -> Unit,
    onValueChanged: (Float) -> Unit
) {
    SettingCard {
        SettingsControlRow(
            title = title,
            enabled = enabled,
            showSwitch = true,
            value = value,
            onEnabledChanged = onEnabledChanged,
            onValueChanged = onValueChanged
        )
    }
}

/** Renders the compact gameplay settings UI. */
@Composable
private fun CompactGameplaySettings(
    encounterChance: Float,
    onEncounterChanceChanged: (Float) -> Unit
) {
    SettingCard {
        SettingsControlRow(
            title = "Encounter Rate",
            enabled = true,
            showSwitch = false,
            value = encounterChance,
            onValueChanged = onEncounterChanceChanged
        )
    }
}

/** Renders the setting card UI. */
@Composable
private fun SettingCard(content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.42f))
            .border(1.dp, colors.primary.copy(alpha = 0.36f), RoundedCornerShape(8.dp))
            .padding(horizontal = 11.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        content = content
    )
}

/** Renders the settings control row UI. */
@Composable
private fun SettingsControlRow(
    title: String,
    enabled: Boolean,
    showSwitch: Boolean,
    value: Float,
    onEnabledChanged: (Boolean) -> Unit = {},
    onValueChanged: (Float) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val percent = (value.coerceIn(0f, 1f) * 100).roundToInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            fontFamily = CinzelFamily,
            modifier = Modifier.width(112.dp)
        )

        if (showSwitch) {
            Switch(
                checked = enabled,
                onCheckedChange = { checked ->
                    GameSoundPlayer.play(context, R.raw.button_click)
                    onEnabledChanged(checked)
                }
            )
        }

        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onValueChanged,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$percent%",
            color = colors.onSurface.copy(alpha = 0.82f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CinzelFamily,
            textAlign = TextAlign.End,
            modifier = Modifier.width(42.dp)
        )
    }
}

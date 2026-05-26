package com.example.chimeralis.ui.overlays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.ui.screens.world.Joystick
import com.example.chimeralis.ui.screens.world.SmallWorldMenuButton
import com.example.chimeralis.ui.screens.world.TeamSlots

@Composable
fun BoxScope.WorldControlsOverlay(
    team: List<Chimera>,
    teamStateKey: Int,
    joystickEnabled: Boolean,
    joystickResetKey: Any? = Unit,
    actionLabel: String? = null,
    showMenuButton: Boolean = true,
    showBagButton: Boolean = true,
    onDirectionChanged: (Float, Float) -> Unit,
    onMenu: () -> Unit,
    onBag: () -> Unit,
    onAction: () -> Unit = {}
) {
    if (showMenuButton) {
        Box(
            modifier = Modifier
                .padding(14.dp)
                .width(92.dp)
                .height(34.dp)
        ) {
            SmallWorldMenuButton(text = "Menu", onClick = onMenu)
        }
    }

    if (showBagButton) {
        Box(
            modifier = Modifier
                .padding(14.dp)
                .align(Alignment.TopEnd)
                .width(76.dp)
                .height(34.dp)
        ) {
            SmallWorldMenuButton(text = "Bag", onClick = onBag)
        }
    }

    TeamSlots(
        team = team,
        stateKey = teamStateKey,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 39.dp, bottom = 20.dp)
    )

    Joystick(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 93.dp, bottom = 43.dp),
        enabled = joystickEnabled,
        resetKey = joystickResetKey,
        onDirectionChanged = onDirectionChanged
    )

    actionLabel?.let { label ->
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(128.dp)
                .height(38.dp)
        ) {
            SmallWorldMenuButton(text = label, onClick = onAction)
        }
    }
}

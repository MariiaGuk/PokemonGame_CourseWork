package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.ui.theme.CinzelFamily

/** Renders the battle back arrow button UI. */
@Composable
internal fun BattleBackArrowButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = modifier
            .size(BattleBackButtonSize)
            .clip(RoundedCornerShape(5.dp))
            .background(colors.background.copy(alpha = 0.42f))
            .border(1.dp, colors.primary.copy(alpha = 0.55f), RoundedCornerShape(5.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        GameSoundPlayer.play(context, R.raw.button_click)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "<",
            color = colors.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Black,
            fontSize = 22.sp
        )
    }
}

/** Renders the battle message UI. */
@Composable
internal fun BattleMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Text(
        text = text,
        color = colors.onSurface,
        fontFamily = CinzelFamily,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 4.sp,
        modifier = modifier
    )
}

package com.example.chimeralis.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.ui.theme.CinzelFamily

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            colors.secondary.copy(alpha = 0.6f),
            colors.primary.copy(alpha = 0.9f),
            colors.secondary.copy(alpha = 0.6f)
        )
    )
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(160.dp)
            .height(42.dp)
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
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cut = size.height * 0.25f
            val path = Path().apply {
                moveTo(cut, 0f)
                lineTo(size.width - cut, 0f)
                lineTo(size.width, cut)
                lineTo(size.width, size.height - cut)
                lineTo(size.width - cut, size.height)
                lineTo(cut, size.height)
                lineTo(0f, size.height - cut)
                lineTo(0f, cut)
                close()
            }
            drawPath(
                path = path,
                color = if (isPressed) colors.surface.copy(alpha = 0.85f)
                else colors.background.copy(alpha = 0.75f)
            )
            drawPath(
                path = path,
                brush = gradientBrush,
                style = Stroke(width = 2f)
            )
        }
        Text(
            text = text,
            color = colors.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = CinzelFamily
        )
    }
}

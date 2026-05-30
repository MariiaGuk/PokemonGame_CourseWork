package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chimeralis.logic.battle.ChimeraEvolutionEvent
import com.example.chimeralis.ui.screens.chimera.chimeraImageRes
import kotlinx.coroutines.delay

internal const val EvolutionOverlayDurationMillis = 8000L
internal const val EvolutionRevealMillis = 7000L
private const val EvolutionBlinkIntervalMillis = 500L
private const val EvolutionFrameMillis = 150L

/** Renders the post-battle evolution animation. */
@Composable
internal fun EvolutionOverlay(
    event: ChimeraEvolutionEvent,
    modifier: Modifier = Modifier
) {
    var elapsedMillis by remember(event) { mutableLongStateOf(0L) }

    LaunchedEffect(event) {
        elapsedMillis = 0L
        while (elapsedMillis < EvolutionOverlayDurationMillis) {
            delay(EvolutionFrameMillis)
            elapsedMillis += EvolutionFrameMillis
        }
    }

    val isRevealed = elapsedMillis >= EvolutionRevealMillis
    val imageRes = if (isRevealed) {
        event.newSpecies.chimeraImageRes()
    } else {
        event.oldSpecies.chimeraImageRes()
    }
    val blinkAlpha = if (isRevealed || (elapsedMillis / EvolutionBlinkIntervalMillis) % 2L == 0L) {
        1f
    } else {
        0.18f
    }
    val revealProgress = ((elapsedMillis - EvolutionRevealMillis).coerceAtLeast(0L) / 700f)
        .coerceIn(0f, 1f)
    val scale = if (isRevealed) 1f + 0.08f * revealProgress else 1f

    BoxWithConstraints(
        modifier = modifier
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val imageWidth = minOf(maxWidth * 0.74f, 620.dp)
        val imageHeight = minOf(maxHeight * 0.62f, 360.dp)

        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = if (!isRevealed && blinkAlpha < 1f) {
                    ColorFilter.tint(Color.White.copy(alpha = 0.7f))
                } else {
                    null
                },
                modifier = Modifier
                    .width(imageWidth)
                    .height(imageHeight)
                    .graphicsLayer {
                        alpha = blinkAlpha
                        scaleX = scale
                        scaleY = scale
                    }
            )
        }
    }
}

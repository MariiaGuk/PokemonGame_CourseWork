package com.example.chimeralis.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/** Renders the location transition overlay UI. */
@Composable
fun LocationTransitionOverlay(
    alpha: Float,
    modifier: Modifier = Modifier
) {
    if (alpha <= 0f) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
            .background(Color.White.copy(alpha = alpha.coerceIn(0f, 1f)))
    )
}

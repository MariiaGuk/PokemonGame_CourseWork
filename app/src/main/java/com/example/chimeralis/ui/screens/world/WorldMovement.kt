package com.example.chimeralis.ui.screens.world

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/** Renders the joystick UI. */
@Composable
internal fun Joystick(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    resetKey: Any? = Unit,
    onDirectionChanged: (Float, Float) -> Unit
) {
    var knobX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var knobY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val radiusPx = with(density) { 42.dp.toPx() }

    LaunchedEffect(enabled, resetKey) {
        if (!enabled || resetKey != Unit) {
            knobX = 0f
            knobY = 0f
            onDirectionChanged(0f, 0f)
        }
    }

    Box(
        modifier = modifier
            .size(112.dp)
            .background(Color.Black.copy(alpha = 0.25f), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .pointerInput(enabled, resetKey) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = {
                        knobX = 0f
                        knobY = 0f
                    },
                    onDragEnd = {
                        knobX = 0f
                        knobY = 0f
                        onDirectionChanged(0f, 0f)
                    },
                    onDragCancel = {
                        knobX = 0f
                        knobY = 0f
                        onDirectionChanged(0f, 0f)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val nextX = knobX + dragAmount.x
                        val nextY = knobY + dragAmount.y
                        val distance = hypot(nextX, nextY)
                        val scale = if (distance > radiusPx) radiusPx / distance else 1f

                        knobX = nextX * scale
                        knobY = nextY * scale
                        onDirectionChanged(knobX / radiusPx, knobY / radiusPx)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knobX.roundToInt(), knobY.roundToInt()) }
                .size(42.dp)
                .background(Color(0xFFFFD700).copy(alpha = 0.9f), CircleShape)
                .border(2.dp, Color(0xFF3D1500), CircleShape)
        )
    }
}

/** Handles joystick direction behavior. */
internal fun joystickDirection(x: Float, y: Float): Direction? {
    if (abs(x) < JoystickDeadZone && abs(y) < JoystickDeadZone) return null
    return if (abs(x) > abs(y)) {
        if (x < 0f) Direction.Left else Direction.Right
    } else {
        if (y < 0f) Direction.Up else Direction.Down
    }
}

/** Handles next tile behavior. */
internal fun nextTile(column: Int, row: Int, direction: Direction): Pair<Int, Int> = when (direction) {
    Direction.Down -> column to (row + 1).coerceAtMost(MapRows - 1)
    Direction.Up -> column to (row - 1).coerceAtLeast(0)
    Direction.Left -> (column - 1).coerceAtLeast(0) to row
    Direction.Right -> (column + 1).coerceAtMost(MapColumns - 1) to row
}

/** Handles next interior tile behavior. */
internal fun nextInteriorTile(column: Int, row: Int, direction: Direction): Pair<Int, Int> = when (direction) {
    Direction.Down -> column to (row + 1).coerceAtMost(InteriorRows - 1)
    Direction.Up -> column to (row - 1).coerceAtLeast(0)
    Direction.Left -> (column - 1).coerceAtLeast(0) to row
    Direction.Right -> (column + 1).coerceAtMost(InteriorColumns - 1) to row
}


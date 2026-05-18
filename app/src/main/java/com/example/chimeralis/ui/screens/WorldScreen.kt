package com.example.chimeralis.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random

private const val MapColumns = 18
private const val MapRows = 10
private const val EncounterChance = 0.22f
private const val StepDurationMs = 280
private const val HeldStepDelayMs = 65L
private const val JoystickDeadZone = 0.35f
private const val MovingFrameDelayMs = 160L
private const val IdleFrameDelayMs = 320L
private const val WorldZoom = 1.18f

private val grassTiles = setOf(
    3 to 2, 4 to 2, 5 to 2, 12 to 2, 13 to 2, 14 to 2,
    3 to 3, 4 to 3, 5 to 3, 12 to 3, 13 to 3, 14 to 3,
    7 to 5, 8 to 5, 9 to 5, 10 to 5,
    7 to 6, 8 to 6, 9 to 6, 10 to 6,
    2 to 7, 3 to 7, 14 to 7, 15 to 7,
    2 to 8, 3 to 8, 14 to 8, 15 to 8
)

private enum class Direction { Down, Up, Left, Right }

@Composable
fun WorldScreen(
    starter: ChimeraSpecies?,
    onWildEncounter: (ChimeraSpecies) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var playerColumn by remember { mutableIntStateOf(1) }
    var playerRow by remember { mutableIntStateOf(1) }
    var targetColumn by remember { mutableIntStateOf(1) }
    var targetRow by remember { mutableIntStateOf(1) }
    var direction by remember { mutableStateOf(Direction.Down) }
    var requestedDirection by remember { mutableStateOf<Direction?>(null) }
    var isMoving by remember { mutableStateOf(false) }
    var animationFrame by remember { mutableIntStateOf(0) }
    var lastGrassTile by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val groundTexture = ImageBitmap.imageResource(id = R.drawable.lava_ground)
    val grassTexture = ImageBitmap.imageResource(id = R.drawable.rock_grass_tile)

    val animatedColumn by animateFloatAsState(
        targetValue = targetColumn.toFloat(),
        animationSpec = tween(durationMillis = StepDurationMs, easing = LinearEasing),
        label = "playerColumn"
    )
    val animatedRow by animateFloatAsState(
        targetValue = targetRow.toFloat(),
        animationSpec = tween(durationMillis = StepDurationMs, easing = LinearEasing),
        label = "playerRow"
    )

    LaunchedEffect(isMoving, direction) {
        while (true) {
            animationFrame++
            delay(if (isMoving) MovingFrameDelayMs else IdleFrameDelayMs)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val nextDirection = requestedDirection
            if (nextDirection == null) {
                delay(16L)
                continue
            }

            val currentColumn = playerColumn
            val currentRow = playerRow
            val nextTile = nextTile(currentColumn, currentRow, nextDirection)
            direction = nextDirection

            if (nextTile.first == currentColumn && nextTile.second == currentRow) {
                delay(HeldStepDelayMs)
                continue
            }

            targetColumn = nextTile.first
            targetRow = nextTile.second
            isMoving = true
            delay(StepDurationMs.toLong())

            playerColumn = nextTile.first
            playerRow = nextTile.second
            isMoving = false

            if (nextTile in grassTiles) {
                if (nextTile != lastGrassTile) {
                    lastGrassTile = nextTile
                    if (Random.nextFloat() < EncounterChance) {
                        onWildEncounter(randomWildChimera(starter))
                    }
                }
            } else {
                lastGrassTile = null
            }

            delay(1L)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF241829))
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val tileWidth = widthPx / MapColumns * WorldZoom
        val tileHeight = heightPx / MapRows * WorldZoom
        val mapWidth = tileWidth * MapColumns
        val mapHeight = tileHeight * MapRows
        val mapLeft = (widthPx / 2f - (animatedColumn + 0.5f) * tileWidth)
            .coerceIn(widthPx - mapWidth, 0f)
        val mapTop = (heightPx / 2f - (animatedRow + 0.5f) * tileHeight)
            .coerceIn(heightPx - mapHeight, 0f)
        val spriteBaseSize = minOf(tileWidth, tileHeight)
        val spriteWidth = spriteBaseSize * 1.06f
        val spriteHeight = spriteBaseSize * 1.62f
        val playerCenterX = mapLeft + (animatedColumn + 0.5f) * tileWidth
        val playerCenterY = mapTop + (animatedRow + 0.5f) * tileHeight

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (row in 0 until MapRows) {
                for (column in 0 until MapColumns) {
                    val left = mapLeft + column * tileWidth
                    val top = mapTop + row * tileHeight
                    val isGrass = column to row in grassTiles
                    val tileDstSize = IntSize(
                        width = (tileWidth + 1f).roundToInt(),
                        height = (tileHeight + 1f).roundToInt()
                    )

                    if (isGrass) {
                        drawImage(
                            image = grassTexture,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(grassTexture.width, grassTexture.height),
                            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                            dstSize = tileDstSize
                        )
                    } else {
                        drawImage(
                            image = groundTexture,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(groundTexture.width, groundTexture.height),
                            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                            dstSize = tileDstSize
                        )
                    }

                }
            }
        }

        Image(
            painter = painterResource(id = playerFrame(direction, isMoving, animationFrame)),
            contentDescription = "Player",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (playerCenterX - spriteWidth / 2f).roundToInt(),
                        y = (playerCenterY - spriteHeight * 0.76f).roundToInt()
                    )
                }
                .size(
                    width = with(density) { spriteWidth.toDp() },
                    height = with(density) { spriteHeight.toDp() }
                )
                .graphicsLayer {
                    scaleX = if (direction == Direction.Left) -1f else 1f
                }
        )

        Text(
            text = "Wild grass can trigger battles",
            color = colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CinzelFamily,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(colors.surface.copy(alpha = 0.7f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )

        Joystick(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 24.dp),
            onDirectionChanged = { x, y ->
                requestedDirection = joystickDirection(x, y)
            }
        )
    }
}

@Composable
private fun Joystick(
    modifier: Modifier = Modifier,
    onDirectionChanged: (Float, Float) -> Unit
) {
    var knobX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var knobY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val radiusPx = with(density) { 42.dp.toPx() }

    Box(
        modifier = modifier
            .size(112.dp)
            .background(Color.Black.copy(alpha = 0.25f), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .pointerInput(Unit) {
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

private fun joystickDirection(x: Float, y: Float): Direction? {
    if (abs(x) < JoystickDeadZone && abs(y) < JoystickDeadZone) return null
    return if (abs(x) > abs(y)) {
        if (x < 0f) Direction.Left else Direction.Right
    } else {
        if (y < 0f) Direction.Up else Direction.Down
    }
}

private fun nextTile(column: Int, row: Int, direction: Direction): Pair<Int, Int> = when (direction) {
    Direction.Down -> column to (row + 1).coerceAtMost(MapRows - 1)
    Direction.Up -> column to (row - 1).coerceAtLeast(0)
    Direction.Left -> (column - 1).coerceAtLeast(0) to row
    Direction.Right -> (column + 1).coerceAtMost(MapColumns - 1) to row
}

private fun playerFrame(direction: Direction, isMoving: Boolean, frameIndex: Int): Int {
    val frames = when {
        isMoving && direction == Direction.Down -> frontRunFrames
        isMoving && direction == Direction.Up -> backRunFrames
        isMoving && (direction == Direction.Left || direction == Direction.Right) -> sideRunFrames
        !isMoving && direction == Direction.Down -> frontIdleFrames
        !isMoving && direction == Direction.Up -> backIdleFrames
        else -> sideIdleFrames
    }

    return frames[frameIndex % frames.size]
}

private val frontIdleFrames = listOf(
    R.drawable.player_front_idle_1,
    R.drawable.player_front_idle_2,
    R.drawable.player_front_idle_3,
    R.drawable.player_front_idle_4
)
private val backIdleFrames = listOf(
    R.drawable.player_back_idle_1,
    R.drawable.player_back_idle_2,
    R.drawable.player_back_idle_3,
    R.drawable.player_back_idle_4
)
private val sideIdleFrames = listOf(
    R.drawable.player_side_idle_1,
    R.drawable.player_side_idle_2,
    R.drawable.player_side_idle_3
)
private val frontRunFrames = listOf(
    R.drawable.player_front_run_1,
    R.drawable.player_front_run_2,
    R.drawable.player_front_run_3,
    R.drawable.player_front_run_4,
    R.drawable.player_front_run_5,
    R.drawable.player_front_run_6,
    R.drawable.player_front_run_7,
    R.drawable.player_front_run_8
)
private val backRunFrames = listOf(
    R.drawable.player_back_run_1,
    R.drawable.player_back_run_2,
    R.drawable.player_back_run_3,
    R.drawable.player_back_run_4,
    R.drawable.player_back_run_5,
    R.drawable.player_back_run_6,
    R.drawable.player_back_run_7,
    R.drawable.player_back_run_8
)
private val sideRunFrames = listOf(
    R.drawable.player_side_run_1,
    R.drawable.player_side_run_2,
    R.drawable.player_side_run_3,
    R.drawable.player_side_run_4,
    R.drawable.player_side_run_5,
    R.drawable.player_side_run_6,
    R.drawable.player_side_run_7,
    R.drawable.player_side_run_8
)

private fun randomWildChimera(starter: ChimeraSpecies?): ChimeraSpecies {
    val pool = listOf(
        ChimeraSpecies.Sunflare,
        ChimeraSpecies.Sylvhorn,
        ChimeraSpecies.Aquantis
    ).filterNot { it == starter }

    return pool.random()
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBushTile(
    left: Float,
    top: Float,
    tileSize: Float
) {
    val dark = Color(0xFF1F5E28)
    val light = Color(0xFF55A84B)

    drawCircle(
        color = dark,
        radius = tileSize * 0.28f,
        center = Offset(left + tileSize * 0.28f, top + tileSize * 0.55f)
    )
    drawCircle(
        color = light,
        radius = tileSize * 0.25f,
        center = Offset(left + tileSize * 0.5f, top + tileSize * 0.42f)
    )
    drawCircle(
        color = dark,
        radius = tileSize * 0.28f,
        center = Offset(left + tileSize * 0.72f, top + tileSize * 0.55f)
    )
}

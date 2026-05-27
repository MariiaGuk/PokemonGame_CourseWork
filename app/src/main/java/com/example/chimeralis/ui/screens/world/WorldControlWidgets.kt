package com.example.chimeralis.ui.screens.world

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.world.locations.TownInterior
import com.example.chimeralis.ui.screens.world.locations.TownSign
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.math.roundToInt

/** Renders the team slots UI. */
@Composable
internal fun TeamSlots(
    team: List<Chimera>,
    modifier: Modifier = Modifier,
    selectionMode: Boolean = false,
    stateKey: Int = 0,
    targetItem: Item? = null,
    onChimeraSelected: (Chimera) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(MaxTeamSize) { index ->
            val chimera = team.getOrNull(index)
            TeamSlot(
                chimera = chimera,
                isActive = index == 0 && team.isNotEmpty(),
                selectionMode = selectionMode,
                stateKey = stateKey,
                isSelectable = chimera?.let { targetItem?.canUseOn(it) ?: true } ?: false,
                onChimeraSelected = onChimeraSelected
            )
        }
    }
}

/** Renders the team slot UI. */
@Composable
internal fun TeamSlot(
    chimera: Chimera?,
    isActive: Boolean,
    selectionMode: Boolean,
    stateKey: Int,
    isSelectable: Boolean,
    onChimeraSelected: (Chimera) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val hpRatio = chimera?.let {
        (it.stats.currentHp.toFloat() / it.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    } ?: 0f
    val frameAlpha = if (chimera == null) 0.22f else 0.58f
    val contentAlpha = when {
        chimera == null -> 0.18f
        selectionMode && !isSelectable -> 0.28f
        chimera.stats.isAlive() -> 1f
        else -> 0.45f
    }

    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surface.copy(alpha = frameAlpha))
            .border(
                width = if (isActive || selectionMode) 2.dp else 1.dp,
                color = colors.primary.copy(
                    alpha = when {
                        selectionMode && chimera != null && isSelectable -> 0.92f
                        selectionMode && chimera != null -> 0.24f
                        isActive -> 0.8f
                        else -> 0.38f
                    }
                ),
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(chimera, selectionMode, isSelectable, stateKey) {
                detectTapGestures(
                    onTap = {
                        if (selectionMode && chimera != null && isSelectable) {
                            onChimeraSelected(chimera)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (chimera != null) {
            Image(
                painter = painterResource(id = chimera.species.teamImageRes()),
                contentDescription = chimera.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = (-2).dp, y = -2.dp)
                    .size(38.dp)
                    .graphicsLayer { alpha = contentAlpha }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 5.dp, vertical = 3.dp)
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF2B190E).copy(alpha = 0.9f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpRatio)
                        .height(3.dp)
                        .background(
                            when {
                                hpRatio > 0.5f -> Color(0xFF66C96A)
                                hpRatio > 0.2f -> Color(0xFFE0B84B)
                                else -> Color(0xFFD85A4A)
                            }
                        )
                )
            }
        }
    }
}

/** Renders the shift npc world sprite UI. */
@Composable
internal fun ShiftNpcWorldSprite(
    frameIndex: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = shiftNpcIdleFrame(frameIndex)),
        contentDescription = "Shift NPC",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

/** Renders the trainer npc world sprite UI. */
@Composable
internal fun TrainerNpcWorldSprite(
    frameIndex: Int,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = trainerNpcIdleFrame(frameIndex)),
        contentDescription = "Trainer NPC",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

/** Renders the small world menu button UI. */
@Composable
internal fun SmallWorldMenuButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface.copy(alpha = 0.42f))
            .border(
                1.dp,
                colors.primary.copy(alpha = if (enabled) 0.45f else 0.18f),
                RoundedCornerShape(6.dp)
            )
            .pointerInput(enabled) {
                detectTapGestures(
                    onTap = {
                        if (enabled) {
                            GameSoundPlayer.play(context, R.raw.button_click)
                            onClick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = text,
            style = TextStyle(
                color = colors.primary.copy(alpha = if (enabled) 0.9f else 0.34f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily
            )
        )
    }
}


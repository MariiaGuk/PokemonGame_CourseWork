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

@Composable
internal fun ItemTargetSelectionOverlay(
    item: Item,
    team: List<Chimera>,
    teamStateKey: Int,
    onChimeraSelected: (Chimera) -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.62f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(260.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.78f))
                .border(1.dp, colors.primary.copy(alpha = 0.46f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.name,
                color = colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = "Choose a chimera",
                color = colors.onSurface.copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontFamily = CinzelFamily
            )

            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(28.dp)
            ) {
                SmallWorldMenuButton(text = "Cancel", onClick = onCancel)
            }
        }

        TeamSlots(
            team = team,
            selectionMode = true,
            stateKey = teamStateKey,
            targetItem = item,
            onChimeraSelected = onChimeraSelected,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 39.dp, bottom = 20.dp)
        )
    }
}

@Composable
internal fun ConfirmItemUseDialog(
    item: Item,
    chimera: Chimera,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val canUseItem = item.canUseOn(chimera)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.9f))
                .border(1.dp, colors.primary.copy(alpha = 0.54f), RoundedCornerShape(8.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Use ${item.name}?",
                color = colors.primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = if (canUseItem) "Use on ${chimera.name}?"
                else "${item.name} cannot be used on ${chimera.name}.",
                color = colors.onSurface.copy(alpha = 0.78f),
                fontSize = 12.sp,
                fontFamily = CinzelFamily
            )

            MenuButton(text = "Use", enabled = canUseItem, onClick = onConfirm)
            MenuButton(text = "Cancel", onClick = onCancel)
        }
    }
}

@Composable
internal fun TownSignDialogOverlay(
    sign: TownSign,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
            .pointerInput(Unit) {
                detectTapGestures { }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(520.dp)
                .background(colors.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .border(1.dp, colors.primary.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = sign.title,
                color = colors.primary,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = sign.body,
                color = colors.onSurface.copy(alpha = 0.88f),
                fontSize = 16.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(42.dp)
            ) {
                SmallWorldMenuButton(text = "Close", onClick = onClose)
            }
        }
    }
}

@Composable
internal fun ShiftNpcDialogOverlay(
    step: Int,
    isReturnDialog: Boolean,
    isShortTravelDialog: Boolean,
    onNext: () -> Unit,
    onStay: () -> Unit,
    onTravel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var portraitFrame by remember(step) { mutableIntStateOf(0) }

    LaunchedEffect(step) {
        while (true) {
            portraitFrame++
            delay(520L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
    ) {
        Image(
            painter = painterResource(
                id = shiftNpcDialogFrame(
                    step = if (isReturnDialog || isShortTravelDialog) 2 else step,
                    frameIndex = portraitFrame
                )
            ),
            contentDescription = "Shift NPC dialog",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 130.dp)
                .height(620.dp)
                .graphicsLayer(
                    scaleX = 1.6f,
                    scaleY = 1.6f,
                    transformOrigin = TransformOrigin(0.2f, 0.2f)
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(152.dp)
                .background(colors.surface.copy(alpha = 0.78f))
                .border(1.dp, colors.primary.copy(alpha = 0.42f))
                .padding(horizontal = 90.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when {
                    isReturnDialog -> "Ready to go back?"
                    isShortTravelDialog -> "Ready to head to trainer town?"
                    else -> shiftNpcDialogText(step)
                },
                color = colors.primary,
                fontSize = if (step >= 2 || isReturnDialog || isShortTravelDialog) 15.sp else 18.sp,
                lineHeight = if (step >= 2 || isReturnDialog || isShortTravelDialog) 21.sp else 25.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                modifier = Modifier.weight(1f)
            )

            if (step < 3 && !isReturnDialog && !isShortTravelDialog) {
                Box(
                    modifier = Modifier
                        .width(160.dp)
                        .height(42.dp)
                ) {
                    MenuButton(text = "Next", onClick = onNext)
                }
            } else {
                Column(
                    modifier = Modifier.width(174.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isReturnDialog) "Return?" else "Go now?",
                        color = colors.onSurface.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CinzelFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MenuButton(text = "Yes", onClick = onTravel)
                    MenuButton(text = "No", onClick = onStay)
                }
            }
        }
    }
}


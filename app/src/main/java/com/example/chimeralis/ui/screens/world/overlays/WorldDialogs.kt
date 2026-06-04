package com.example.chimeralis.ui.screens.world.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.trainers.NpcDialogues
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.world.locations.model.TownSign
import com.example.chimeralis.ui.screens.world.SmallWorldMenuButton
import com.example.chimeralis.ui.screens.world.sprites.shiftNpcDialogFrame
import com.example.chimeralis.ui.screens.world.sprites.shiftNpcDialogText
import com.example.chimeralis.ui.screens.world.sprites.trainerNpcDialogFrame
import com.example.chimeralis.ui.screens.world.TeamSlots
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay

/**
 * Renders the item target selection overlay UI.
 *
 * @param item Domain object used by this operation: item.
 * @param team The team value used by this operation.
 * @param teamStateKey The team state key value used by this operation.
 * @param onChimeraSelected Callback invoked when chimera selected occurs.
 * @param onCancel Callback invoked when cancel occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the confirm item use dialog UI.
 *
 * @param item Domain object used by this operation: item.
 * @param chimera Domain object used by this operation: chimera.
 * @param onConfirm Callback invoked when confirm occurs.
 * @param onCancel Callback invoked when cancel occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the town sign dialog overlay UI.
 *
 * @param sign The sign value used by this operation.
 * @param onClose Callback invoked when close occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the shift npc dialog overlay UI.
 *
 * @param step Numeric value used by this operation: step.
 * @param isReturnDialog Flag that controls or describes is return dialog.
 * @param isShortTravelDialog Flag that controls or describes is short travel dialog.
 * @param onNext Callback invoked when next occurs.
 * @param onStay Callback invoked when stay occurs.
 * @param onTravel Callback invoked when travel occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
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

/**
 * Renders the trainer npc challenge overlay UI.
 *
 * @param step Numeric value used by this operation: step.
 * @param onNext Callback invoked when next occurs.
 * @param onDecline Callback invoked when decline occurs.
 * @param onChallenge Callback invoked when challenge occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun TrainerNpcChallengeOverlay(
    step: Int,
    onNext: () -> Unit,
    onDecline: () -> Unit,
    onChallenge: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var portraitFrame by remember(step) { mutableIntStateOf(0) }

    LaunchedEffect(step) {
        while (true) {
            portraitFrame++
            delay(560L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
    ) {
        Image(
            painter = painterResource(id = trainerNpcDialogFrame(portraitFrame)),
            contentDescription = "Trainer NPC dialog",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 120.dp)
                .height(620.dp)
                .graphicsLayer(
                    scaleX = 1.45f,
                    scaleY = 1.45f,
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
                text = NpcDialogues.RivalTrainer.challengeLine(step).orEmpty(),
                color = colors.primary,
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                modifier = Modifier.weight(1f)
            )

            if (step == 0) {
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
                        text = "Battle?",
                        color = colors.onSurface.copy(alpha = 0.82f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CinzelFamily,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    MenuButton(text = "Yes", onClick = onChallenge)
                    MenuButton(text = "No", onClick = onDecline)
                }
            }
        }
    }
}


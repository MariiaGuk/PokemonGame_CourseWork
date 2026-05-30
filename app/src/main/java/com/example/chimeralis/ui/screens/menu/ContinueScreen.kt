package com.example.chimeralis.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.data.GameSave
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily

/** Renders the continue screen UI. */
@Composable
fun ContinueScreen(
    saves: List<GameSave>,
    onLoad: (GameSave) -> Unit,
    onDelete: (GameSave) -> Unit,
    onBack: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var pendingDeleteSave by remember { mutableStateOf<GameSave?>(null) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF542012), colors.background, Color(0xFF120806)),
                    center = Offset(180f, 80f),
                    radius = 950f
                )
            )
    ) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF173F70).copy(alpha = 0.92f), Color.Transparent),
                        center = Offset(widthPx * 1.05f, heightPx * 1.08f),
                        radius = widthPx * 0.48f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Continue",
                color = colors.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            if (saves.isEmpty()) {
                Box(
                    modifier = Modifier
                        .width(520.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved games yet.",
                        color = colors.onSurface.copy(alpha = 0.82f),
                        fontSize = 14.sp,
                        fontFamily = CinzelFamily,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                SaveList(
                    saves = saves,
                    onLoad = onLoad,
                    onDelete = { save -> pendingDeleteSave = save },
                    modifier = Modifier
                        .width(520.dp)
                        .weight(1f)
                )
            }

            MenuButton(text = "Back", onClick = onBack)
        }

        pendingDeleteSave?.let { save ->
            DeleteSaveConfirmation(
                save = save,
                onConfirm = {
                    onDelete(save)
                    pendingDeleteSave = null
                },
                onCancel = { pendingDeleteSave = null }
            )
        }
    }
}

/** Renders the delete save confirmation UI. */
@Composable
private fun DeleteSaveConfirmation(
    save: GameSave,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.52f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.width(320.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Delete Save?",
                color = colors.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Delete ${save.trainerName}'s progress?",
                color = colors.onSurface.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center
            )

            MenuButton(text = "Delete", onClick = onConfirm)
            MenuButton(text = "Cancel", onClick = onCancel)
        }
    }
}

/** Renders the save list UI. */
@Composable
private fun SaveList(
    saves: List<GameSave>,
    onLoad: (GameSave) -> Unit,
    onDelete: (GameSave) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val scrollbarFraction = if (scrollState.maxValue == 0) {
        0f
    } else {
        scrollState.value / scrollState.maxValue.toFloat()
    }

    Row(
        modifier = modifier.height(ContinueVisibleSaveRowsHeight),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            saves.forEach { save ->
                SaveRow(
                    save = save,
                    onLoad = { onLoad(save) },
                    onDelete = { onDelete(save) }
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(99.dp))
                .background(colors.surface.copy(alpha = 0.5f))
        ) {
            val visibleRows = 4f
            val visibleFraction = (visibleRows / saves.size.toFloat()).coerceIn(0f, 1f)
            val thumbHeight = maxHeight * visibleFraction
            val thumbTravel = maxHeight - thumbHeight
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = thumbTravel * scrollbarFraction)
                    .width(6.dp)
                    .height(thumbHeight)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colors.primary.copy(alpha = 0.85f))
            )
        }
    }
}

/** Renders the save row UI. */
@Composable
private fun SaveRow(
    save: GameSave,
    onLoad: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.76f))
            .border(1.dp, colors.primary.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .clickable(onClick = onLoad)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = save.trainerName,
                color = colors.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            Text(
                text = "${save.starterNickname} / ${save.starterSpecies.displayName()}",
                color = colors.onSurface.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily
            )
        }

        SaveActionButton(text = "Load", onClick = onLoad)
        SaveActionButton(text = "Delete", onClick = onDelete)
    }
}

/** Renders the save action button UI. */
@Composable
private fun SaveActionButton(
    text: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(104.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.background.copy(alpha = 0.64f))
            .border(1.dp, colors.primary.copy(alpha = 0.58f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = CinzelFamily
        )
    }
}

private val ContinueVisibleSaveRowsHeight = 250.dp

/** Handles display name behavior. */
private fun ChimeraSpecies.displayName(): String = ChimeraFactory.speciesName(this)

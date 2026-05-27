package com.example.chimeralis.ui.screens.world

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.trainers.PlayerCollectionLimits
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlin.math.abs

/** Shows the PC-style overlay for managing the active team and stored chimeras. */
@Composable
internal fun ChimeraStorageOverlay(
    team: List<Chimera>,
    storage: List<Chimera>,
    teamStateKey: Int,
    onSwapTeamMembers: (Int, Int) -> Unit,
    onDepositTeamMember: (Int) -> Unit,
    onWithdrawStoredChimera: (Int) -> Unit,
    onSwapTeamWithStorage: (Int, Int) -> Unit,
    onClose: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selectedTeamIndex by remember(teamStateKey) { mutableStateOf<Int?>(null) }
    var selectedStorageIndex by remember(teamStateKey) { mutableStateOf<Int?>(null) }
    val storageScrollState = rememberScrollState()

    /** Selects a team slot for storage actions. */
    fun selectTeam(index: Int) {
        val storageIndex = selectedStorageIndex
        if (storageIndex != null) {
            onSwapTeamWithStorage(index, storageIndex)
            selectedTeamIndex = null
            selectedStorageIndex = null
        } else {
            selectedTeamIndex = index
        }
    }

    /** Selects a storage slot for team actions. */
    fun selectStorage(index: Int) {
        val teamIndex = selectedTeamIndex
        if (teamIndex != null) {
            onSwapTeamWithStorage(teamIndex, index)
            selectedTeamIndex = null
            selectedStorageIndex = null
        } else {
            selectedStorageIndex = index
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 820.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.96f))
                .border(2.dp, colors.primary.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Chimera Storage",
                    color = colors.primary,
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                CloseStorageButton(
                    onClose = onClose,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StoragePanel(title = "Team", count = "${team.size}/${PlayerCollectionLimits.MaxTeamSize}", width = 400.dp) {
                    repeat(2) { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            repeat(3) { column ->
                                val index = row * 3 + column
                                StorageTeamSlot(
                                    chimera = team.getOrNull(index),
                                    selected = selectedTeamIndex == index,
                                    isPrimary = index == 0,
                                    onTap = { if (team.getOrNull(index) != null) selectTeam(index) },
                                    onDrag = { dragX, dragY ->
                                        when {
                                            dragX > 90f -> onDepositTeamMember(index)
                                            dragY < -70f -> onSwapTeamMembers(index, (index - 3).coerceAtLeast(0))
                                            dragY > 70f -> onSwapTeamMembers(index, (index + 3).coerceAtMost(team.lastIndex))
                                            dragX < -70f -> onSwapTeamMembers(index, (index - 1).coerceAtLeast(0))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                StoragePanel(
                    title = "Storage",
                    count = "${storage.size}/${PlayerCollectionLimits.MaxStorageSize}",
                    width = 290.dp
                ) {
                    Row(
                        modifier = Modifier.height(204.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(storageScrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            storage.forEachIndexed { index, chimera ->
                                StorageChimeraRow(
                                    chimera = chimera,
                                    selected = selectedStorageIndex == index,
                                    isPrimary = false,
                                    onTap = { selectStorage(index) },
                                    onSwipeLeft = {
                                        onWithdrawStoredChimera(index)
                                    }
                                )
                            }
                        }

                        StorageScrollIndicator(
                            scrollFraction = if (storageScrollState.maxValue == 0) {
                                0f
                            } else {
                                storageScrollState.value / storageScrollState.maxValue.toFloat()
                            },
                            totalItems = storage.size,
                            modifier = Modifier
                                .width(6.dp)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

/** Draws a bordered storage section with a title, counter, and custom content. */
@Composable
private fun StoragePanel(
    title: String,
    count: String,
    width: Dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .width(width)
            .height(258.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.background.copy(alpha = 0.56f))
            .border(1.dp, colors.primary.copy(alpha = 0.36f), RoundedCornerShape(7.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = colors.primary, fontFamily = CinzelFamily, fontWeight = FontWeight.Bold)
            Text(count, color = colors.secondary, fontFamily = CinzelFamily, fontSize = 12.sp)
        }
        content()
    }
}

/** Renders the compact top-right close control for the storage overlay. */
@Composable
private fun CloseStorageButton(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.background.copy(alpha = 0.52f))
            .border(1.dp, colors.primary.copy(alpha = 0.48f), RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onClose() })
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "X",
            color = colors.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Draws one team slot with level, sprite, HP bar, and drag gestures. */
@Composable
private fun StorageTeamSlot(
    chimera: Chimera?,
    selected: Boolean,
    isPrimary: Boolean,
    onTap: () -> Unit,
    onDrag: (Float, Float) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }
    val hpRatio = chimera?.let {
        (it.stats.currentHp.toFloat() / it.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    } ?: 0f
    val contentAlpha = when {
        chimera == null -> 0.18f
        chimera.stats.isAlive() -> 1f
        else -> 0.45f
    }

    Box(
        modifier = Modifier
            .width(118.dp)
            .height(78.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surface.copy(alpha = if (selected) 0.86f else 0.58f))
            .border(
                width = if (selected || isPrimary) 2.dp else 1.dp,
                color = colors.primary.copy(
                    alpha = when {
                        selected -> 0.92f
                        isPrimary -> 0.76f
                        else -> 0.32f
                    }
                ),
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(chimera) {
                detectTapGestures(onTap = { if (chimera != null) onTap() })
            }
            .pointerInput(chimera) {
                detectDragGestures(
                    onDragStart = {
                        dragX = 0f
                        dragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                        dragY += dragAmount.y
                    },
                    onDragEnd = {
                        if (chimera != null && (abs(dragX) > 60f || abs(dragY) > 60f)) {
                            onDrag(dragX, dragY)
                        }
                    }
                )
            }
            .padding(7.dp),
        contentAlignment = Alignment.Center
    ) {
        if (chimera != null) {
            Text(
                text = "Lv.${chimera.level}",
                color = colors.secondary.copy(alpha = contentAlpha),
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            Image(
                painter = painterResource(id = chimera.species.teamImageRes()),
                contentDescription = chimera.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(52.dp)
                    .graphicsLayer { alpha = contentAlpha }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF2B190E).copy(alpha = 0.9f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(hpRatio)
                        .height(5.dp)
                        .background(hpBarColor(hpRatio))
                )
            }
        } else {
            Text(
                text = "Empty",
                color = colors.outline.copy(alpha = 0.58f),
                fontFamily = CinzelFamily,
                fontSize = 11.sp
            )
        }
    }
}

/** Draws one scrollable storage row and handles left-swipe withdrawal. */
@Composable
private fun StorageChimeraRow(
    chimera: Chimera?,
    selected: Boolean,
    isPrimary: Boolean,
    onTap: () -> Unit,
    onSwipeLeft: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var dragX by remember { mutableFloatStateOf(0f) }
    val alpha = if (chimera == null) 0.35f else 1f

    Row(
        modifier = Modifier
            .width(246.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.surface.copy(alpha = if (selected) 0.86f else 0.58f))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = colors.primary.copy(alpha = if (selected) 0.92f else 0.32f),
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(chimera) {
                detectTapGestures(onTap = { if (chimera != null) onTap() })
            }
            .pointerInput(chimera) {
                detectHorizontalDragGestures(
                    onDragStart = { dragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount
                    },
                    onDragEnd = {
                        if (chimera != null && dragX < -90f) {
                            onSwipeLeft()
                        }
                    }
                )
            }
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (chimera != null) {
            Image(
                painter = painterResource(id = chimera.species.teamImageRes()),
                contentDescription = chimera.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer { this.alpha = alpha }
            )
            Column {
                Text(
                    text = chimera.name,
                    color = colors.primary.copy(alpha = alpha),
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Lv.${chimera.level}  HP ${chimera.stats.currentHp}/${chimera.stats.maxHp}" +
                            if (isPrimary) "  Lead" else "",
                    color = colors.secondary.copy(alpha = alpha),
                    fontFamily = CinzelFamily,
                    fontSize = 10.sp
                )
            }
        } else {
            Text(
                text = "Empty",
                color = colors.outline.copy(alpha = 0.58f),
                fontFamily = CinzelFamily,
                fontSize = 12.sp
            )
        }
    }
}

/** Chooses the HP bar color according to the remaining health ratio. */
private fun hpBarColor(hpRatio: Float): Color =
    when {
        hpRatio > 0.5f -> Color(0xFF66C96A)
        hpRatio > 0.2f -> Color(0xFFE0B84B)
        else -> Color(0xFFD85A4A)
    }

/** Draws a menu-style vertical scrollbar beside the storage list. */
@Composable
private fun StorageScrollIndicator(
    scrollFraction: Float,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    BoxWithConstraints(
        modifier = modifier
            .clip(RoundedCornerShape(99.dp))
            .background(colors.surface.copy(alpha = 0.5f))
    ) {
        val visibleFraction = if (totalItems <= VisibleStorageRows) {
            1f
        } else {
            (VisibleStorageRows.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
        }
        val thumbHeight = maxHeight * visibleFraction
        val thumbTravel = maxHeight - thumbHeight

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = thumbTravel * scrollFraction)
                .width(6.dp)
                .height(thumbHeight)
                .clip(RoundedCornerShape(99.dp))
                .background(colors.primary.copy(alpha = if (totalItems > VisibleStorageRows) 0.85f else 0.45f))
        )
    }
}

private const val VisibleStorageRows = 4

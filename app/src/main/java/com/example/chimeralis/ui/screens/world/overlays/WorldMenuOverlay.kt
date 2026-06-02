package com.example.chimeralis.ui.screens.world.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.R
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.world.model.ExitAction
import com.example.chimeralis.ui.screens.world.SmallWorldMenuButton
import com.example.chimeralis.ui.screens.world.WorldInventoryColumns
import com.example.chimeralis.ui.screens.world.WorldInventorySlotCount
import com.example.chimeralis.ui.theme.CinzelFamily

/**
 * Renders the in game menu overlay UI.
 *
 * @param showSaveAndExit Flag that controls or describes show save and exit.
 * @param showSettings Flag that controls or describes show settings.
 * @param pendingExitAction The pending exit action value used by this operation.
 * @param pendingExitRequiresSave The pending exit requires save value used by this operation.
 * @param showSaveMessage Flag that controls or describes show save message.
 * @param musicEnabled The music enabled value used by this operation.
 * @param musicVolume The music volume value used by this operation.
 * @param soundEnabled The sound enabled value used by this operation.
 * @param soundVolume The sound volume value used by this operation.
 * @param encounterChance The encounter chance value used by this operation.
 * @param onResume Callback invoked when resume occurs.
 * @param onSettings Callback invoked when settings occurs.
 * @param onMusicEnabledChanged Callback invoked when music enabled changed occurs.
 * @param onMusicVolumeChanged Callback invoked when music volume changed occurs.
 * @param onSoundEnabledChanged Callback invoked when sound enabled changed occurs.
 * @param onSoundVolumeChanged Callback invoked when sound volume changed occurs.
 * @param onEncounterChanceChanged Callback invoked when encounter chance changed occurs.
 * @param onBackFromSubmenu Callback invoked when back from submenu occurs.
 * @param onSaveGame Callback invoked when save game occurs.
 * @param onMainMenu Callback invoked when main menu occurs.
 * @param onExitGame Callback invoked when exit game occurs.
 * @param onCancelExit Callback invoked when cancel exit occurs.
 * @param onExitWithSave Callback invoked when exit with save occurs.
 * @param onExitWithoutSave Callback invoked when exit without save occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun InGameMenuOverlay(
    showSaveAndExit: Boolean = true,
    showSettings: Boolean,
    pendingExitAction: ExitAction?,
    pendingExitRequiresSave: Boolean,
    showSaveMessage: Boolean,
    musicEnabled: Boolean,
    musicVolume: Float,
    soundEnabled: Boolean,
    soundVolume: Float,
    encounterChance: Float,
    onResume: () -> Unit,
    onSettings: () -> Unit,
    onMusicEnabledChanged: (Boolean) -> Unit,
    onMusicVolumeChanged: (Float) -> Unit,
    onSoundEnabledChanged: (Boolean) -> Unit,
    onSoundVolumeChanged: (Float) -> Unit,
    onEncounterChanceChanged: (Float) -> Unit,
    onBackFromSubmenu: () -> Unit,
    onSaveGame: () -> Unit,
    onMainMenu: () -> Unit,
    onExitGame: () -> Unit,
    onCancelExit: () -> Unit,
    onExitWithSave: () -> Unit,
    onExitWithoutSave: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
            .background(Color.Black.copy(alpha = 0.36f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(420.dp)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = when {
                    pendingExitAction != null && pendingExitRequiresSave -> "Save Progress?"
                    pendingExitAction != null -> "Are You Sure?"
                    showSettings -> "Settings"
                    else -> "Game Menu"
                },
                color = colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily
            )

            if (pendingExitAction != null && pendingExitRequiresSave) {
                Text(
                    text = "Do you want to save before leaving?",
                    color = colors.onSurface.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontFamily = CinzelFamily
                )
                MenuButton(text = "Save", onClick = onExitWithSave)
                MenuButton(text = "Don't Save", onClick = onExitWithoutSave)
                MenuButton(text = "Cancel", onClick = onCancelExit)
            } else if (pendingExitAction != null) {
                Text(
                    text = when (pendingExitAction) {
                        ExitAction.MainMenu -> "Return to the main menu?"
                        ExitAction.ExitGame -> "Exit the game?"
                    },
                    color = colors.onSurface.copy(alpha = 0.78f),
                    fontSize = 12.sp,
                    fontFamily = CinzelFamily
                )
                MenuButton(text = "Yes", onClick = onExitWithoutSave)
                MenuButton(text = "Cancel", onClick = onCancelExit)
            } else if (showSettings) {
                GameSettingsPanel(
                    musicEnabled = musicEnabled,
                    musicVolume = musicVolume,
                    soundEnabled = soundEnabled,
                    soundVolume = soundVolume,
                    encounterChance = encounterChance,
                    onMusicEnabledChanged = onMusicEnabledChanged,
                    onMusicVolumeChanged = onMusicVolumeChanged,
                    onSoundEnabledChanged = onSoundEnabledChanged,
                    onSoundVolumeChanged = onSoundVolumeChanged,
                    onEncounterChanceChanged = onEncounterChanceChanged
                )
                MenuButton(text = "Back", onClick = onBackFromSubmenu)
            } else {
                MenuButton(text = "Resume", onClick = onResume)
                if (showSaveAndExit) {
                    MenuButton(text = "Save", onClick = onSaveGame)
                }
                MenuButton(text = "Settings", onClick = onSettings)
                if (showSaveAndExit) {
                    MenuButton(text = "Main Menu", onClick = onMainMenu)
                    MenuButton(text = "Exit Game", onClick = onExitGame)
                }
            }
        }
        if (showSaveMessage) {
            SaveMessagePlate(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
            )
        }
    }
}

/**
 * Renders the world inventory panel UI.
 *
 * @param inventoryItems The inventory items value used by this operation.
 * @param selectedItem The selected item value used by this operation.
 * @param money The money value used by this operation.
 * @param onSelectedItemChanged Callback invoked when selected item changed occurs.
 * @param onClose Callback invoked when close occurs.
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun WorldInventoryPanel(
    inventoryItems: Map<Item, Int>,
    selectedItem: Item?,
    money: Int,
    onSelectedItemChanged: (Item?) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val filledSlots = inventoryItems.entries
        .sortedBy { it.key.name }
        .map { it.key to it.value }
    val slots = buildList {
        addAll(filledSlots)
        repeat((WorldInventorySlotCount - filledSlots.size).coerceAtLeast(0)) {
            add(null)
        }
    }.take(WorldInventorySlotCount)

    LaunchedEffect(inventoryItems, selectedItem) {
        val item = selectedItem
        if (item != null && item !in inventoryItems.keys) {
            onSelectedItemChanged(null)
        }
    }

    Box(
        modifier = modifier
            .width(198.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.58f))
            .border(1.dp, colors.primary.copy(alpha = 0.42f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory",
                    color = colors.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CinzelFamily
                )

                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(24.dp)
                ) {
                    SmallWorldMenuButton(text = "X", onClick = onClose)
                }
            }

            Text(
                text = "Coins: $money",
                color = colors.primary.copy(alpha = 0.92f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                modifier = Modifier.fillMaxWidth()
            )

            slots.chunked(WorldInventoryColumns).forEach { rowSlots ->
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    rowSlots.forEach { slot ->
                        WorldInventorySlot(
                            slot = slot,
                            isSelected = slot?.first == selectedItem,
                            onSelected = { item ->
                                onSelectedItemChanged(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the inventory item details plate UI.
 *
 * @param item Domain object used by this operation: item.
 * @param amount Numeric value used by this operation: amount.
 * @param canUse Flag that controls or describes can use.
 * @param onUse Callback invoked when use occurs.
 * @param onCancel Callback invoked when cancel occurs.
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun InventoryItemDetailsPlate(
    item: Item,
    amount: Int,
    canUse: Boolean,
    onUse: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.72f))
            .border(1.dp, colors.primary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${item.name} x$amount",
            color = colors.primary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            fontFamily = CinzelFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = item.description(),
            color = colors.onSurface.copy(alpha = 0.82f),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = CinzelFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MenuButton(
                text = "Use",
                enabled = canUse,
                onClick = onUse
            )
            MenuButton(
                text = "Cancel",
                onClick = onCancel
            )
        }
    }
}

/**
 * Renders the world inventory slot UI.
 *
 * @param slot The slot value used by this operation.
 * @param isSelected Flag that controls or describes is selected.
 * @param onSelected Callback invoked when selected occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun WorldInventorySlot(
    slot: Pair<Item, Int>?,
    isSelected: Boolean,
    onSelected: (Item) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val item = slot?.first
    val amount = slot?.second

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(colors.background.copy(alpha = if (item == null) 0.22f else 0.48f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = colors.primary.copy(
                    alpha = when {
                        isSelected -> 0.86f
                        item == null -> 0.22f
                        else -> 0.48f
                    }
                ),
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(item) {
                detectTapGestures(
                    onTap = {
                        if (item != null) {
                            onSelected(item)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (item != null && amount != null) {
            ItemIcon(item = item)

            Text(
                text = "x$amount",
                color = colors.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CinzelFamily,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp, bottom = 2.dp)
            )
        }
    }
}

/**
 * Renders the item icon UI.
 *
 * @param item Domain object used by this operation: item.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun ItemIcon(item: Item) {
    val imageRes = when (item.itemName) {
        ItemName.POTION -> R.drawable.potion
        ItemName.SUPER_POTION -> R.drawable.super_potion
        ItemName.REVIVE -> R.drawable.revive
        ItemName.BINDING_STONE -> R.drawable.binding_stone_base
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = item.name,
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(44.dp)
    )
}

/**
 * Handles description behavior.
 *
 * @receiver The item receiver used by this operation.
 * @return The text value produced by this operation.
 */
internal fun Item.description(): String {
    return when (itemName) {
        ItemName.POTION -> "Restores 20 HP to one chimera."
        ItemName.SUPER_POTION -> "Restores 60 HP to one chimera."
        ItemName.REVIVE -> "Revives a fainted chimera."
        ItemName.BINDING_STONE -> "Resonates with a wild chimera during battle."
    }
}

/**
 * Handles item icon res behavior.
 *
 * @param item Domain object used by this operation: item.
 * @return The calculated numeric value.
 */
internal fun itemIconRes(item: Item): Int {
    return when (item.itemName) {
        ItemName.POTION -> R.drawable.potion
        ItemName.SUPER_POTION -> R.drawable.super_potion
        ItemName.REVIVE -> R.drawable.revive
        ItemName.BINDING_STONE -> R.drawable.binding_stone_base
    }
}

/**
 * Renders the save message plate UI.
 *
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
internal fun SaveMessagePlate(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            colors.secondary.copy(alpha = 0.6f),
            colors.primary.copy(alpha = 0.9f),
            colors.secondary.copy(alpha = 0.6f)
        )
    )

    Box(
        modifier = modifier
            .width(184.dp)
            .height(42.dp),
        contentAlignment = Alignment.Center
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
                color = colors.background.copy(alpha = 0.75f)
            )
            drawPath(
                path = path,
                brush = gradientBrush,
                style = Stroke(width = 2f)
            )
        }

        Text(
            text = "Progress Saved",
            color = colors.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontFamily = CinzelFamily
        )
    }
}


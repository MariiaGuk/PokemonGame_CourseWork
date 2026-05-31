package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily

/** Renders the battle inventory buttons UI. */
@Composable
internal fun BattleInventoryButtons(
    inventoryItems: Map<Item, Int>,
    team: List<Chimera>,
    canUseCaptureItems: Boolean = true,
    onItemSelected: (Item) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (inventoryItems.isEmpty()) {
            Text(
                text = "Bag is empty",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = CinzelFamily,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
        } else {
            inventoryItems.entries.sortedBy { it.key.name }.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { (item, amount) ->
                        val canUseItem = if (item.isCaptureItem) {
                            canUseCaptureItems
                        } else {
                            team.any { item.canUseOn(it) }
                        }
                        MenuButton(
                            text = "${item.name} x$amount",
                            enabled = canUseItem,
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}

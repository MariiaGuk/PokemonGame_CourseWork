package com.example.chimeralis.ui.screens.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlin.math.roundToInt

/** Renders the empty battle team slot UI. */
@Composable
internal fun EmptyBattleTeamSlot() {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .width(122.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF3E443E).copy(alpha = 0.26f))
            .border(1.dp, colors.primary.copy(alpha = 0.18f), RoundedCornerShape(3.dp))
    )
}

/** Renders the battle team slot UI. */
@Composable
internal fun BattleTeamSlot(
    chimera: Chimera,
    isActive: Boolean,
    onSwitchSelected: (Chimera) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val canSwitch = !isActive && chimera.stats.isAlive()
    val hpRatio = (chimera.stats.currentHp.toFloat() / chimera.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    val hpPercent = (hpRatio * 100).roundToInt()
    val alpha = if (canSwitch || isActive) 1f else 0.42f

    Row(
        modifier = Modifier
            .width(122.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(
                if (isActive) Color(0xFF5B5F55).copy(alpha = 0.92f)
                else Color(0xFF3E443E).copy(alpha = 0.88f)
            )
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = colors.primary.copy(alpha = if (isActive) 0.88f else 0.44f),
                shape = RoundedCornerShape(3.dp)
            )
            .pointerInput(canSwitch, chimera) {
                detectTapGestures(
                    onTap = {
                        if (canSwitch) {
                            onSwitchSelected(chimera)
                        }
                    }
                )
            }
            .padding(3.dp)
            .graphicsLayer { this.alpha = alpha },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BattleSlotContent(
            chimera = chimera,
            hpRatio = hpRatio,
            hpPercent = hpPercent
        )
    }
}

/** Renders the battle item target slot UI. */
@Composable
internal fun BattleItemTargetSlot(
    chimera: Chimera,
    canUseItem: Boolean,
    onItemTargetSelected: (Chimera) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val hpRatio = (chimera.stats.currentHp.toFloat() / chimera.stats.maxHp.toFloat()).coerceIn(0f, 1f)
    val hpPercent = (hpRatio * 100).roundToInt()

    Row(
        modifier = Modifier
            .width(122.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(0xFF3E443E).copy(alpha = 0.88f))
            .border(
                width = if (canUseItem) 2.dp else 1.dp,
                color = colors.primary.copy(alpha = if (canUseItem) 0.78f else 0.22f),
                shape = RoundedCornerShape(3.dp)
            )
            .pointerInput(canUseItem, chimera) {
                detectTapGestures(
                    onTap = {
                        if (canUseItem) {
                            onItemTargetSelected(chimera)
                        }
                    }
                )
            }
            .padding(3.dp)
            .graphicsLayer { alpha = if (canUseItem) 1f else 0.42f },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BattleSlotContent(
            chimera = chimera,
            hpRatio = hpRatio,
            hpPercent = hpPercent
        )
    }
}

/** Renders the shared chimera summary inside battle selection slots. */
@Composable
private fun RowScope.BattleSlotContent(
    chimera: Chimera,
    hpRatio: Float,
    hpPercent: Int
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFCBD0C5).copy(alpha = 0.32f)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = chimera.species.battleImageRes()),
            contentDescription = chimera.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer { scaleX = -1f }
        )
    }

    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chimera.name,
                color = Color(0xFFE8E8D8),
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 7.sp,
                maxLines = 1
            )
            Text(
                text = "Lv.${chimera.level}",
                color = Color(0xFFE8E8D8),
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 7.sp,
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color(0xFF252818))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(hpRatio)
                    .fillMaxHeight()
                    .background(
                        when {
                            hpRatio > 0.5f -> Color(0xFF80D35D)
                            hpRatio > 0.2f -> Color(0xFFE0B84B)
                            else -> Color(0xFFD85A4A)
                        }
                    )
            )
        }

        Text(
            text = "HP: ${chimera.stats.currentHp}/${chimera.stats.maxHp} - $hpPercent%",
            color = Color(0xFFE8E8D8),
            fontFamily = CinzelFamily,
            fontSize = 6.sp,
            maxLines = 1
        )
    }
}

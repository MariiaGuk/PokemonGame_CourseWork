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
import com.example.chimeralis.logic.items.price
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
internal fun ServiceNpcDialogOverlay(
    interior: TownInterior,
    step: Int,
    message: String?,
    onNext: () -> Unit,
    onHeal: () -> Unit,
    onOpenShop: () -> Unit,
    onClose: () -> Unit
) {
    val isNurse = interior == TownInterior.ChimeraCenter
    val colors = MaterialTheme.colorScheme
    var portraitFrame by remember(interior, step) { mutableIntStateOf(0) }

    LaunchedEffect(interior, step) {
        while (true) {
            portraitFrame++
            delay(760L)
        }
    }

    val text = message ?: when {
        isNurse && step == 0 -> "Welcome to the Chimera Center."
        isNurse -> "Would you like me to heal your chimeras?"
        !isNurse && step == 0 -> "Welcome to the Chimera Store."
        else -> "Take a look. We have everything a trainer needs."
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.34f))
    ) {
        Image(
            painter = painterResource(id = serviceNpcDialogFrame(interior, step, portraitFrame)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 165.dp, bottom = 130.dp)
                .height(980.dp)
                .graphicsLayer(
                    scaleX = 2.05f,
                    scaleY = 2.05f,
                    transformOrigin = TransformOrigin(0.18f, 0.02f)
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
                text = text,
                color = colors.primary,
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                lineHeight = 23.sp,
                modifier = Modifier.weight(1f)
            )

            Column(
                modifier = Modifier.width(174.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    message != null -> MenuButton(text = "Close", onClick = onClose)
                    step == 0 -> MenuButton(text = "Next", onClick = onNext)
                    isNurse -> {
                        MenuButton(text = "Heal", onClick = onHeal)
                        MenuButton(text = "Cancel", onClick = onClose)
                    }
                    else -> {
                        MenuButton(text = "Shop", onClick = onOpenShop)
                        MenuButton(text = "Cancel", onClick = onClose)
                    }
                }
            }
        }
    }
}

@Composable
internal fun HealingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Healing...",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            letterSpacing = 3.sp
        )
    }
}

@Composable
internal fun ShopOverlay(
    money: Int,
    inventoryItems: Map<Item, Int>,
    message: String?,
    onBuyItem: (ItemName, Int) -> Unit,
    onClose: () -> Unit
) {
    var selectedAmounts by remember { mutableStateOf<Map<ItemName, Int>>(emptyMap()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.38f))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(560.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.76f), RoundedCornerShape(8.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shop",
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Coins: $money",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = CinzelFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(28.dp)
                ) {
                    SmallWorldMenuButton(text = "X", onClick = onClose)
                }
            }

            ItemName.values().forEach { itemName ->
                val amount = selectedAmounts[itemName] ?: 1
                val ownedAmount = inventoryItems.entries
                    .firstOrNull { it.key.itemName == itemName }
                    ?.value ?: 0
                val maxPurchasableAmount = money / itemName.price()
                val visibleAmount = amount.coerceAtMost(maxPurchasableAmount.coerceAtLeast(1))
                ShopItemRow(
                    itemName = itemName,
                    amount = visibleAmount,
                    ownedAmount = ownedAmount,
                    maxPurchasableAmount = maxPurchasableAmount,
                    canAfford = maxPurchasableAmount > 0 &&
                            money >= itemName.price() * visibleAmount,
                    onAmountChanged = { newAmount ->
                        selectedAmounts = selectedAmounts + (itemName to newAmount.coerceAtLeast(1))
                    },
                    onBuyItem = onBuyItem
                )
            }

            Text(
                text = message ?: "Choose an item to buy.",
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = CinzelFamily,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
internal fun ShopItemRow(
    itemName: ItemName,
    amount: Int,
    ownedAmount: Int,
    maxPurchasableAmount: Int,
    canAfford: Boolean,
    onAmountChanged: (Int) -> Unit,
    onBuyItem: (ItemName, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color(0xFF2F241D).copy(alpha = 0.42f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.34f), RoundedCornerShape(5.dp))
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = itemIconRes(ItemFactory.createItem(itemName))),
            contentDescription = itemName.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(34.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = itemName.displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                text = "${itemName.price()} coins each - Owned: $ownedAmount",
                color = MaterialTheme.colorScheme.primary,
                fontFamily = CinzelFamily,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(30.dp)
            ) {
                ShopStepperButton(
                    label = "-",
                    enabled = amount > 1,
                    onClick = { onAmountChanged(amount - 1) }
                )
            }
            Text(
                text = amount.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = CinzelFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(34.dp)
            )
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(30.dp)
            ) {
                ShopStepperButton(
                    label = "+",
                    enabled = maxPurchasableAmount > 0 && amount < maxPurchasableAmount,
                    onClick = { onAmountChanged(amount + 1) }
                )
            }
        }

        Text(
            text = "Total: ${itemName.price() * amount}",
            color = MaterialTheme.colorScheme.primary,
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp)
        )

        Box(
            modifier = Modifier
                .width(98.dp)
                .height(34.dp)
        ) {
            SmallWorldMenuButton(
                text = "Buy",
                enabled = canAfford,
                onClick = { onBuyItem(itemName, amount) }
            )
        }
    }
}

@Composable
internal fun ShopStepperButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surface.copy(alpha = if (enabled) 0.5f else 0.24f))
            .border(
                1.dp,
                colors.primary.copy(alpha = if (enabled) 0.72f else 0.22f),
                RoundedCornerShape(6.dp)
            )
            .pointerInput(enabled, onClick) {
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
        Text(
            text = label,
            color = colors.primary.copy(alpha = if (enabled) 1f else 0.35f),
            fontFamily = CinzelFamily,
            fontWeight = FontWeight.Black,
            fontSize = 13.sp
        )
    }
}


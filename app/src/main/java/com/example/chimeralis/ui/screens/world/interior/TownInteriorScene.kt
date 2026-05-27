package com.example.chimeralis.ui.screens.world.interior

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import com.example.chimeralis.R
import com.example.chimeralis.ui.screens.world.Direction
import com.example.chimeralis.ui.screens.world.MapColumns
import com.example.chimeralis.ui.screens.world.MapRows
import com.example.chimeralis.ui.screens.world.WorldZoom
import com.example.chimeralis.ui.screens.world.playerFrame
import com.example.chimeralis.ui.screens.world.serviceNpcIdleFrame
import com.example.chimeralis.ui.screens.world.locations.TownInterior
import com.example.chimeralis.ui.screens.world.locations.TownInteriorData
import kotlin.math.roundToInt

/** Renders the town interior scene UI. */
@Composable
internal fun BoxScope.TownInteriorScene(
    interior: TownInterior,
    interiorData: TownInteriorData,
    widthPx: Float,
    heightPx: Float,
    imageSizePx: Float,
    imageLeft: Float,
    imageTop: Float,
    tileSize: Float,
    animatedColumn: Float,
    animatedRow: Float,
    direction: Direction,
    isMoving: Boolean,
    animationFrame: Int,
    serviceNpcIdleFrame: Int
) {
    val density = LocalDensity.current
    val playerCenterX = imageLeft + (animatedColumn + 0.5f) * tileSize
    val playerCenterY = imageTop + (animatedRow + 0.5f) * tileSize
    val npcCenterX = imageLeft + (interiorData.npcColumn + 0.5f) * tileSize
    val npcCenterY = imageTop + (interiorData.npcRow + 0.5f) * tileSize
    val storageCenterX = interiorData.storageColumn?.let { column ->
        imageLeft + (column + 0.5f) * tileSize
    }
    val storageCenterY = interiorData.storageRow?.let { row ->
        imageTop + (row + 0.5f) * tileSize
    }
    val worldLikeTileSize = minOf(widthPx / MapColumns * WorldZoom, heightPx / MapRows * WorldZoom)
    val spriteWidth = worldLikeTileSize * 1.06f
    val spriteHeight = worldLikeTileSize * 1.62f
    val serviceNpcWidth = spriteWidth * 0.72f
    val serviceNpcHeight = spriteHeight * 0.96f

    Image(
        painter = painterResource(id = interiorData.backgroundRes),
        contentDescription = interiorData.buildingName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .align(Alignment.Center)
            .size(with(density) { imageSizePx.toDp() })
    )

    Image(
        painter = painterResource(id = serviceNpcIdleFrame(interior, serviceNpcIdleFrame)),
        contentDescription = interiorData.npcName,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (npcCenterX - serviceNpcWidth / 2f).roundToInt(),
                    y = (npcCenterY - serviceNpcHeight * 0.86f).roundToInt()
                )
            }
            .size(
                width = with(density) { serviceNpcWidth.toDp() },
                height = with(density) { serviceNpcHeight.toDp() }
            )
    )

    if (storageCenterX != null && storageCenterY != null) {
        Image(
            painter = painterResource(id = R.drawable.storage),
            contentDescription = "Chimera Storage",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (storageCenterX - spriteWidth * 0.45f).roundToInt(),
                        y = (storageCenterY - spriteHeight * 0.58f).roundToInt()
                    )
                }
                .size(
                    width = with(density) { (spriteWidth * 0.9f).toDp() },
                    height = with(density) { (spriteHeight * 0.72f).toDp() }
                )
        )
    }

    Image(
        painter = painterResource(id = playerFrame(direction, isMoving, animationFrame)),
        contentDescription = "Player",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (playerCenterX - spriteWidth / 2f).roundToInt(),
                    y = (playerCenterY - spriteHeight * 0.82f).roundToInt()
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
}

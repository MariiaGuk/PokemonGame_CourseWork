package com.example.chimeralis.ui.screens.world.locations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.chimeralis.R
import com.example.chimeralis.ui.screens.world.MapColumns
import com.example.chimeralis.ui.screens.world.MapRows
import kotlin.math.roundToInt

internal data class TownBuilding(
    val imageRes: Int,
    val column: Int,
    val row: Int,
    val columns: Int = 4,
    val rows: Int = 4
)

internal data class TownSign(
    val column: Int,
    val row: Int,
    val title: String,
    val body: String
)

internal val grassTownBuildings = listOf(
    TownBuilding(imageRes = R.drawable.town_building_library, column = 1, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.chimeracenter, column = 6, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.chimerastore, column = 11, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_shop, column = 16, row = 1, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_hotel, column = 1, row = 6, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_bank, column = 6, row = 6, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_cafe, column = 11, row = 6, columns = 4, rows = 3),
    TownBuilding(imageRes = R.drawable.town_building_hair_salon, column = 16, row = 6, columns = 4, rows = 3)
)

internal val grassTownSigns = listOf(
    TownSign(
        column = 1,
        row = 4,
        title = "Town Library",
        body = "Temporarily closed for renovations. Please check back later."
    ),
    TownSign(
        column = 6,
        row = 4,
        title = "Chimera Center",
        body = "Let your chimeras rest here. Our nurse will heal their wounds and restore their strength."
    ),
    TownSign(
        column = 11,
        row = 4,
        title = "Chimera Mart",
        body = "Stock up before your next journey. Potions, revives, and binding stones are available inside."
    ),
    TownSign(
        column = 16,
        row = 4,
        title = "General Store",
        body = "Temporarily closed for renovations. Please check back later."
    ),
    TownSign(
        column = 1,
        row = 9,
        title = "Trainer Inn",
        body = "Temporarily closed for renovations. Please check back later."
    ),
    TownSign(
        column = 6,
        row = 9,
        title = "Town Bank",
        body = "Temporarily closed for renovations. Please check back later."
    ),
    TownSign(
        column = 11,
        row = 9,
        title = "Chimera Cafe",
        body = "Temporarily closed for renovations. Please check back later."
    ),
    TownSign(
        column = 16,
        row = 9,
        title = "Style Salon",
        body = "Temporarily closed for renovations. Please check back later."
    )
)

internal val grassTownBuildingTiles = grassTownBuildings
    .flatMap { building ->
        (building.column until building.column + building.columns).flatMap { column ->
            (building.row until building.row + building.rows).map { row -> column to row }
        }
    }
    .toSet()

internal val grassTownPathTiles = buildSet {
    for (column in 0 until MapColumns) {
        add(column to 0)
        add(column to 4)
        add(column to 5)
        add(column to 9)
    }

    for (row in 0 until MapRows) {
        add(0 to row)
        add(5 to row)
        add(10 to row)
        add(15 to row)
        add(20 to row)
    }
}

@Composable
internal fun TownLocationTiles(
    mapLeft: Float,
    mapTop: Float,
    tileWidth: Float,
    tileHeight: Float,
    groundTexture: ImageBitmap,
    pathTexture: ImageBitmap
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        for (row in 0 until MapRows) {
            for (column in 0 until MapColumns) {
                val left = mapLeft + column * tileWidth
                val top = mapTop + row * tileHeight
                val tile = column to row
                val tileDstSize = IntSize(
                    width = (tileWidth + 1f).roundToInt(),
                    height = (tileHeight + 1f).roundToInt()
                )

                drawImage(
                    image = groundTexture,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(groundTexture.width, groundTexture.height),
                    dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                    dstSize = tileDstSize
                )

                if (tile in grassTownPathTiles) {
                    drawImage(
                        image = pathTexture,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(pathTexture.width, pathTexture.height),
                        dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                        dstSize = tileDstSize
                    )
                }
            }
        }
    }
}

@Composable
internal fun TownLocationBuildings(
    drawOverPlayer: Boolean,
    animatedRow: Float,
    mapLeft: Float,
    mapTop: Float,
    tileWidth: Float,
    tileHeight: Float
) {
    val density = LocalDensity.current

    grassTownBuildings.forEach { building ->
        val shouldDrawOverPlayer = animatedRow < building.row
        if (shouldDrawOverPlayer != drawOverPlayer) return@forEach

        val painter = painterResource(id = building.imageRes)
        val buildingWidth = tileWidth * building.columns
        val fallbackHeight = tileHeight * building.rows
        val intrinsicSize = painter.intrinsicSize
        val buildingHeight = if (intrinsicSize.width > 0f && intrinsicSize.height > 0f) {
            buildingWidth * intrinsicSize.height / intrinsicSize.width
        } else {
            fallbackHeight
        }
        val left = mapLeft + building.column * tileWidth
        val bottom = mapTop + (building.row + building.rows) * tileHeight
        val top = bottom - buildingHeight

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = left.roundToInt(),
                        y = top.roundToInt()
                    )
                }
                .size(
                    width = with(density) { buildingWidth.toDp() },
                    height = with(density) { buildingHeight.toDp() }
                )
        )
    }
}

@Composable
internal fun TownLocationSigns(
    mapLeft: Float,
    mapTop: Float,
    tileWidth: Float,
    tileHeight: Float
) {
    val density = LocalDensity.current

    grassTownSigns.forEach { sign ->
        val signWidth = tileWidth * 1.5f
        val signHeight = tileHeight * 1.5f
        val left = mapLeft + (sign.column + 0.9f) * tileWidth - signWidth / 2f
        val top = mapTop + (sign.row + 0.5f) * tileHeight - signHeight

        Image(
            painter = painterResource(id = R.drawable.sign),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = left.roundToInt(),
                        y = top.roundToInt()
                    )
                }
                .size(
                    width = with(density) { signWidth.toDp() },
                    height = with(density) { signHeight.toDp() }
                )
        )
    }
}

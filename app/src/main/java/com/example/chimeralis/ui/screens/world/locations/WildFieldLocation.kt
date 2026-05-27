package com.example.chimeralis.ui.screens.world.locations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.chimeralis.ui.screens.world.MapColumns
import com.example.chimeralis.ui.screens.world.MapRows
import kotlin.math.roundToInt

internal val grassTiles = setOf(
    3 to 2, 4 to 2, 5 to 2,
    3 to 3, 4 to 3, 5 to 3,
    12 to 2, 13 to 2, 14 to 2, 15 to 2, 16 to 2, 17 to 2, 18 to 2, 19 to 2,
    12 to 3, 13 to 3, 14 to 3, 15 to 3, 16 to 3, 17 to 3, 18 to 3, 19 to 3,
    7 to 5, 8 to 5, 9 to 5,
    7 to 6, 8 to 6, 9 to 6,
    11 to 7, 12 to 7, 13 to 7, 14 to 7,
    11 to 8, 12 to 8, 13 to 8, 14 to 8,
    2 to 7, 3 to 7, 18 to 7, 19 to 7,
    2 to 8, 3 to 8, 18 to 8, 19 to 8
)

/** Renders the wild field location tiles UI. */
@Composable
internal fun WildFieldLocationTiles(
    mapLeft: Float,
    mapTop: Float,
    tileWidth: Float,
    tileHeight: Float,
    groundTexture: ImageBitmap,
    grassTexture: ImageBitmap
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

                if (tile in grassTiles) {
                    drawImage(
                        image = grassTexture,
                        srcOffset = IntOffset.Zero,
                        srcSize = IntSize(grassTexture.width, grassTexture.height),
                        dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                        dstSize = tileDstSize
                    )
                }
            }
        }
    }
}

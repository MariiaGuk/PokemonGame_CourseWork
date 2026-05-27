package com.example.chimeralis.ui.screens.world.locations

import com.example.chimeralis.R

/** Stores town interior data data. */
data class TownInteriorData(
    val backgroundRes: Int,
    val walkableTiles: Set<Pair<Int, Int>>,
    val npcColumn: Int,
    val npcRow: Int,
    val storageColumn: Int? = null,
    val storageRow: Int? = null,
    val buildingName: String,
    val npcName: String
)

internal val TownInterior.data: TownInteriorData
    get() = when (this) {
        TownInterior.ChimeraCenter -> TownInteriorData(
            backgroundRes = R.drawable.chimeracenter_interior,
            walkableTiles = chimeraCenterWalkableTiles,
            npcColumn = 10,
            npcRow = 5,
            storageColumn = 5,
            storageRow = 5,
            buildingName = "Chimera Center",
            npcName = "Nurse"
        )
        TownInterior.ChimeraStore -> TownInteriorData(
            backgroundRes = R.drawable.chimerastore_interior,
            walkableTiles = chimeraStoreWalkableTiles,
            npcColumn = 10,
            npcRow = 5,
            buildingName = "Chimera Store",
            npcName = "Seller"
        )
    }

private val chimeraCenterWalkableTiles = buildSet {
    for (row in 5..14) {
        for (column in 1..14) {
            add(column to row)
        }
    }

    removeAll(
        buildSet {
            for (row in 5..11) {
                add(1 to row)
                add(2 to row)
                add(13 to row)
                add(14 to row)
            }
            add(5 to 5)
            for (column in 1..14) {
                if (column !in 7..8) {
                    add(column to 14)
                }
            }
        }
    )
}

private val chimeraStoreWalkableTiles = buildSet {
    for (row in 4..14) {
        for (column in 1..14) {
            add(column to row)
        }
    }

    removeAll(
        buildSet {
            for (column in 1..14) {
                if (column !in 7..8) {
                    add(column to 14)
                }
            }
            for (row in 4..10) {
                add(1 to row)
                add(2 to row)
                add(13 to row)
                add(14 to row)
            }
            for (row in 6..10) {
                add(4 to row)
                add(5 to row)
                add(10 to row)
                add(11 to row)
            }
            for (column in 5..12) {
                add(column to 4)
            }
        }
    )
}

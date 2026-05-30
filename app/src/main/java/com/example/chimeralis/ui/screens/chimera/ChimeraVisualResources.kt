package com.example.chimeralis.ui.screens.chimera

import androidx.compose.ui.graphics.Color
import com.example.chimeralis.R
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.moves.MoveName

/** Resolves the main image from the species visual definition. */
internal fun ChimeraSpecies.chimeraImageRes(): Int {
    val visuals = ChimeraFactory.speciesVisuals(this)
    return drawableResourceId(visuals.mainImage)
        ?: drawableResourceId(visuals.fallbackImage)
        ?: R.drawable.logo
}

/** Selects the starter card accent color from the species type. */
internal fun ChimeraSpecies.starterAccentColor(): Color {
    return when (ChimeraFactory.speciesType(this)) {
        ChimeraType.FIRE -> Color(0xFFFF6A2A)
        ChimeraType.GRASS -> Color(0xFF66C96A)
        ChimeraType.WATER -> Color(0xFF4EB4FF)
        ChimeraType.NORMAL -> Color(0xFFD8B66A)
    }
}

/** Selects the starter card shadow color from the species type. */
internal fun ChimeraSpecies.starterShadowColor(): Color {
    return when (ChimeraFactory.speciesType(this)) {
        ChimeraType.FIRE -> Color(0xFF5A1708)
        ChimeraType.GRASS -> Color(0xFF143D22)
        ChimeraType.WATER -> Color(0xFF0D3156)
        ChimeraType.NORMAL -> Color(0xFF423015)
    }
}

/** Resolves move animation frames from the species visual definition. */
internal fun ChimeraSpecies.battleMoveFrames(moveId: MoveName?): List<Int> {
    if (moveId == null) return emptyList()

    return ChimeraFactory.speciesVisuals(this)
        .moveFrames[moveId]
        .orEmpty()
        .mapNotNull(::drawableResourceId)
}

/** Finds a drawable id by the resource name configured in the catalog. */
private fun drawableResourceId(name: String): Int? {
    return runCatching {
        R.drawable::class.java.getField(name).getInt(null)
    }.getOrNull()
}

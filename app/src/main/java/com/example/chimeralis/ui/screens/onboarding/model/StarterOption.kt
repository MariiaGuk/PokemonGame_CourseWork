package com.example.chimeralis.ui.screens.onboarding.model

import androidx.compose.ui.graphics.Color
import com.example.chimeralis.logic.chimeras.ChimeraSpecies

/** Stores starter option data. */
internal data class StarterOption(
    val species: ChimeraSpecies,
    val accent: Color,
    val shadow: Color,
    val imageRes: Int
)

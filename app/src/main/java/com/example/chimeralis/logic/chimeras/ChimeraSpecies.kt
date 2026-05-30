package com.example.chimeralis.logic.chimeras

/** Defines stable chimera species identifiers. */
sealed class ChimeraSpecies {

    /** Identifies the Solignis species. */
    object Solignis : ChimeraSpecies()

    /** Identifies the Solflare species. */
    object Solflare : ChimeraSpecies()

    /** Identifies the Sunflare species. */
    object Sunflare : ChimeraSpecies()

    /** Identifies the Sylvarchon species. */
    object Sylvarchon : ChimeraSpecies()

    /** Identifies the Sylvhorn species. */
    object Sylvhorn : ChimeraSpecies()

    /** Identifies the Leviantis species. */
    object Leviantis : ChimeraSpecies()

    /** Identifies the Aquantis species. */
    object Aquantis : ChimeraSpecies()
}

package com.example.chimeralis.logic.chimeras
/** Defines the chimera species hierarchy. */
sealed class ChimeraSpecies(
    val evolvesInto: ChimeraSpecies? = null,
    val evolutionLevel: Int? = null
) {

    /** Provides solignis behavior. */
    object Solignis : ChimeraSpecies()

    /** Provides solflare behavior. */
    object Solflare : ChimeraSpecies(evolvesInto = Solignis, evolutionLevel = 36)

    /** Provides sunflare behavior. */
    object Sunflare : ChimeraSpecies(evolvesInto = Solflare, evolutionLevel = 16)

    /** Provides sylvhorn behavior. */
    object Sylvhorn : ChimeraSpecies()

    /** Provides aquantis behavior. */
    object Aquantis : ChimeraSpecies()
}
package com.example.chimeralis.logic.chimeras

sealed class ChimeraSpecies(
    val evolvesInto: ChimeraSpecies? = null,
    val evolutionLevel: Int? = null
) {
    object Solignis : ChimeraSpecies()
    object Solflare : ChimeraSpecies(evolvesInto = Solignis, evolutionLevel = 36)
    object Sunflare : ChimeraSpecies(evolvesInto = Solflare, evolutionLevel = 16)

    object Sylvhorn : ChimeraSpecies()
    object Aquantis : ChimeraSpecies()
}
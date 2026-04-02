package com.example.chimeralis.logic.chimeras

enum class ChimeraSpecies(
    val evolvesInto: ChimeraSpecies? = null,
    val evolutionLevel: Int? = null
){
    SOLIGNIS,
    SOLFLARE(evolvesInto = SOLIGNIS, evolutionLevel = 36),
    SUNFLARE(evolvesInto = SOLFLARE, evolutionLevel = 16),

    SYLVHORN(),
    AQUANTIS(),
}
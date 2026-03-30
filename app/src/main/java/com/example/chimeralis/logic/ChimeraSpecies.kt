package com.example.chimeralis.logic

enum class ChimeraSpecies(val evolvesInto: ChimeraSpecies? = null) {
    SOLIGNIS,
    SOLFLARE(evolvesInto = SOLIGNIS),
    SUNFLARE(evolvesInto = SOLFLARE),

    SYLVHORN(),
    AQUANTIS(),
}
package com.example.chimeralis.logic.chimeras

/** Creates chimera instances for services that should not depend on a concrete factory. */
fun interface ChimeraCreator {
    fun create(species: ChimeraSpecies, level: Int, ivStats: Stats): Chimera
}

/** Resolves chimera evolution rules without coupling the Chimera entity to factory logic. */
class ChimeraEvolutionService(
    private val catalog: ChimeraCatalog = ChimeraFactory.catalog,
    private val chimeraCreator: ChimeraCreator = ChimeraFactoryChimeraCreator
) {

    /** Returns true when the chimera has reached the configured evolution requirement. */
    fun canEvolve(chimera: Chimera): Boolean {
        val evolution = catalog.definitionFor(chimera.species).evolution ?: return false
        return chimera.level >= evolution.level
    }

    /** Creates the evolved chimera form while preserving nickname, IV stats, HP, and experience. */
    fun evolve(chimera: Chimera): Chimera? {
        if (!canEvolve(chimera)) return null

        val currentDefinition = catalog.definitionFor(chimera.species)
        val nextSpecies = currentDefinition.evolution?.evolvesInto ?: return null
        val evolved = chimeraCreator.create(
            nextSpecies,
            chimera.level,
            chimera.ivStats
        )

        if (chimera.name != currentDefinition.displayName) {
            evolved.rename(chimera.name)
        }

        val hpAfterEvolution = chimera.stats.currentHp + (evolved.stats.maxHp - chimera.stats.maxHp)
        evolved.stats.restoreHp(hpAfterEvolution)
        evolved.gainExp(chimera.exp)
        return evolved
    }
}

/** Default chimera creator backed by the configured application factory. */
private object ChimeraFactoryChimeraCreator : ChimeraCreator {
    override fun create(species: ChimeraSpecies, level: Int, ivStats: Stats): Chimera {
        return ChimeraFactory.createChimera(
            species = species,
            level = level,
            ivStats = ivStats
        )
    }
}

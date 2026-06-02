package com.example.chimeralis

import com.example.chimeralis.logic.battle.RandomProvider
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.items.Inventory
import com.example.chimeralis.logic.trainers.Player

internal fun zeroIvStats(): Stats = Stats(
    maxHp = 0,
    attack = 0,
    defence = 0,
    speed = 0
)

internal fun testChimera(
    species: ChimeraSpecies = ChimeraSpecies.Sunflare,
    level: Int = 5
): Chimera {
    return ChimeraFactory.createChimera(
        species = species,
        level = level,
        ivStats = zeroIvStats()
    )
}

internal fun testPlayer(
    vararg team: Chimera,
    name: String = "Tester",
    inventory: Inventory = Inventory()
): Player {
    return Player(
        name = name,
        team = team.toList(),
        inventory = inventory
    )
}

internal class FixedRandomProvider(
    doubles: List<Double> = emptyList(),
    ints: List<Int> = emptyList()
) : RandomProvider {
    private val doubleValues = ArrayDeque(doubles)
    private val intValues = ArrayDeque(ints)

    override fun nextDouble(): Double {
        return doubleValues.removeFirstOrNull() ?: 0.0
    }

    override fun nextInt(range: IntRange): Int {
        val value = intValues.removeFirstOrNull() ?: range.first
        return value.coerceIn(range.first, range.last)
    }
}

package com.example.chimeralis

import com.example.chimeralis.logic.battle.resolution.BattleMoveAccuracyResolver
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.Move
import com.example.chimeralis.logic.chimeras.moves.MoveExecutionResult
import com.example.chimeralis.logic.chimeras.moves.MoveFactory
import com.example.chimeralis.logic.chimeras.moves.MoveName
import com.example.chimeralis.logic.chimeras.moves.moveEffects.DamageEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.HealEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.RecoilEffect
import com.example.chimeralis.logic.chimeras.moves.moveEffects.StatChangeEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveAndEffectTest {

    @Test
    fun typeEffectivenessUsesExpectedTriangle() {
        assertEquals(2.0, ChimeraType.FIRE.typeEffectiveness(ChimeraType.GRASS), 0.01)
        assertEquals(0.5, ChimeraType.FIRE.typeEffectiveness(ChimeraType.WATER), 0.01)
        assertEquals(1.0, ChimeraType.FIRE.typeEffectiveness(ChimeraType.NORMAL), 0.01)
    }

    @Test
    fun moveConsumesPpAndDoesNotApplyEffectsWhenItMisses() {
        val attacker = testChimera()
        val target = testChimera(ChimeraSpecies.Sylvhorn)
        val initialHp = target.stats.currentHp
        val move = Move(
            id = MoveName.TACKLE,
            name = "Broken Move",
            type = ChimeraType.NORMAL,
            maxPp = 10,
            accuracy = 0,
            effects = listOf(DamageEffect(power = 100))
        )

        val result = move.execute(attacker, target, hits = false)

        assertEquals(MoveExecutionResult.Missed, result)
        assertEquals(9, move.pp)
        assertEquals(initialHp, target.stats.currentHp)
    }

    @Test
    fun moveWithoutPowerPointsReturnsNoPowerPointsWithoutChangingTarget() {
        val attacker = testChimera()
        val target = testChimera(ChimeraSpecies.Sylvhorn)
        val move = Move(
            id = MoveName.TACKLE,
            name = "One Shot",
            type = ChimeraType.NORMAL,
            maxPp = 1,
            accuracy = 100,
            effects = listOf(DamageEffect(power = 40))
        )

        assertEquals(MoveExecutionResult.Hit, move.execute(attacker, target, hits = true))
        val targetHpAfterFirstHit = target.stats.currentHp

        assertEquals(MoveExecutionResult.NoPowerPoints, move.execute(attacker, target, hits = true))
        assertEquals(0, move.pp)
        assertEquals(targetHpAfterFirstHit, target.stats.currentHp)
    }

    @Test
    fun accuracyResolverUsesInjectedRandomProvider() {
        val move = MoveFactory.createMove(MoveName.TACKLE)
        val resolverThatHits = BattleMoveAccuracyResolver(FixedRandomProvider(ints = listOf(100)))
        val resolverThatMisses = BattleMoveAccuracyResolver(FixedRandomProvider(ints = listOf(101)))

        assertTrue(resolverThatHits.moveHits(move))
        assertTrue(!resolverThatMisses.moveHits(move.copyWithAccuracy(50)))
    }

    @Test
    fun damageEffectUsesTypeEffectivenessAndStab() {
        val fireAttacker = testChimera(ChimeraSpecies.Sunflare, level = 8)
        val grassTarget = testChimera(ChimeraSpecies.Sylvhorn, level = 8)
        val waterTarget = testChimera(ChimeraSpecies.Aquantis, level = 8)

        val fireIntoGrass = DamageEffect.calculateDamageAmount(
            attacker = fireAttacker,
            target = grassTarget,
            moveType = ChimeraType.FIRE,
            power = 40
        )
        val fireIntoWater = DamageEffect.calculateDamageAmount(
            attacker = fireAttacker,
            target = waterTarget,
            moveType = ChimeraType.FIRE,
            power = 40
        )

        assertTrue(fireIntoGrass > fireIntoWater)
    }

    @Test
    fun statHealAndRecoilEffectsApplyToExpectedTargets() {
        val attacker = testChimera(ChimeraSpecies.Sunflare, level = 8)
        val target = testChimera(ChimeraSpecies.Sylvhorn, level = 8)
        val initialTargetAttack = target.stats.attack

        StatChangeEffect(
            statType = Stats.StatType.ATTACK,
            amount = -1,
            onTarget = true
        ).apply(attacker, target, ChimeraType.NORMAL)
        assertEquals(initialTargetAttack - 1, target.stats.attack)

        attacker.stats.takeDamage(10)
        val damagedHp = attacker.stats.currentHp
        HealEffect(healAmount = 5).apply(attacker, target, ChimeraType.NORMAL)
        assertEquals(damagedHp + 5, attacker.stats.currentHp)

        val attackerHpBeforeRecoil = attacker.stats.currentHp
        val targetHpBeforeRecoil = target.stats.currentHp
        RecoilEffect(power = 40, recoilPercent = 50).apply(attacker, target, ChimeraType.NORMAL)

        assertTrue(target.stats.currentHp < targetHpBeforeRecoil)
        assertTrue(attacker.stats.currentHp < attackerHpBeforeRecoil)
    }

    private fun Move.copyWithAccuracy(accuracy: Int): Move {
        return Move(
            id = id,
            name = name,
            type = type,
            maxPp = maxPp,
            accuracy = accuracy,
            effects = emptyList()
        )
    }
}

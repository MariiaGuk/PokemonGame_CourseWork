package com.example.chimeralis.logic.battle.reporting

import com.example.chimeralis.logic.battle.model.BattleStatsSnapshot
import com.example.chimeralis.logic.chimeras.Stats

/**
 * Converts data into battle stats snapshot.
 *
 * @receiver The stats receiver used by this operation.
 * @return The resulting BattleStatsSnapshot value.
 */
fun Stats.toBattleStatsSnapshot(): BattleStatsSnapshot {
    return BattleStatsSnapshot(
        currentHp = currentHp,
        maxHp = maxHp,
        attack = attack,
        defence = defence,
        speed = speed,
        attackStage = attackStage,
        defenceStage = defenceStage,
        speedStage = speedStage
    )
}

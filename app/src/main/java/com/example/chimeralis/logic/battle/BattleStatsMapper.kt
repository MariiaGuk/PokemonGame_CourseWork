package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.chimeras.Stats

/** Converts data into battle stats snapshot. */
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

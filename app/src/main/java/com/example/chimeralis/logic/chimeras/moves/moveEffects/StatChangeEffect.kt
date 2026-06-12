package com.example.chimeralis.logic.chimeras.moves.moveEffects

import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats

/** Changes one battle stat stage on the attacker or target. */
class StatChangeEffect(
    private val statType: Stats.StatType,
    private val amount: Int,
    private val onTarget: Boolean = true
): MoveEffect {

    /**
     * Applies the stat-stage change to the configured subject.
     *
     * @param attacker The attacker value used by this operation.
     * @param target The target value used by this operation.
     * @param moveType The move type value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    override fun apply(attacker: Chimera, target: Chimera, moveType: ChimeraType)
    {
        val subject = if (onTarget) target else attacker

        subject.stats.modifyStat(statType, amount)
    }
}

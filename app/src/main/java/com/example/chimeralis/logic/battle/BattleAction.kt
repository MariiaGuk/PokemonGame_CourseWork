package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move

/** Defines the battle action hierarchy. */
sealed class BattleAction {

    /** Stores use move data. */
    data class UseMove(val move: Move) : BattleAction()

    /** Stores use item data. */
    data class UseItem(val item: Item, val target: Chimera? = null) : BattleAction()

    /** Stores switch chimera data. */
    data class SwitchChimera(val chimera: Chimera) : BattleAction()

    /** Provides run behavior. */
    object Run : BattleAction()
}

package com.example.chimeralis.logic.battle

import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.moves.Move

sealed class BattleAction {
    data class UseMove(val move: Move) : BattleAction()
    data class UseItem(val item: Item, val target: Chimera? = null) : BattleAction()
    data class SwitchChimera(val chimera: Chimera) : BattleAction()
    object Run : BattleAction()
}

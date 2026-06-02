package com.example.chimeralis.ui.screens.battle.presentation.model

import com.example.chimeralis.logic.items.Item

/** Stores one item option with its domain payload. */
internal data class BattleItemOptionPresentation(
    val item: Item,
    val label: String,
    val enabled: Boolean,
    val isCaptureItem: Boolean
)

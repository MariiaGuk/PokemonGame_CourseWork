package com.example.chimeralis.data.save

import com.example.chimeralis.logic.items.ItemName

/** Serializable inventory entry with item type and amount. */
data class SavedItem(
    val itemName: ItemName,
    val amount: Int
)

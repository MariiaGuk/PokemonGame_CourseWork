package com.example.chimeralis.logic.items

/**
 * Handles price behavior.
 *
 * @receiver The item name receiver used by this operation.
 * @return The calculated numeric value.
 */
fun ItemName.price(): Int = when (this) {
    ItemName.POTION -> 30
    ItemName.SUPER_POTION -> 80
    ItemName.REVIVE -> 120
    ItemName.BINDING_STONE -> 100
}

package com.example.chimeralis.logic.items

enum class ItemKind {
    Healing,
    Revival,
    Capture
}

enum class ItemName(
    val displayName: String,
    val kind: ItemKind
) {
    POTION("Potion", ItemKind.Healing),
    SUPER_POTION("Super Potion", ItemKind.Healing),
    REVIVE("Revive", ItemKind.Revival),
    BINDING_STONE("Binding Stone", ItemKind.Capture)
}

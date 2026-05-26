package com.example.chimeralis.ui.navigation

enum class GameScreen {
    Splash,
    MainMenu,
    Continue,
    TrainerName,
    StarterSelection,
    LavaField,
    GrassField,
    ChimeraCenterInterior,
    ChimeraStoreInterior,
    Battle;

    val isLocation: Boolean
        get() = when (this) {
            LavaField,
            GrassField,
            ChimeraCenterInterior,
            ChimeraStoreInterior -> true
            else -> false
        }
}

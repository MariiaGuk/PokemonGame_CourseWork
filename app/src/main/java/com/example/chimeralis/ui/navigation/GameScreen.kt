package com.example.chimeralis.ui.navigation

/** Lists the game screen values. */
enum class GameScreen {
    Splash,
    MainMenu,
    Continue,
    TrainerName,
    StarterSelection,
    Tutorial,
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

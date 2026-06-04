package com.example.chimeralis.logic.trainers

/** Stores reusable lines spoken by a non-player trainer. */
data class NPCDialogue(
    val challengeLines: List<String> = emptyList(),
    val battleOpeningLine: String? = null,
    val nextChimeraLine: String? = null,
    val defeatLine: String? = null
) {
    /**
     * Returns the challenge dialog line for the current dialog step.
     *
     * @param step Numeric value used by this operation: step.
     * @return The resolved line value, or null when it is unavailable.
     */
    fun challengeLine(step: Int): String? {
        return challengeLines.getOrNull(step) ?: challengeLines.lastOrNull()
    }

    /**
     * Returns the battle opening line with the current trainer and chimera names.
     *
     * @param npcName The npc name value used by this operation.
     * @param chimeraName The chimera name value used by this operation.
     * @return The resolved line value, or null when it is unavailable.
     */
    fun battleOpening(npcName: String, chimeraName: String): String? {
        return battleOpeningLine?.withBattleNames(npcName, chimeraName)
    }

    /**
     * Returns the line spoken before the next enemy chimera is sent out.
     *
     * @param npcName The npc name value used by this operation.
     * @param chimeraName The chimera name value used by this operation.
     * @return The resolved line value, or null when it is unavailable.
     */
    fun nextChimera(npcName: String, chimeraName: String): String? {
        return nextChimeraLine?.withBattleNames(npcName, chimeraName)
    }

    /**
     * Returns the line spoken when the NPC has been defeated.
     *
     * @param npcName The npc name value used by this operation.
     * @return The resolved line value, or null when it is unavailable.
     */
    fun defeat(npcName: String): String? {
        return defeatLine?.withBattleNames(npcName, chimeraName = "")
    }

    private fun String.withBattleNames(npcName: String, chimeraName: String): String {
        return replace("{npc}", npcName).replace("{chimera}", chimeraName)
    }
}

/** Shared NPC dialog presets used by world interactions and battle scenarios. */
object NpcDialogues {
    val WildEncounter = NPCDialogue(
        battleOpeningLine = "A wild {chimera} appeared!"
    )

    val RivalTrainer = NPCDialogue(
        challengeLines = listOf(
            "Hey! You look like you have a strong team. I have been waiting for a real challenge.",
            "Want to test your chimeras against mine?"
        ),
        battleOpeningLine = "{npc} challenged you!",
        nextChimeraLine = "{npc}: Not bad. Let's see how you handle {chimera}!",
        defeatLine = "{npc}: You battled well. I need more training."
    )
}

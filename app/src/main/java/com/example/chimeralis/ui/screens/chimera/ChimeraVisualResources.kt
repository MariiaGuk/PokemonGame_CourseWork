package com.example.chimeralis.ui.screens.chimera

import androidx.compose.ui.graphics.Color
import com.example.chimeralis.logic.chimeras.catalog.ChimeraCatalog
import com.example.chimeralis.logic.chimeras.catalog.ChimeraDefinition
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.moves.MoveName
import com.example.chimeralis.R

private var validatedCatalog: ChimeraCatalog? = null

/**
 * Validates that all chimera visual resource names exist in drawable resources.
 *
 * @param catalog Domain object used by this operation: catalog.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
internal fun validateChimeraVisualResourceMappings(
    catalog: ChimeraCatalog = ChimeraFactory.catalog
) {
    if (validatedCatalog === catalog) return

    val errors = mutableListOf<String>()

    catalog.definitions.forEachIndexed { index, definition ->
        validateDefinitionVisualResources(index, definition, errors)
    }

    if (errors.isNotEmpty()) {
        throw IllegalArgumentException(
            buildString {
                appendLine("Invalid chimera visual resource mappings:")
                errors.forEach { error -> appendLine("- $error") }
            }.trimEnd()
        )
    }

    validatedCatalog = catalog
}

/**
 * Resolves the main image from the species visual definition.
 *
 * @receiver The chimera species receiver used by this operation.
 * @return The calculated numeric value.
 */
internal fun ChimeraSpecies.chimeraImageRes(): Int {
    validateChimeraVisualResourceMappings()

    val visuals = ChimeraFactory.speciesVisuals(this)
    return requireDrawableResourceId(
        name = visuals.mainImage,
        usage = "${catalogName()} mainImage"
    )
}

/**
 * Selects the starter card accent color from the species type.
 *
 * @receiver The chimera species receiver used by this operation.
 * @return The resulting Color value.
 */
internal fun ChimeraSpecies.starterAccentColor(): Color {
    return when (ChimeraFactory.speciesType(this)) {
        ChimeraType.FIRE -> Color(0xFFFF6A2A)
        ChimeraType.GRASS -> Color(0xFF66C96A)
        ChimeraType.WATER -> Color(0xFF4EB4FF)
        ChimeraType.NORMAL -> Color(0xFFD8B66A)
    }
}

/**
 * Selects the starter card shadow color from the species type.
 *
 * @receiver The chimera species receiver used by this operation.
 * @return The resulting Color value.
 */
internal fun ChimeraSpecies.starterShadowColor(): Color {
    return when (ChimeraFactory.speciesType(this)) {
        ChimeraType.FIRE -> Color(0xFF5A1708)
        ChimeraType.GRASS -> Color(0xFF143D22)
        ChimeraType.WATER -> Color(0xFF0D3156)
        ChimeraType.NORMAL -> Color(0xFF423015)
    }
}

/**
 * Resolves move animation frames from the species visual definition.
 *
 * @receiver The chimera species receiver used by this operation.
 * @param moveId The move id value used by this operation.
 * @return The collection produced by this operation.
 */
internal fun ChimeraSpecies.battleMoveFrames(moveId: MoveName?): List<Int> {
    if (moveId == null) return emptyList()

    validateChimeraVisualResourceMappings()

    return ChimeraFactory.speciesVisuals(this)
        .moveFrames[moveId]
        .orEmpty()
        .mapIndexed { index, frameName ->
            requireDrawableResourceId(
                name = frameName,
                usage = "${catalogName()} $moveId move frame #$index"
            )
        }
}

/**
 * Validates every visual resource configured for one definition.
 *
 * @param index Numeric value used by this operation: index.
 * @param definition The definition value used by this operation.
 * @param errors The errors value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
private fun validateDefinitionVisualResources(
    index: Int,
    definition: ChimeraDefinition,
    errors: MutableList<String>
) {
    val label = definition.catalogLabel(index)
    val visuals = definition.visuals

    validateDrawableResource(label, "mainImage", visuals.mainImage, errors)
    validateDrawableResource(label, "fallbackImage", visuals.fallbackImage, errors)
    visuals.moveFrames.forEach { (moveName, frames) ->
        frames.forEachIndexed { frameIndex, frameName ->
            validateDrawableResource(label, "$moveName frame #$frameIndex", frameName, errors)
        }
    }
}

/**
 * Validates one drawable resource name and appends a detailed error if it is missing.
 *
 * @param label The label value used by this operation.
 * @param usage The usage value used by this operation.
 * @param name The name value used by this operation.
 * @param errors The errors value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
private fun validateDrawableResource(
    label: String,
    usage: String,
    name: String,
    errors: MutableList<String>
) {
    if (drawableResourceId(name) == null) {
        errors.add("$label references missing drawable resource '$name' for $usage.")
    }
}

/**
 * Resolves a drawable id or throws a resource-specific configuration error.
 *
 * @param name The name value used by this operation.
 * @param usage The usage value used by this operation.
 * @return The calculated numeric value.
 */
private fun requireDrawableResourceId(name: String, usage: String): Int {
    return drawableResourceId(name)
        ?: throw IllegalArgumentException("Missing drawable resource '$name' for $usage.")
}

/**
 * Finds a drawable id by the resource name configured in the catalog.
 *
 * @param name The name value used by this operation.
 * @return The resolved int value, or null when it is unavailable.
 */
private fun drawableResourceId(name: String): Int? {
    return drawableResourceIds[name]
}

/**
 * Formats one catalog definition for visual resource errors.
 *
 * @receiver The chimera definition receiver used by this operation.
 * @param index Numeric value used by this operation: index.
 * @return The text value produced by this operation.
 */
private fun ChimeraDefinition.catalogLabel(index: Int): String {
    return "definition #$index (${species.catalogName()}, displayName='$displayName')"
}

/**
 * Formats one species identifier for visual resource errors.
 *
 * @receiver The chimera species receiver used by this operation.
 * @return The text value produced by this operation.
 */
private fun ChimeraSpecies.catalogName(): String {
    return javaClass.simpleName
}

private val drawableResourceIds: Map<String, Int> by lazy {
    R.drawable::class.java.fields.associate { field ->
        field.name to field.getInt(null)
    }
}

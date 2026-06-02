package com.example.chimeralis

import com.example.chimeralis.logic.chimeras.catalog.ChimeraAvailability
import com.example.chimeralis.logic.chimeras.catalog.ChimeraCatalog
import com.example.chimeralis.logic.chimeras.catalog.ChimeraDefinition
import com.example.chimeralis.logic.chimeras.catalog.ChimeraEvolution
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.catalog.ChimeraVisualSet
import com.example.chimeralis.logic.chimeras.catalog.DefaultChimeraCatalog
import com.example.chimeralis.logic.chimeras.catalog.LearnableMove
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.logic.chimeras.moves.DefaultMoveCatalog
import com.example.chimeralis.logic.chimeras.moves.MoveCatalog
import com.example.chimeralis.logic.chimeras.moves.MoveFactory
import com.example.chimeralis.logic.chimeras.moves.MoveName
import com.example.chimeralis.logic.items.DefaultItemCatalog
import com.example.chimeralis.logic.items.ItemCatalog
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class CatalogFactoryTest {

    @Test
    fun defaultFactoriesCoverConfiguredCatalogs() {
        DefaultChimeraCatalog.definitions.forEach { definition ->
            val chimera = ChimeraFactory.createChimera(definition.species, level = 1, ivStats = zeroIvStats())

            assertSame(definition.species, chimera.species)
            assertEquals(definition.displayName, chimera.name)
            assertTrue(chimera.moves.isNotEmpty())
        }

        MoveName.values().forEach { moveName ->
            assertEquals(moveName, MoveFactory.createMove(moveName).id)
        }

        ItemName.values().forEach { itemName ->
            assertEquals(itemName, ItemFactory.createItem(itemName).itemName)
        }
    }

    @Test
    fun factoriesCreateIndependentRuntimeInstances() {
        val firstChimera = ChimeraFactory.createChimera(ChimeraSpecies.Sunflare, level = 6, ivStats = zeroIvStats())
        val secondChimera = ChimeraFactory.createChimera(ChimeraSpecies.Sunflare, level = 6, ivStats = zeroIvStats())
        val firstMove = MoveFactory.createMove(MoveName.TACKLE)
        val secondMove = MoveFactory.createMove(MoveName.TACKLE)
        val firstItem = ItemFactory.createItem(ItemName.POTION)
        val secondItem = ItemFactory.createItem(ItemName.POTION)

        firstChimera.moves.first().restorePp(0)
        firstMove.restorePp(0)

        assertNotSame(firstChimera, secondChimera)
        assertNotSame(firstChimera.moves.first(), secondChimera.moves.first())
        assertTrue(secondChimera.moves.first().pp > 0)
        assertNotSame(firstMove, secondMove)
        assertTrue(secondMove.pp > 0)
        assertNotSame(firstItem, secondItem)
    }

    @Test
    fun chimeraFactorySupportsDisplayNamesClassNamesAndLegacyAliases() {
        assertSame(ChimeraSpecies.Sunflare, ChimeraFactory.speciesByName("Sunflare"))
        assertSame(ChimeraSpecies.Sunflare, ChimeraFactory.speciesByName("sun flare"))
        assertSame(ChimeraSpecies.Sunflare, ChimeraFactory.speciesByName("Sunflare"))
        assertSame(ChimeraSpecies.Aquantis, ChimeraFactory.speciesByName("Aquantis"))
    }

    @Test
    fun chimeraCatalogValidationReportsMultipleConfigurationProblems() {
        val error = expectIllegalArgument {
            ChimeraFactory.configureCatalog(
                object : ChimeraCatalog {
                    override val definitions = listOf(
                        invalidDefinition(
                            species = ChimeraSpecies.Sunflare,
                            displayName = "",
                            learnset = emptyList(),
                            availability = ChimeraAvailability(),
                            visuals = ChimeraVisualSet(mainImage = "", fallbackImage = "")
                        ),
                        invalidDefinition(
                            species = ChimeraSpecies.Sunflare,
                            displayName = "Duplicate",
                            learnset = listOf(LearnableMove(0, MoveName.TACKLE)),
                            evolution = ChimeraEvolution(ChimeraSpecies.Sunflare, level = 1),
                            availability = ChimeraAvailability()
                        )
                    )
                }
            )
        }

        assertTrue(error.message.orEmpty().contains("Invalid chimera catalog"))
        assertTrue(error.message.orEmpty().contains("defined more than once"))
        assertTrue(error.message.orEmpty().contains("blank displayName"))
        assertTrue(error.message.orEmpty().contains("empty learnset"))
        assertTrue(error.message.orEmpty().contains("cannot evolve into itself"))
        assertTrue(error.message.orEmpty().contains("Catalog must contain at least one starter chimera"))
    }

    @Test
    fun moveAndItemCatalogValidationRejectMissingDefinitions() {
        val moveError = expectIllegalArgument {
            MoveFactory.configureCatalog(
                object : MoveCatalog {
                    override val definitions = DefaultMoveCatalog.definitions
                        .filterNot { definition -> definition.id == MoveName.TACKLE }
                }
            )
        }
        val itemError = expectIllegalArgument {
            ItemFactory.configureCatalog(
                object : ItemCatalog {
                    override val definitions = DefaultItemCatalog.definitions
                        .filterNot { definition -> definition.itemName == ItemName.POTION }
                }
            )
        }

        assertTrue(moveError.message.orEmpty().contains("missing definitions"))
        assertTrue(itemError.message.orEmpty().contains("missing definitions"))
    }

    @Test
    fun defaultVisualMappingsContainRequiredResourceNames() {
        DefaultChimeraCatalog.definitions.forEach { definition ->
            assertTrue(definition.visuals.mainImage.isNotBlank())
            assertTrue(definition.visuals.fallbackImage.isNotBlank())
            definition.visuals.moveFrames.values.flatten().forEach { frameName ->
                assertTrue(frameName.isNotBlank())
            }
        }
    }

    private fun invalidDefinition(
        species: ChimeraSpecies,
        displayName: String,
        learnset: List<LearnableMove>,
        evolution: ChimeraEvolution? = null,
        availability: ChimeraAvailability = ChimeraAvailability(starter = true, wild = true, trainerBattle = true),
        visuals: ChimeraVisualSet = ChimeraVisualSet(mainImage = "main", fallbackImage = "fallback")
    ): ChimeraDefinition {
        return ChimeraDefinition(
            species = species,
            displayName = displayName,
            type = ChimeraType.FIRE,
            baseStatsFactory = { Stats(39, 52, 43, 65) },
            learnset = learnset,
            evolution = evolution,
            availability = availability,
            visuals = visuals
        )
    }

    private fun expectIllegalArgument(block: () -> Unit): IllegalArgumentException {
        return try {
            block()
            fail("Expected IllegalArgumentException")
            throw AssertionError("Unreachable")
        } catch (error: IllegalArgumentException) {
            error
        }
    }
}

package com.example.pokemon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pokemon.R
import com.example.pokemon.logic.pokemons.PokemonFactory
import com.example.pokemon.logic.pokemons.PokemonSpecies
import com.example.pokemon.logic.pokemons.PokemonType
import com.example.pokemon.logic.pokemons.Stats
import com.example.pokemon.ui.components.MenuButton
import com.example.pokemon.ui.theme.CinzelFamily

private data class StarterOption(
    val species: PokemonSpecies,
    val accent: Color,
    val shadow: Color,
    val imageRes: Int
)

@Composable
fun StarterSelectionScreen(
    onStarterSelected: (PokemonSpecies) -> Unit,
    onBack: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val starters = listOf(
        StarterOption(
            species = PokemonSpecies.Charmander,
            accent = Color(0xFFFF6A2A),
            shadow = Color(0xFF5A1708),
            imageRes = R.drawable.starter_fire
        ),
        StarterOption(
            species = PokemonSpecies.Bulbasaur,
            accent = Color(0xFF66C96A),
            shadow = Color(0xFF143D22),
            imageRes = R.drawable.starter_grass
        ),
        StarterOption(
            species = PokemonSpecies.Squirtle,
            accent = Color(0xFF4EB4FF),
            shadow = Color(0xFF0D3156),
            imageRes = R.drawable.starter_water
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF542012), colors.background, Color(0xFF120806)),
                    center = Offset(180f, 80f),
                    radius = 950f
                )
            )
            .padding(horizontal = 28.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Your Starter",
                color = colors.primary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                fontFamily = CinzelFamily
            )

            Text(
                text = "Your first partner will lead the opening battle.",
                color = colors.onSurface.copy(alpha = 0.78f),
                fontSize = 11.sp,
                fontFamily = CinzelFamily
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                starters.forEach { starter ->
                    StarterCard(
                        starter = starter,
                        onClick = { onStarterSelected(starter.species) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            MenuButton(text = "Back", onClick = onBack)
        }
    }
}

@Composable
private fun StarterCard(
    starter: StarterOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val pokemon = remember(starter.species) {
        PokemonFactory.createPokemon(starter.species)
    }
    val stats = remember(pokemon) {
        pokemon.baseStats.asUiStats()
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        starter.shadow.copy(alpha = 0.92f),
                        colors.surface.copy(alpha = 0.94f)
                    )
                )
            )
            .border(1.dp, starter.accent.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StarterImage(starter = starter, name = pokemon.name)

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = pokemon.name,
            color = colors.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            fontFamily = CinzelFamily
        )

        Text(
            text = "${pokemon.type.displayName()} / ${pokemon.type.battleTrait()}",
            color = starter.accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            fontFamily = CinzelFamily
        )

        Spacer(modifier = Modifier.height(6.dp))

        StatsGrid(stats = stats, accent = starter.accent)

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StarterImage(starter: StarterOption, name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(starter.accent.copy(alpha = 0.08f))
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = starter.imageRes),
            contentDescription = name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun Stats.asUiStats(): List<Pair<String, Int>> = listOf(
    "HP" to maxHp,
    "ATK" to attack,
    "DEF" to defence,
    "SPD" to speed
)

private fun PokemonType.displayName(): String {
    val lower = name.lowercase()
    return lower.replaceFirstChar { it.uppercase() }
}

private fun PokemonType.battleTrait(): String = when (this) {
    PokemonType.FIRE -> "Fast striker"
    PokemonType.GRASS -> "Steady fighter"
    PokemonType.WATER -> "Tough defender"
    PokemonType.NORMAL -> "Balanced fighter"
}

@Composable
private fun StatsGrid(
    stats: List<Pair<String, Int>>,
    accent: Color
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        stats.chunked(2).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowStats.forEach { (name, value) ->
                    StatRow(
                        name = name,
                        value = value,
                        accent = accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    name: String,
    value: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(36.dp),
            fontFamily = CinzelFamily
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((value / 100f).coerceIn(0f, 1f))
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(accent)
            )
        }

        Text(
            text = value.toString(),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(18.dp),
            fontFamily = CinzelFamily
        )
    }
}

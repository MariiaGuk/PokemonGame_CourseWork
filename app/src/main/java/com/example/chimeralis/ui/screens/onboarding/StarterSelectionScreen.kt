package com.example.chimeralis.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.chimeras.ChimeraType
import com.example.chimeralis.logic.chimeras.Stats
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.chimera.chimeraImageRes
import com.example.chimeralis.ui.screens.chimera.starterAccentColor
import com.example.chimeralis.ui.screens.chimera.starterShadowColor
import com.example.chimeralis.ui.theme.CinzelFamily

/** Stores starter option data. */
private data class StarterOption(
    val species: ChimeraSpecies,
    val accent: Color,
    val shadow: Color,
    val imageRes: Int
)

/** Renders the starter selection screen UI. */
@Composable
fun StarterSelectionScreen(
    onStarterSelected: (ChimeraSpecies, String) -> Unit,
    onBack: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var selectedStarter by remember { mutableStateOf<StarterOption?>(null) }
    var chimeraName by remember { mutableStateOf(TextFieldValue("")) }
    var isChimeraNameFocused by remember { mutableStateOf(false) }
    val starters = ChimeraFactory.starterSpecies().map { species ->
        StarterOption(
            species = species,
            accent = species.starterAccentColor(),
            shadow = species.starterShadowColor(),
            imageRes = species.chimeraImageRes()
        )
    }

    BoxWithConstraints(
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
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF173F70).copy(alpha = 0.92f), Color.Transparent),
                        center = Offset(widthPx * 1.05f, heightPx * 1.08f),
                        radius = widthPx * 0.48f
                    )
                )
        )

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
                        onClick = {
                            selectedStarter = starter
                            chimeraName = TextFieldValue(starter.species.defaultName())
                            isChimeraNameFocused = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            MenuButton(text = "Back", onClick = onBack)
        }

        selectedStarter?.let { starter ->
            ChimeraNameOverlay(
                starter = starter,
                name = chimeraName,
                isNameFocused = isChimeraNameFocused,
                onNameChanged = { value ->
                    if (value.text.length <= MaxChimeraNameLength) {
                        chimeraName = value
                    }
                },
                onFocusChanged = { focused -> isChimeraNameFocused = focused },
                onBack = {
                    selectedStarter = null
                    chimeraName = TextFieldValue("")
                    isChimeraNameFocused = false
                },
                onConfirm = {
                    val finalName = chimeraName.text.trim().ifBlank { starter.species.defaultName() }
                    onStarterSelected(starter.species, finalName)
                }
            )
        }
    }
}

/** Renders the chimera name overlay UI. */
@Composable
private fun ChimeraNameOverlay(
    starter: StarterOption,
    name: TextFieldValue,
    isNameFocused: Boolean,
    onNameChanged: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
        )

        Column(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface.copy(alpha = 0.86f))
                .border(1.dp, starter.accent.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Name Your Partner",
                color = colors.primary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(id = starter.imageRes),
                contentDescription = starter.species.defaultName(),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .height(92.dp)
                    .fillMaxWidth()
            )

            Box(
                modifier = Modifier
                    .width(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background.copy(alpha = 0.55f))
                    .border(1.dp, colors.primary.copy(alpha = 0.52f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    singleLine = true,
                    modifier = Modifier.onFocusChanged { onFocusChanged(it.isFocused) },
                    textStyle = TextStyle(
                        color = colors.primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CinzelFamily,
                        textAlign = TextAlign.Center
                    ),
                    decorationBox = { innerTextField ->
                        if (name.text.isBlank() && !isNameFocused) {
                            Text(
                                text = "Enter name",
                                color = colors.primary.copy(alpha = 0.45f),
                                fontSize = 16.sp,
                                fontFamily = CinzelFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(text = "Back", onClick = onBack)
                MenuButton(text = "Confirm", onClick = onConfirm)
            }
        }
    }
}

/** Renders the starter card UI. */
@Composable
private fun StarterCard(
    starter: StarterOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val chimera = remember(starter.species) {
        ChimeraFactory.createChimera(starter.species)
    }
    val stats = remember(chimera) {
        chimera.baseStats.asUiStats()
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
        StarterImage(starter = starter, name = chimera.name)

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = chimera.name,
            color = colors.primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            fontFamily = CinzelFamily
        )

        Text(
            text = "${chimera.type.displayName()} / ${chimera.type.battleTrait()}",
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

/** Renders the starter image UI. */
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

/** Handles as ui stats behavior. */
private fun Stats.asUiStats(): List<Pair<String, Int>> = listOf(
    "HP" to maxHp,
    "ATK" to attack,
    "DEF" to defence,
    "SPD" to speed
)

/** Handles display name behavior. */
private fun ChimeraType.displayName(): String {
    val lower = name.lowercase()
    return lower.replaceFirstChar { it.uppercase() }
}

/** Handles battle trait behavior. */
private fun ChimeraType.battleTrait(): String = when (this) {
    ChimeraType.FIRE -> "Fast striker"
    ChimeraType.GRASS -> "Steady fighter"
    ChimeraType.WATER -> "Tough defender"
    ChimeraType.NORMAL -> "Balanced fighter"
}

/** Handles default name behavior. */
private fun ChimeraSpecies.defaultName(): String = ChimeraFactory.speciesName(this)

private const val MaxChimeraNameLength = 12

/** Renders the stats grid UI. */
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

/** Renders the stat row UI. */
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

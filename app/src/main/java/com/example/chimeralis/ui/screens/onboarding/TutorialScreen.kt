package com.example.chimeralis.ui.screens.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.chimeras.ChimeraFactory
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.ui.screens.chimera.chimeraImageRes
import com.example.chimeralis.ui.screens.chimera.starterAccentColor
import com.example.chimeralis.ui.screens.chimera.starterShadowColor
import com.example.chimeralis.ui.theme.CinzelFamily

/**
 * Renders the new game tutorial screen UI.
 *
 * @param trainerName The trainer name value used by this operation.
 * @param starter The starter value used by this operation.
 * @param starterNickname The starter nickname value used by this operation.
 * @param onBegin Callback invoked when begin occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
fun TutorialScreen(
    trainerName: String,
    starter: ChimeraSpecies?,
    starterNickname: String,
    onBegin: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val accent = starter?.starterAccentColor() ?: colors.primary
    val shadow = starter?.starterShadowColor() ?: colors.surface
    val displayName = starterNickname.ifBlank {
        starter?.let(ChimeraFactory::speciesName) ?: "Partner"
    }
    val starterImageRes = starter?.chimeraImageRes()
    val slides = remember(displayName) { tutorialSlides(displayName) }
    var slideIndex by remember { mutableIntStateOf(0) }
    val currentSlide = slides[slideIndex]
    val isFirstSlide = slideIndex == 0
    val isLastSlide = slideIndex == slides.lastIndex

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
            .padding(horizontal = 30.dp, vertical = 18.dp)
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

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TutorialVisualPanel(
                    slide = currentSlide,
                    starterImageRes = starterImageRes,
                    starterName = displayName,
                    accent = accent,
                    shadow = shadow,
                    modifier = Modifier
                        .weight(0.95f)
                        .fillMaxHeight()
                )

                TutorialTextPanel(
                    slide = currentSlide,
                    trainerName = trainerName,
                    accent = accent,
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight()
                )
            }

            TutorialNavigation(
                slideIndex = slideIndex,
                slideCount = slides.size,
                canGoBack = !isFirstSlide,
                isLastSlide = isLastSlide,
                accent = accent,
                onPrevious = { slideIndex-- },
                onNext = {
                    if (isLastSlide) {
                        onBegin()
                    } else {
                        slideIndex++
                    }
                }
            )
        }
    }
}

/**
 * Renders the text half of the tutorial slide.
 *
 * @param slide The slide value used by this operation.
 * @param trainerName The trainer name value used by this operation.
 * @param accent The accent value used by this operation.
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialTextPanel(
    slide: TutorialSlide,
    trainerName: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = slide.title,
            color = colors.primary,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black,
            fontFamily = CinzelFamily
        )

        Text(
            text = slide.subtitle.withTrainerName(trainerName),
            color = colors.onSurface.copy(alpha = 0.84f),
            fontSize = 13.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CinzelFamily,
            modifier = Modifier.padding(top = 7.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        slide.tips.forEach { tip ->
            TutorialTipRow(text = tip, accent = accent)
        }
    }
}

/**
 * Renders one slide visual panel.
 *
 * @param slide The slide value used by this operation.
 * @param starterImageRes Resource id used by this operation: starter image res.
 * @param starterName The starter name value used by this operation.
 * @param accent The accent value used by this operation.
 * @param shadow The shadow value used by this operation.
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialVisualPanel(
    slide: TutorialSlide,
    @DrawableRes starterImageRes: Int?,
    starterName: String,
    accent: Color,
    shadow: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val panelModifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    shadow.copy(alpha = 0.88f),
                    colors.surface.copy(alpha = 0.72f)
                )
            )
        )
        .border(1.dp, accent.copy(alpha = 0.68f), RoundedCornerShape(8.dp))

    Box(
        modifier = if (slide.visual == TutorialVisual.Battle) {
            panelModifier
        } else {
            panelModifier.padding(14.dp)
        }
    ) {
        when (slide.visual) {
            TutorialVisual.Partner -> PartnerVisual(
                starterImageRes = starterImageRes,
                starterName = starterName,
                accent = accent
            )
            TutorialVisual.Movement -> MovementVisual(accent = accent)
            TutorialVisual.Interaction -> InteractionVisual(accent = accent)
            TutorialVisual.Battle -> BattleScreenshotImage()
            TutorialVisual.Care -> CareVisual(accent = accent)
        }
    }
}

/**
 * Renders the tutorial bottom navigation.
 *
 * @param slideIndex Numeric value used by this operation: slide index.
 * @param slideCount Numeric value used by this operation: slide count.
 * @param canGoBack Flag that controls or describes can go back.
 * @param isLastSlide Flag that controls or describes is last slide.
 * @param accent The accent value used by this operation.
 * @param onPrevious Callback invoked when previous occurs.
 * @param onNext Callback invoked when next occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialNavigation(
    slideIndex: Int,
    slideCount: Int,
    canGoBack: Boolean,
    isLastSlide: Boolean,
    accent: Color,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TutorialNavButton(
            direction = NavDirection.Left,
            enabled = canGoBack,
            accent = accent,
            onClick = onPrevious
        )

        Row(
            modifier = Modifier
                .width(156.dp)
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slideCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == slideIndex) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == slideIndex) accent
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                )
            }
        }

        TutorialNavButton(
            direction = NavDirection.Right,
            enabled = true,
            accent = accent,
            launch = isLastSlide,
            onClick = onNext
        )
    }
}

/**
 * Renders one tutorial navigation button.
 *
 * @param direction The direction value used by this operation.
 * @param enabled Flag that controls or describes enabled.
 * @param accent The accent value used by this operation.
 * @param launch Flag that controls or describes launch.
 * @param onClick Callback invoked when click occurs.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialNavButton(
    direction: NavDirection,
    enabled: Boolean,
    accent: Color,
    launch: Boolean = false,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val context = LocalContext.current
    val borderAlpha = if (enabled) 0.62f else 0.2f
    val contentColor = if (enabled) accent else colors.primary.copy(alpha = 0.24f)

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = if (enabled) 0.64f else 0.24f))
            .border(1.dp, colors.primary.copy(alpha = borderAlpha), RoundedCornerShape(8.dp))
            .pointerInput(enabled, launch) {
                detectTapGestures {
                    if (enabled) {
                        GameSoundPlayer.play(context, R.raw.button_click)
                        onClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(23.dp)) {
            if (launch) {
                val path = Path().apply {
                    moveTo(size.width * 0.28f, size.height * 0.18f)
                    lineTo(size.width * 0.28f, size.height * 0.82f)
                    lineTo(size.width * 0.82f, size.height * 0.5f)
                    close()
                }
                drawPath(path = path, color = contentColor)
            } else {
                val path = Path().apply {
                    val startX = if (direction == NavDirection.Right) size.width * 0.32f else size.width * 0.68f
                    val endX = if (direction == NavDirection.Right) size.width * 0.68f else size.width * 0.32f
                    moveTo(startX, size.height * 0.2f)
                    lineTo(endX, size.height * 0.5f)
                    lineTo(startX, size.height * 0.8f)
                }
                drawPath(
                    path = path,
                    color = contentColor,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

/**
 * Renders one tutorial tip row.
 *
 * @param text The text value used by this operation.
 * @param accent The accent value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialTipRow(
    text: String,
    accent: Color
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface.copy(alpha = 0.64f))
            .border(1.dp, colors.primary.copy(alpha = 0.26f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = text,
            color = colors.onSurface.copy(alpha = 0.86f),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CinzelFamily,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Renders the partner slide illustration.
 *
 * @param starterImageRes Resource id used by this operation: starter image res.
 * @param starterName The starter name value used by this operation.
 * @param accent The accent value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun PartnerVisual(
    @DrawableRes starterImageRes: Int?,
    starterName: String,
    accent: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(0.82f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.player_front_idle_1),
                    contentDescription = "Trainer",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .height(205.dp)
                        .fillMaxWidth()
                )
            }

            starterImageRes?.let { imageRes ->
                Box(
                    modifier = Modifier
                        .weight(1.08f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = starterName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(160.dp)
                            .height(140.dp)
                            .offset(y = (-18).dp)
                    )
                }
            }
        }

        Text(
            text = starterName,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            fontFamily = CinzelFamily,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Box(
            modifier = Modifier
                .padding(top = 8.dp, bottom = 6.dp)
                .height(5.dp)
                .fillMaxWidth(0.64f)
                .clip(RoundedCornerShape(99.dp))
                .background(accent)
        )
    }
}

/**
 * Renders the movement slide illustration.
 *
 * @param accent The accent value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun MovementVisual(accent: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.lava_ground),
            contentDescription = "Lava field",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        Image(
            painter = painterResource(id = R.drawable.rock_grass_tile),
            contentDescription = "Wild grass",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 18.dp)
                .size(92.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, accent.copy(alpha = 0.58f), RoundedCornerShape(8.dp))
        )

        Image(
            painter = painterResource(id = R.drawable.player_front_idle_1),
            contentDescription = "Player",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .size(120.dp)
        )

        TutorialJoystickPreview(
            accent = accent,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, bottom = 12.dp)
        )
    }
}

/**
 * Renders the interaction slide illustration.
 *
 * @param accent The accent value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun InteractionVisual(accent: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.grass_field_ground),
            contentDescription = "Town grass",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(150.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            TutorialPicture(
                imageRes = R.drawable.sign,
                contentDescription = "Town sign",
                accent = accent
            )
            TutorialPicture(
                imageRes = R.drawable.chimeracenter,
                contentDescription = "Chimera Center",
                accent = accent
            )
            TutorialPicture(
                imageRes = R.drawable.trainer_npc_idle_1,
                contentDescription = "Trainer",
                accent = accent
            )
        }

        ActionBadge(
            text = "Talk / Read / Enter",
            accent = accent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * Renders the battle slide screenshot PNG.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun BattleScreenshotImage() {
    Image(
        painter = painterResource(id = R.drawable.tutorial_battle_screenshot),
        contentDescription = "Battle screenshot",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
    )
}

/**
 * Renders the care slide illustration.
 *
 * @param accent The accent value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun CareVisual(accent: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.chimeracenter_interior),
            contentDescription = "Chimera Center interior",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TutorialItemBadge(
                imageRes = R.drawable.potion,
                contentDescription = "Potion",
                accent = accent,
                size = 74
            )
            TutorialItemBadge(
                imageRes = R.drawable.revive,
                contentDescription = "Revive",
                accent = accent,
                size = 74
            )
            TutorialItemBadge(
                imageRes = R.drawable.binding_stone_base,
                contentDescription = "Binding Stone",
                accent = accent,
                size = 74
            )
        }

        ActionBadge(
            text = "Menu: Save",
            accent = accent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

/**
 * Renders a framed tutorial picture.
 *
 * @param imageRes Resource id used by this operation: image res.
 * @param contentDescription The content description value used by this operation.
 * @param accent The accent value used by this operation.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialPicture(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    accent: Color
) {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(132.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.48f))
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(5.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Renders an item badge used in tutorial illustrations.
 *
 * @param imageRes Resource id used by this operation: image res.
 * @param contentDescription The content description value used by this operation.
 * @param accent The accent value used by this operation.
 * @param size Numeric value used by this operation: size.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialItemBadge(
    @DrawableRes imageRes: Int,
    contentDescription: String,
    accent: Color,
    size: Int = 54
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .border(1.dp, accent.copy(alpha = 0.58f), RoundedCornerShape(8.dp))
            .padding(7.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Renders a tutorial action badge.
 *
 * @param text The text value used by this operation.
 * @param accent The accent value used by this operation.
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun ActionBadge(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        fontFamily = CinzelFamily,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.78f))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Renders a static joystick preview.
 *
 * @param accent The accent value used by this operation.
 * @param modifier Compose modifier applied to the rendered component.
 * @return Unit; the operation updates state, performs side effects, or renders UI.
 */
@Composable
private fun TutorialJoystickPreview(
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(92.dp)
            .background(Color.Black.copy(alpha = 0.32f), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.28f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x = 18.dp, y = (-12).dp)
                .size(36.dp)
                .background(accent.copy(alpha = 0.95f), CircleShape)
                .border(2.dp, Color(0xFF3D1500), CircleShape)
        )
    }
}

private fun String.withTrainerName(trainerName: String): String {
    return if (contains(TrainerNameToken) && trainerName.isNotBlank()) {
        replace(TrainerNameToken, trainerName)
    } else {
        replace("$TrainerNameToken, ", "")
    }
}

private fun tutorialSlides(starterName: String): List<TutorialSlide> = listOf(
    TutorialSlide(
        title = "Meet Your Partner",
        subtitle = "$TrainerNameToken, this is the chimera that will lead your first battles.",
        tips = listOf(
            "$starterName starts in the first team slot and enters battle first.",
            "Keep an eye on HP. A defeated team cannot start new battles.",
            "Your partner can grow stronger, learn moves, and eventually evolve."
        ),
        visual = TutorialVisual.Partner
    ),
    TutorialSlide(
        title = "Explore the Field",
        subtitle = "Hold the joystick to walk through Chimeralis one tile at a time.",
        tips = listOf(
            "Paths are safe, but wild terrain can trigger random encounters.",
            "Release the joystick to stop exactly where you are.",
            "The team slots stay visible so you can track your active chimera."
        ),
        visual = TutorialVisual.Movement
    ),
    TutorialSlide(
        title = "Use Action Spots",
        subtitle = "The action button appears when something nearby can be used.",
        tips = listOf(
            "Stand beside signs to read them and beside doors to enter buildings.",
            "Talk to NPCs for travel, healing, shopping, or trainer challenges.",
            "The button label changes to Read, Enter, or Talk based on context."
        ),
        visual = TutorialVisual.Interaction
    ),
    TutorialSlide(
        title = "Win Battles",
        subtitle = "Battles are turn based, so choose each action with care.",
        tips = listOf(
            "Moves spend PP and can damage, heal, or change stats.",
            "Use items when your chimera is hurt or when you want to capture wild ones.",
            "Wild battles can be escaped, but trainer battles must be won."
        ),
        visual = TutorialVisual.Battle
    ),
    TutorialSlide(
        title = "Care and Save",
        subtitle = "Before heading deeper into the field, keep your bag and save ready.",
        tips = listOf(
            "Potions heal, Revives bring a fainted chimera back, and Binding Stones capture wild chimeras.",
            "Open the Menu to save before leaving the game.",
            "Your first goal is to explore the Lava Field and find the traveler."
        ),
        visual = TutorialVisual.Care
    )
)

private data class TutorialSlide(
    val title: String,
    val subtitle: String,
    val tips: List<String>,
    val visual: TutorialVisual
)

private enum class TutorialVisual {
    Partner,
    Movement,
    Interaction,
    Battle,
    Care
}

private enum class NavDirection {
    Left,
    Right
}

private const val TrainerNameToken = "{trainer}"

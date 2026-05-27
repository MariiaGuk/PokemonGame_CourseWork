package com.example.chimeralis.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.theme.CinzelFamily

/** Renders the trainer name screen UI. */
@Composable
fun TrainerNameScreen(
    onNameConfirmed: (String) -> Unit,
    onBack: () -> Unit,
    errorMessage: String? = null,
    onNameEdited: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var trainerName by remember { mutableStateOf(TextFieldValue("")) }
    var isNameFocused by remember { mutableStateOf(false) }
    val cleanName = trainerName.text.trim()
    val canContinue = cleanName.isNotEmpty()

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
            modifier = Modifier
                .align(Alignment.Center)
                .width(360.dp)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Trainer Name",
                color = colors.primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Choose the name that will follow you through Chimeralis.",
                color = colors.onSurface.copy(alpha = 0.82f),
                fontSize = 12.sp,
                fontFamily = CinzelFamily,
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .width(260.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surface.copy(alpha = 0.68f))
                    .border(1.dp, colors.primary.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = trainerName,
                    onValueChange = { value ->
                        if (value.text.length <= MaxTrainerNameLength) {
                            trainerName = value
                            onNameEdited()
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.onFocusChanged { focusState ->
                        isNameFocused = focusState.isFocused
                    },
                    textStyle = TextStyle(
                        color = colors.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CinzelFamily,
                        textAlign = TextAlign.Center
                    ),
                    decorationBox = { innerTextField ->
                        if (trainerName.text.isBlank() && !isNameFocused) {
                            Text(
                                text = "Enter name",
                                color = colors.primary.copy(alpha = 0.45f),
                                fontSize = 18.sp,
                                fontFamily = CinzelFamily,
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6A2A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CinzelFamily,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MenuButton(text = "Back", onClick = onBack)
                MenuButton(
                    text = "Start",
                    onClick = {
                        if (canContinue) {
                            onNameConfirmed(cleanName)
                        }
                    }
                )
            }
        }
    }
}

private const val MaxTrainerNameLength = 12

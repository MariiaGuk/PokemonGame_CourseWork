package com.example.pokemon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.pokemon.R

val CinzelFamily = FontFamily(
    Font(R.font.cinzel_black, FontWeight.Black)
)

val ChimeralisTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        letterSpacing = 4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Black,
        fontSize = 14.sp,
        letterSpacing = 2.sp
    ),
    bodySmall = TextStyle(
        fontFamily = CinzelFamily,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        letterSpacing = 2.sp
    )
)
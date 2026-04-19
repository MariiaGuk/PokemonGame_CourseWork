package com.example.pokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.remote.creation.random
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf("splash") }

    when (currentScreen) {
        "splash" -> SplashScreen(onFinished = { currentScreen = "main_menu" })
        "main_menu" -> MainMenuScreen(
            onNewGame = { currentScreen = "starter_selection" },
            onContinue = { /* пізніше */ }
        )
        "starter_selection" -> StarterSelectionScreen(
            onStarterSelected = { currentScreen = "battle" }
        )
        "battle" -> BattleScreen()
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 150),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        while (progress < 1f) {
            val pause = if ((1..5).random() == 1) (300L..600L).random() else (20L..60L).random()
            delay(pause)
            progress += (8..35).random() / 1000f
            progress = progress.coerceAtMost(1f)
        }
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .padding(horizontal = 32.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.15f))
                    .border(
                        width = 0.5.dp,
                        color = Color(0xFFFF6B00).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFFF4500),
                                    Color(0xFFFF6B00),
                                    Color(0xFFFFD700)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun MainMenuScreen(onNewGame: () -> Unit, onContinue: () -> Unit) {
    // TODO
}

@Composable
fun StarterSelectionScreen(onStarterSelected: () -> Unit) {
    // TODO
}

@Composable
fun BattleScreen() {
    // TODO
}
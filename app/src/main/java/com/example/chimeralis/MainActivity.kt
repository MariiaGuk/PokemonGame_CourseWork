package com.example.chimeralis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.chimeralis.ui.navigation.AppNavigation
import com.example.chimeralis.ui.screens.chimera.validateChimeraVisualResourceMappings
import com.example.chimeralis.ui.theme.ChimeralisTheme

/** Represents the main activity. */
class MainActivity : ComponentActivity() {
    /**
     * Executes the on create operation.
     *
     * @param savedInstanceState The saved instance state value used by this operation.
     * @return Unit; the operation updates state, performs side effects, or renders UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validateChimeraVisualResourceMappings()
        enableEdgeToEdge()

        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            ChimeralisTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(onExitGame = ::finish)
                }
            }
        }
    }
}


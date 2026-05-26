package com.example.chimeralis.ui.screens.world

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chimeralis.R
import com.example.chimeralis.audio.GameSoundPlayer
import com.example.chimeralis.logic.chimeras.Chimera
import com.example.chimeralis.logic.chimeras.ChimeraSpecies
import com.example.chimeralis.logic.items.Item
import com.example.chimeralis.logic.items.ItemFactory
import com.example.chimeralis.logic.items.ItemName
import com.example.chimeralis.ui.components.GameSettingsPanel
import com.example.chimeralis.ui.components.MenuButton
import com.example.chimeralis.ui.screens.world.locations.TownInterior
import com.example.chimeralis.ui.screens.world.locations.TownSign
import com.example.chimeralis.ui.theme.CinzelFamily
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.random.Random
import kotlin.math.roundToInt

internal fun serviceNpcIdleFrame(interior: TownInterior, frameIndex: Int): Int {
    val frames = when (interior) {
        TownInterior.ChimeraCenter -> nurseIdleFrames
        TownInterior.ChimeraStore -> sellerIdleFrames
    }

    return frames[frameIndex % frames.size]
}

internal fun serviceNpcDialogFrame(interior: TownInterior, step: Int, frameIndex: Int): Int {
    val frames = when (interior) {
        TownInterior.ChimeraCenter -> if (step == 0) {
            nurseGreetingDialogFrames
        } else {
            nurseServiceDialogFrames
        }
        TownInterior.ChimeraStore -> if (step == 0) {
            sellerGreetingDialogFrames
        } else {
            sellerServiceDialogFrames
        }
    }

    return frames[frameIndex % frames.size]
}

internal fun playerFrame(direction: Direction, isMoving: Boolean, frameIndex: Int): Int {
    val frames = when {
        isMoving && direction == Direction.Down -> frontRunFrames
        isMoving && direction == Direction.Up -> backRunFrames
        isMoving && (direction == Direction.Left || direction == Direction.Right) -> sideRunFrames
        !isMoving && direction == Direction.Down -> frontIdleFrames
        !isMoving && direction == Direction.Up -> backIdleFrames
        else -> sideIdleFrames
    }

    return frames[frameIndex % frames.size]
}

internal fun shiftNpcIdleFrame(frameIndex: Int): Int {
    return shiftNpcIdleFrames[frameIndex % shiftNpcIdleFrames.size]
}

internal fun shiftNpcDialogFrame(step: Int, frameIndex: Int): Int {
    val frames = when (step) {
        0 -> shiftNpcSeriousDialogFrames
        1 -> shiftNpcSurprisedDialogFrames
        else -> shiftNpcCalmDialogFrames
    }

    return frames[frameIndex % frames.size]
}

internal fun shiftNpcDialogText(step: Int): String {
    return when (step) {
        0 -> "Hey, who are you and what do you want?"
        1 -> "Wait, you are a trainer too?"
        2 -> "Sorry, I did not expect to see any new faces here. You are just starting your journey, right?"
        else -> "In that case, let me show you our trainer town. You can heal your chimeras there and stock up on new gear."
    }
}

internal val frontIdleFrames = listOf(
    R.drawable.player_front_idle_1,
    R.drawable.player_front_idle_2,
    R.drawable.player_front_idle_3,
    R.drawable.player_front_idle_4
)
internal val backIdleFrames = listOf(
    R.drawable.player_back_idle_1,
    R.drawable.player_back_idle_2,
    R.drawable.player_back_idle_3,
    R.drawable.player_back_idle_4
)
internal val sideIdleFrames = listOf(
    R.drawable.player_side_idle_1,
    R.drawable.player_side_idle_2,
    R.drawable.player_side_idle_3
)
internal val frontRunFrames = listOf(
    R.drawable.player_front_run_1,
    R.drawable.player_front_run_2,
    R.drawable.player_front_run_3,
    R.drawable.player_front_run_4,
    R.drawable.player_front_run_5,
    R.drawable.player_front_run_6,
    R.drawable.player_front_run_7,
    R.drawable.player_front_run_8
)
internal val backRunFrames = listOf(
    R.drawable.player_back_run_1,
    R.drawable.player_back_run_2,
    R.drawable.player_back_run_3,
    R.drawable.player_back_run_4,
    R.drawable.player_back_run_5,
    R.drawable.player_back_run_6,
    R.drawable.player_back_run_7,
    R.drawable.player_back_run_8
)
internal val sideRunFrames = listOf(
    R.drawable.player_side_run_1,
    R.drawable.player_side_run_2,
    R.drawable.player_side_run_3,
    R.drawable.player_side_run_4,
    R.drawable.player_side_run_5,
    R.drawable.player_side_run_6,
    R.drawable.player_side_run_7,
    R.drawable.player_side_run_8
)

internal val shiftNpcIdleFrames = listOf(
    R.drawable.shift_npc_1,
    R.drawable.shift_npc_2,
    R.drawable.shift_npc_3
)

internal val nurseIdleFrames = listOf(
    R.drawable.nurse_idle_1,
    R.drawable.nurse_idle_2,
    R.drawable.nurse_idle_3
)

internal val sellerIdleFrames = listOf(
    R.drawable.seller_idle_1,
    R.drawable.seller_idle_2
)

internal val nurseGreetingDialogFrames = listOf(
    R.drawable.nurse_dialog_1,
    R.drawable.nurse_dialog_2
)

internal val nurseServiceDialogFrames = listOf(
    R.drawable.nurse_dialog_2,
    R.drawable.nurse_dialog_1
)

internal val sellerGreetingDialogFrames = listOf(
    R.drawable.seller_dialog_1,
    R.drawable.seller_dialog_2
)

internal val sellerServiceDialogFrames = listOf(
    R.drawable.seller_dialog_2,
    R.drawable.seller_dialog_1
)

internal val shiftNpcSeriousDialogFrames = listOf(
    R.drawable.dialog_shift_npc_serious_1,
    R.drawable.dialog_shift_npc_serious_2
)

internal val shiftNpcSurprisedDialogFrames = listOf(
    R.drawable.dialog_shift_npc_surprised_1,
    R.drawable.dialog_shift_npc_surprised_2
)

internal val shiftNpcCalmDialogFrames = listOf(
    R.drawable.dialog_shift_npc_calm_1,
    R.drawable.dialog_shift_npc_calm_2
)

internal fun randomWildChimera(starter: ChimeraSpecies?): ChimeraSpecies {
    val pool = listOf(
        ChimeraSpecies.Sunflare,
        ChimeraSpecies.Sylvhorn,
        ChimeraSpecies.Aquantis
    )

    return pool.random()
}

internal fun ChimeraSpecies.teamImageRes(): Int = when (this) {
    ChimeraSpecies.Sunflare,
    ChimeraSpecies.Solflare,
    ChimeraSpecies.Solignis -> R.drawable.starter_fire
    ChimeraSpecies.Sylvhorn -> R.drawable.starter_grass
    ChimeraSpecies.Aquantis -> R.drawable.starter_water
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBushTile(
    left: Float,
    top: Float,
    tileSize: Float
) {
    val dark = Color(0xFF1F5E28)
    val light = Color(0xFF55A84B)

    drawCircle(
        color = dark,
        radius = tileSize * 0.28f,
        center = Offset(left + tileSize * 0.28f, top + tileSize * 0.55f)
    )
    drawCircle(
        color = light,
        radius = tileSize * 0.25f,
        center = Offset(left + tileSize * 0.5f, top + tileSize * 0.42f)
    )
    drawCircle(
        color = dark,
        radius = tileSize * 0.28f,
        center = Offset(left + tileSize * 0.72f, top + tileSize * 0.55f)
    )
}

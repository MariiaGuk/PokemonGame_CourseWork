package com.example.chimeralis.ui.screens.world.interior.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.chimeralis.ui.screens.world.Direction
import com.example.chimeralis.ui.screens.world.locations.TownInterior

/** Controls player movement and sprite animation state for a town interior. */
internal class TownInteriorMovementState(
    initialPlayerColumn: Int,
    initialPlayerRow: Int,
    initialPlayerDirection: Direction
) {
    var playerColumn by mutableIntStateOf(initialPlayerColumn)
        private set
    var playerRow by mutableIntStateOf(initialPlayerRow)
        private set
    var targetColumn by mutableIntStateOf(initialPlayerColumn)
        private set
    var targetRow by mutableIntStateOf(initialPlayerRow)
        private set
    var direction by mutableStateOf(initialPlayerDirection)
        private set
    var requestedDirection by mutableStateOf<Direction?>(null)
        private set
    var isMoving by mutableStateOf(false)
        private set
    var animationFrame by mutableIntStateOf(0)
        private set
    var serviceNpcIdleFrame by mutableIntStateOf(0)
        private set

    /** Updates the direction requested by the virtual joystick. */
    fun requestDirection(direction: Direction?) {
        requestedDirection = direction
    }

    /** Stops active movement and clears held movement input. */
    fun stopMovement() {
        requestedDirection = null
        isMoving = false
    }

    /** Changes the direction the player sprite is facing. */
    fun face(direction: Direction) {
        this.direction = direction
    }

    /** Starts moving the player sprite toward the target interior tile. */
    fun beginStepTo(column: Int, row: Int) {
        targetColumn = column
        targetRow = row
        isMoving = true
    }

    /** Commits the player position after a step animation finishes. */
    fun finishStepAt(column: Int, row: Int) {
        playerColumn = column
        playerRow = row
        isMoving = false
    }

    /** Advances the player sprite animation frame. */
    fun advanceAnimationFrame() {
        animationFrame++
    }

    /** Advances the service NPC idle animation frame. */
    fun advanceServiceNpcIdleFrame() {
        serviceNpcIdleFrame++
    }
}

/** Remembers movement state for one town interior. */
@Composable
internal fun rememberTownInteriorMovementState(
    interior: TownInterior,
    initialPlayerColumn: Int,
    initialPlayerRow: Int,
    initialPlayerDirection: Direction
): TownInteriorMovementState {
    return remember(interior) {
        TownInteriorMovementState(
            initialPlayerColumn = initialPlayerColumn,
            initialPlayerRow = initialPlayerRow,
            initialPlayerDirection = initialPlayerDirection
        )
    }
}

package com.example.chimeralis.ui.screens.world

internal const val MapColumns = 21
internal const val MapRows = 10
internal const val StepDurationMs = 280
internal const val InteriorStepDurationMs = 150
internal const val HeldStepDelayMs = 65L
internal const val JoystickDeadZone = 0.35f
internal const val MovingFrameDelayMs = 160L
internal const val IdleFrameDelayMs = 520L
internal const val WorldReturnInputLockMs = 1400L
internal const val WorldZoom = 1.28f
internal const val MaxTeamSize = 6
internal const val WorldInventoryColumns = 3
internal const val WorldInventorySlotCount = 9
internal const val LavaShiftNpcColumn = 19
internal const val LavaShiftNpcRow = 5
internal const val GrassShiftNpcColumn = 1
internal const val GrassShiftNpcRow = 5
internal const val GrassTrainerNpcColumn = 13
internal const val GrassTrainerNpcRow = 5
internal const val ShiftNpcIdleFrameDelayMs = 960L
internal const val ServiceNpcIdleFrameDelayMs = 980L
internal const val InteriorColumns = 16
internal const val InteriorRows = 16

enum class WorldField { Lava, Grass }

enum class Direction { Down, Up, Left, Right }

internal enum class ExitAction { MainMenu, ExitGame }

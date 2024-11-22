package dev.vizualjack.matrix_shortcut

import dev.vizualjack.matrix_shortcut.ui.Settings
import kotlinx.serialization.Serializable

@Serializable
data class AppData(var settings: Settings? = null, var gestures:List<Gesture>? = null)
package dev.psiae.mltoolbox.composeui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.WindowState

@Composable
fun rememberMainGUIState(

): MainGUIState {
    val state = remember { MainGUIState() }

    // cleanup when removed from composition
    DisposableEffect(
        state,
        effect = {
            state.onRemembered()
            onDispose { state.onForgotten() }
        }
    )

    return state
}

@Stable
class MainGUIState {

    fun onRemembered() {}
    fun onForgotten() {}

    fun mainWindowEnter(state: WindowState) {}
    fun mainWindowExit(state: WindowState) {}
}
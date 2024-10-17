package dev.psiae.mltoolbox.composeui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun ApplicationScope.MainWindow(state: MainGUIState) {
    val windowState = rememberWindowState(
        /// initial values
        placement = WindowPlacement.Floating,
        width = 1280.dp, height = 900.dp,
        position = WindowPosition.PlatformDefault,
    )

    DisposableEffect(this) {
        state.mainWindowEnter(windowState)
        onDispose { state.mainWindowExit(windowState) }
    }
}
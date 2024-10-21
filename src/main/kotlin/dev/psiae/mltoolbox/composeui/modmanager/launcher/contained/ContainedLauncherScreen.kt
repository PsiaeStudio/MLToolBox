package dev.psiae.mltoolbox.composeui.modmanager.launcher.contained

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.composeui.modmanager.WIPScreen
import dev.psiae.mltoolbox.composeui.modmanager.launcher.LauncherScreenState

@Composable
fun ContainedLauncherScreen(
    launcherScreenState: LauncherScreenState
) {
    val state = rememberContainedLauncherScreenState(launcherScreenState)
    WIPScreen()
}
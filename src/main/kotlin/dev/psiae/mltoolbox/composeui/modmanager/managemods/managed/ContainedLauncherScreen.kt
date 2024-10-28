package dev.psiae.mltoolbox.composeui.modmanager.managemods.managed

import androidx.compose.runtime.Composable
import dev.psiae.mltoolbox.composeui.modmanager.WIPScreen
import dev.psiae.mltoolbox.composeui.modmanager.managemods.ManageModsScreenState

@Composable
fun ContainedLauncherScreen(
    manageModsScreenState: ManageModsScreenState
) {
    val state = rememberContainedLauncherScreenState(manageModsScreenState)
    WIPScreen()
}
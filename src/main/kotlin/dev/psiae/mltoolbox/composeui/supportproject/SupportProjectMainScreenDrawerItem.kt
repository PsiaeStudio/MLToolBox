package dev.psiae.mltoolbox.composeui.supportproject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import dev.psiae.mltoolbox.composeui.MainDrawerDestination
import dev.psiae.mltoolbox.composeui.NoOpPainter
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme

@Composable
fun supportProjectMainScreenDrawerItem(): MainDrawerDestination {
    val content = @Composable { DonateMainScreen() }
    val painter = NoOpPainter
    return remember(painter) {
        MainDrawerDestination(
            id = "support_project",
            icon = painter,
            iconTint = null,
            name = "Support Project",
            content = content
        )
    }
}
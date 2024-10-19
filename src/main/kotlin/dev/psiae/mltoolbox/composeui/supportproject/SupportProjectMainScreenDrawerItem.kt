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
    val iconTint = Material3Theme.colorScheme.onSurface
    return remember(painter, iconTint) {
        MainDrawerDestination(
            id = "support_project",
            icon = painter,
            iconTint = iconTint,
            name = "Support Project",
            content = content
        )
    }
}
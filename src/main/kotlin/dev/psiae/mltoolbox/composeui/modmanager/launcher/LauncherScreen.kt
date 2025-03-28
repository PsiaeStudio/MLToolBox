package dev.psiae.mltoolbox.composeui.modmanager.launcher

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.NoOpPainter
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.md3.requireCurrent
import dev.psiae.mltoolbox.composeui.md3.rippleAlphaOrDefault
import dev.psiae.mltoolbox.composeui.modmanager.ModManagerScreenState
import dev.psiae.mltoolbox.composeui.modmanager.SimpleTooltip
import dev.psiae.mltoolbox.composeui.modmanager.launcher.contained.ContainedLauncherScreen
import dev.psiae.mltoolbox.composeui.modmanager.launcher.direct.DirectLauncherScreen
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.composeui.theme.md3.currentLocalAbsoluteOnSurfaceColor
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Theme

@Composable
fun LauncherScreen(
    screenState: ModManagerScreenState
) {
    LauncherScreen(rememberLauncherScreenState(screenState))
}

@Composable
private fun LauncherScreen(
    state: LauncherScreenState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surfaceDim)
            .defaultSurfaceGestureModifiers()
    ) {
        Column {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                TopNavigation(state)
            }
            Box {
                Box(
                    modifier = Modifier.zIndex(if (state.selectedTab == "direct") 1f else 0f)
                ) {
                    DirectLauncherScreen(state)
                }
                Box(
                    modifier = Modifier.zIndex(if (state.selectedTab == "contained") 1f else 0f)
                ) {
                    ManagedLauncherScreen(state)
                }
            }
        }
    }
}

@Composable
private fun TopNavigation(
    state: LauncherScreenState
) {
    val scrollState = rememberScrollState()
    val contentIns = remember { MutableInteractionSource() }
    val scrollBarIns = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(1200.dp)
                .padding(horizontal = 8.dp)
                .hoverable(contentIns)
                .horizontalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                WidthSpacer(8.dp)
                Row(
                    modifier = Modifier
                        .shadow(3.dp, RoundedCornerShape(24.dp))
                        .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(24.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopNavigationItemUI(
                        displayName = "Direct",
                        isSelected = state.selectedTab == "direct",
                        width = 260.dp,
                        enabled = true,
                        icon = NoOpPainter,
                        tintIcon = true,
                        tooltipDescription = "Launch executable directly in the game install directory",
                        onClick = { state.selectedTab = "direct"  }
                    )
                    TopNavigationItemUI(
                        displayName = "Managed (WIP)",
                        isSelected = state.selectedTab == "managed",
                        width = 260.dp,
                        enabled = false,
                        icon = NoOpPainter,
                        tintIcon = true,
                        tooltipDescription = "Launch executable managed in this app",
                        onClick = { state.selectedTab = "managed"  }
                    )
                }
                WidthSpacer(8.dp)
            }
        }
        if (
            run {
                // remove true to only start show when interacted
                true ||
                        contentIns.collectIsHoveredAsState().value or scrollBarIns.collectIsDraggedAsState().value
            } && scrollState.canScrollForward or scrollState.canScrollBackward
        ) {
            HeightSpacer(2.dp)
            HorizontalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(
                        with(LocalDensity.current) {
                            remember(this) {
                                derivedStateOf { scrollState.viewportSize.toDp() }
                            }.value
                        }
                    )
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                    .then(
                        if (scrollState.canScrollForward or scrollState.canScrollBackward)
                            Modifier.background(Color.White.copy(alpha = 0.06f))
                        else Modifier
                    ),
                adapter = rememberScrollbarAdapter(scrollState),
                style = run {
                    val absOnSurface = MD3Theme.currentLocalAbsoluteOnSurfaceColor()
                    defaultScrollbarStyle().copy(
                        unhoverColor = absOnSurface.copy(alpha = 0.25f),
                        hoverColor = absOnSurface.copy(alpha = 0.50f),
                        thickness = 4.dp
                    )
                },
                interactionSource = scrollBarIns
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopNavigationItemUI(
    displayName: String,
    width: Dp,
    isSelected: Boolean,
    enabled: Boolean,
    icon: Painter = NoOpPainter,
    tintIcon: Boolean = true,
    tooltipDescription: String?,
    onClick: () -> Unit,
) {
    val ins = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .width(width)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = enabled, indication = null, interactionSource = ins, onClick = onClick)
            .composed {
                val rippleTheme = LocalRippleConfiguration.requireCurrent()
                if (ins.collectIsHoveredAsState().value) {
                    Modifier
                        .background(color = rippleTheme.color.copy(alpha = rippleTheme.rippleAlphaOrDefault().hoveredAlpha))
                } else {
                    Modifier
                }
            }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(48.dp)
                    .width((1 * width.value).dp)
                    .background(Material3Theme.colorScheme.secondaryContainer)
            )
        }
        Row(
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != NoOpPainter) {
                Box {
                    /*if (isSelected && !tintIcon) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Material3Theme.colorScheme.onSecondaryContainer)
                        ) {

                        }
                    }*/
                    Icon(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                            .alpha(if (enabled) 1f else 0.38f),
                        painter = icon,
                        contentDescription = null,
                        tint = if (!tintIcon)
                            Color.Unspecified
                        else if (isSelected)
                            Material3Theme.colorScheme.onSecondaryContainer
                        else Material3Theme.colorScheme.onSurfaceVariant
                    )
                }
                WidthSpacer(8.dp)
            }
            Text(
                modifier = Modifier.alpha(if (enabled) 1f else 0.38f),
                text = displayName,
                style = Material3Theme.typography.labelLarge.copy(
                    baselineShift = BaselineShift(-0.1f),
                ),
                maxLines = 1,
                color = Material3Theme.colorScheme.onSecondaryContainer,
            )
            tooltipDescription?.let {
                WidthSpacer(12.dp)
                SimpleTooltip(
                    tooltipDescription
                ) {
                    Icon(
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.CenterVertically),
                        painter = painterResource("drawable/icon_info_filled_32px.png"),
                        contentDescription = null,
                        tint = Material3Theme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun DirectLauncherScreen(
    launcherScreenState: LauncherScreenState
) {
    DirectLauncherScreen(launcherScreenState)
}

@Composable
fun ManagedLauncherScreen(
    launcherScreenState: LauncherScreenState
) {
    ContainedLauncherScreen(launcherScreenState)
}
package dev.psiae.mltoolbox.composeui.modmanager.launcher.shared

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.NoOpPainter
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.modmanager.SimpleTooltip
import dev.psiae.mltoolbox.composeui.modmanager.WIPScreen
import dev.psiae.mltoolbox.composeui.modmanager.launcher.LauncherScreenState
import dev.psiae.mltoolbox.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Spec
import dev.psiae.mltoolbox.uifoundation.themes.md3.incrementsDp
import dev.psiae.mltoolbox.uifoundation.themes.md3.padding

@Composable
fun DirectLauncherScreen(
    launcherScreenState: LauncherScreenState
) {
    val state = rememberDirectLauncherScreenState(launcherScreenState)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Material3Theme.colorScheme.surfaceDim)
            .defaultSurfaceGestureModifiers()
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
                    .border(
                        width = 1.dp,
                        color = Material3Theme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .defaultSurfaceGestureModifiers(),
            ) {
                var width by remember {
                    mutableStateOf(0.dp)
                }
                val density = LocalDensity.current
                Column(
                    // TODO: measure content ourselves then render the divider based on it
                    modifier = Modifier
                        .onGloballyPositioned { coord ->
                            with(density) { coord.size.width.toDp() }.let {
                                width = it
                            }
                        }
                ) {
                    HeightSpacer(8.dp)
                    BoxWithConstraints(modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .defaultMinSize(minWidth = 1200.dp)
                        .defaultMinSize(minHeight = 36.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .then(
                            if (LocalIsDarkTheme.current)
                                Modifier.shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
                            else
                                Modifier.border(width = 1.dp, Material3Theme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                        )
                        .clickable { launcherScreenState.modManagerScreenState.userInputChangeWorkingDir() }
                        .background(Material3Theme.colorScheme.inverseOnSurface)
                        .padding(MD3Spec.padding.incrementsDp(2).dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(horizontal = 2.dp)
                                .defaultMinSize(minWidth = with(LocalDensity.current) { constraints.minWidth.toDp() }),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            run {
                                val f = launcherScreenState.modManagerScreenState.gameBinaryFile
                                val fp = launcherScreenState.modManagerScreenState.gameBinaryFile?.absolutePath
                                val (fp1, fp2) = remember(f) {
                                    run {
                                        var dash = false
                                        fp?.dropLastWhile { c -> !dash.also { dash = c == '\\' } }
                                    } to run {
                                        var dash = false
                                        fp?.takeLastWhile { c -> !dash.also { dash = c == '\\' } }
                                    }
                                }
                                val color = Material3Theme.colorScheme.onSurface.copy(alpha = 0.78f)
                                Text(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .align(Alignment.CenterVertically),
                                    text = fp1?.plus(fp2) ?: "Click here to select game executable",
                                    style = Material3Theme.typography.labelMedium,
                                    color = color,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                            WidthSpacer(MD3Spec.padding.incrementsDp(2).dp)
                            Icon(
                                modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
                                painter = painterResource("drawable/icon_folder_96px.png"),
                                contentDescription = null,
                                tint = Material3Theme.colorScheme.secondary
                            )
                        }
                    }
                    HeightSpacer(4.dp)
                    Box(
                        modifier = Modifier.padding(horizontal = 12.dp)
                    ) {
                        ModeNavigation(state)
                    }
                    HeightSpacer(16.dp)
                    HorizontalDivider(
                        modifier = Modifier.width(width),
                        thickness = 1.dp,
                        color = Material3Theme.colorScheme.outlineVariant
                    )
                    HeightSpacer(8.dp)
                    Box {
                        Box(
                            modifier = Modifier.zIndex(if (state.selectedTab == "direct") 1f else 0f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Material3Theme.colorScheme.surfaceContainer)
                                    .defaultSurfaceGestureModifiers()
                            ) {

                            }
                        }
                        Box(
                            modifier = Modifier.zIndex(if (state.selectedTab == "managed") 1f else 0f)
                        ) {
                            WIPScreen(background = Material3Theme.colorScheme.surfaceContainer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeNavigation(
    screenState: SharedLauncherScreenState
) {
    val scrollState = rememberScrollState()
    val contentIns = remember { MutableInteractionSource() }
    val scrollBarIns = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(contentIns)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.Start)
                .horizontalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier
                    .background(Material3Theme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
                    .border(1.dp, Material3Theme.colorScheme.outline, RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier/*.weight(1f, false)*/) {
                    ModeNavigationItemUI(
                        displayName = "Direct Mods",
                        isSelected = screenState.selectedTab == "direct",
                        width = 220.dp,
                        enabled = true,
                        icon = NoOpPainter,
                        tintIcon = true,
                        tooltipDescription = "Manage Mods directly in the game install folder",
                        onClick = { screenState.selectedTab = "direct"  }
                    )
                }
                Box(modifier = Modifier/*.weight(1f, false)*/) {
                    ModeNavigationItemUI(
                        displayName = "Contained Mods (WIP)",
                        isSelected = screenState.selectedTab == "contained",
                        width = 220.dp,
                        enabled = false,
                        icon = NoOpPainter,
                        tintIcon = true,
                        tooltipDescription = "Manage Mods contained in this app",
                        onClick = { screenState.selectedTab = "contained"  }
                    )
                }
            }
        }
        if (
            run {
                // remove true to only start show when interacted
                true ||
                        contentIns.collectIsHoveredAsState().value or scrollBarIns.collectIsDraggedAsState().value or
                        scrollBarIns.collectIsFocusedAsState().value
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
                        if (scrollState.canScrollForward || scrollState.canScrollBackward)
                            Modifier.background(Color.White.copy(alpha = 0.06f))
                        else Modifier
                    ),
                adapter = rememberScrollbarAdapter(scrollState),
                style = remember {
                    defaultScrollbarStyle().copy(
                        unhoverColor = Color.White.copy(alpha = 0.25f),
                        hoverColor = Color.White.copy(alpha = 0.50f),
                        thickness = 4.dp
                    )
                },
                interactionSource = scrollBarIns
            )
        }
    }
}

@Composable
private fun ModeNavigationItemUI(
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
            .height(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, indication = null, interactionSource = ins, onClick = onClick)
            .composed {
                val rippleTheme = LocalRippleTheme.current
                if (ins.collectIsHoveredAsState().value) {
                    Modifier
                        .background(color = rippleTheme.defaultColor().copy(alpha = rippleTheme.rippleAlpha().hoveredAlpha))
                } else {
                    Modifier
                }
            }
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(36.dp)
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
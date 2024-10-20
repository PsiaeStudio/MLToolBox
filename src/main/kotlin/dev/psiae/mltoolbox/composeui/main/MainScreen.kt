package dev.psiae.mltoolbox.composeui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import dev.psiae.mltoolbox.composeui.*
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.modmanager.modManagerMainScreenDrawerItem
import dev.psiae.mltoolbox.composeui.supportproject.supportProjectMainScreenDrawerItem
import dev.psiae.mltoolbox.composeui.text.nonScaledFontSize
import dev.psiae.mltoolbox.composeui.theme.md3.LocalIsDarkTheme
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.composeui.theme.md3.surfaceColorAtElevation
import dev.psiae.mltoolbox.platform.win32.CustomDecorationParameters
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Spec
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Theme
import dev.psiae.mltoolbox.uifoundation.themes.md3.margin
import dev.psiae.mltoolbox.uifoundation.themes.md3.spacingOfWindowWidthDp
import kotlin.math.max

@Composable
fun MainScreen() {
    val state = rememberMainScreenState()
    MainScreen(state)
}

@Composable
fun MainScreen(
    state: MainScreenState
) {

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MainScreenLayoutSurface(
            modifier = Modifier,
            color = animateColorAsState(
                if (LocalIsDarkTheme.current) {
                    Material3Theme.colorScheme.surfaceContainer
                } else {
                    MD3Theme.surfaceColorAtElevation(
                        Material3Theme.colorScheme.surfaceContainer,
                        Material3Theme.colorScheme.surfaceTint,
                        1.dp
                    )
                },
                animationSpec = tween(300)
            ).value,
        )
        MainScreenLayoutContent(
            contentPadding = run {
                val margin = MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp
                PaddingValues(margin, 0.dp, margin, 0.dp)
            }
        )
    }
}

@Composable
fun MainScreenLayoutSurface(
    modifier: Modifier,
    color: Color,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color)
            .defaultSurfaceGestureModifiers()
    )
}

@Composable
fun MainScreenLayoutContent(
    contentPadding: PaddingValues
) {
    val leftPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
    val rightPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
    Column(
        modifier = Modifier
            .layout { measurable, constraints ->
                val rtlAware = false
                val start = leftPadding
                val end = rightPadding
                val top = 0.dp
                val bottom = 0.dp
                val horizontal = start.roundToPx() + end.roundToPx()
                val vertical = top.roundToPx() + bottom.roundToPx()

                val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

                val width = constraints.constrainWidth(placeable.width + horizontal)
                val height = constraints.constrainHeight(placeable.height + vertical)
                layout(width, height) {
                    if (rtlAware) {
                        placeable.placeRelative(start.roundToPx(), top.roundToPx())
                    } else {
                        placeable.place(start.roundToPx(), top.roundToPx())
                    }
                    CustomDecorationParameters.controlBoxRightOffset = end.roundToPx()
                }
            }
    ) {
        MainScreenLayoutTopBar(contentPadding = PaddingValues(top = contentPadding.calculateTopPadding()))
        MainScreenLayoutBody()
    }
}

@Composable
fun MainScreenLayoutTopBar(
    contentPadding: PaddingValues = PaddingValues()
) {
    val titleBarBehavior = LocalTitleBarBehavior.current as CustomWin32TitleBarBehavior
    Box(
        modifier = Modifier
    ) {
        Layout(
            modifier = Modifier.height(60.dp),
            content = {
                MainScreenLayoutIconTitle(
                    modifier = Modifier.layoutId("ic")
                )
                MainScreenLayoutCaptionControls(
                    modifier = Modifier.layoutId("cpc")
                )
            }
        ) { measurable, inConstraint ->
            val constraint = inConstraint.noMinConstraints()
            val icon = measurable.fastFirst { it.layoutId == "ic" }
            val iconMeasure = icon.measure(constraint)
            val cpc = measurable.fastFirst { it.layoutId == "cpc" }
            val cpcMeasure = cpc.measure(constraint)

            var maxH = max(iconMeasure.height, cpcMeasure.height)
            val alignment = BiasAlignment.Vertical(0f)

            layout(
                height = constraint.maxHeight,
                width = constraint.maxWidth
            ) {
                iconMeasure.place(
                    x = 0,
                    y = alignment.align(iconMeasure.height, constraint.maxHeight)
                )
                cpcMeasure.place(
                    x = constraint.maxWidth.minus(cpcMeasure.width),
                    y = alignment.align(cpcMeasure.height, constraint.maxHeight)
                )

                CustomDecorationParameters
                    .apply {
                        titleBarHeight = constraint.maxHeight
                        controlBoxTopOffset = alignment.align(cpcMeasure.height, constraint.maxHeight)
                    }

                titleBarBehavior
                    .apply {
                        titleBarHeightPx = constraint.maxHeight
                    }
            }
        }
    }
}

@Composable
private fun MainScreenLayoutIconTitle(
    modifier: Modifier
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        /*Icon(
            modifier = Modifier
                .sizeIn(maxWidth = 64.dp, maxHeight = 36.dp),
            painter = painterResource("drawable/icon_manorlords_logo_text.png"),
            contentDescription = null,
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(MD3Spec.padding.incrementsDp(2).dp))*/
        Text(
            modifier = Modifier,
            text = "MANOR LORDS Toolbox",
            style = Material3Theme.typography.titleMedium,
            fontSize = Material3Theme.typography.titleMedium.nonScaledFontSize(),
            color = Material3Theme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MainScreenLayoutCaptionControls(
    modifier: Modifier
) {
    val titleBarBehavior = LocalTitleBarBehavior.current
    Row(
        modifier = modifier.layout { measurable, constraints ->

            val measure = measurable.measure(constraints)

            CustomDecorationParameters
                .apply {
                    controlBoxWidth = measure.width
                    controlBoxHeight = measure.height
                }

            layout(measure.width, measure.height) {
                measure.place(0, 0)
            }
        }
    ) {
        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.minimizeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_minimize_win1.png"),
                contentDescription = null,
                tint = Material3Theme.colorScheme.onSurface
            )
        }

        run {
            // keep lambda and painter in sync
            val showRestore = titleBarBehavior.showRestoreWindow
            Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
                if (showRestore) titleBarBehavior.restoreClicked() else titleBarBehavior.maximizeClicked()
            }) {
                Icon(
                    modifier = Modifier.size(20.dp).align(Alignment.Center),
                    painter = if (!showRestore)
                        painterResource("drawable/windowcontrol_maximized_win.png")
                    else
                        painterResource("drawable/windowcontrol_restore_down.png"),
                    contentDescription = null,
                    tint = Material3Theme.colorScheme.onSurface
                )
            }
        }

        Box(modifier = Modifier.width(40.dp).height(30.dp).clickable {
            titleBarBehavior.closeClicked()
        }) {
            Icon(
                modifier = Modifier.size(20.dp).align(Alignment.Center),
                painter = painterResource("drawable/windowcontrol_close2.png"),
                contentDescription = null,
                tint = Material3Theme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MainScreenLayoutBody() {
    BoxWithConstraints(
        modifier = Modifier
    ) {
        val maxWidth = maxWidth
        Row {
            val dest = remember { mutableStateOf<StableList<MainDrawerDestination>>(StableList(emptyList()), neverEqualPolicy()) }
            Column(
                modifier = Modifier
                    .widthIn(max = 160.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    MainScreenLayoutDrawerNavigationPanel(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        onDestinationClicked = { select ->
                            if (!dest.value.contains(select)) {
                                dest.value = StableList(
                                    ArrayList<MainDrawerDestination>()
                                        .apply { addAll(dest.value) ; add(select) }
                                )
                            } else {
                                dest.value = StableList(
                                    ArrayList<MainDrawerDestination>()
                                        .apply {
                                            dest.value.forEach {
                                                if (it.id != select.id) add(it)
                                            }
                                            add(select)
                                        }
                                )
                            }
                        },
                        currentDestinationId = dest.value.lastOrNull()?.id
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(1.dp, Material3Theme.colorScheme.outline, RoundedCornerShape(50))
                            .clip(RoundedCornerShape(50))
                            .clickable { GLOBAL_THEME_IS_DARK = !GLOBAL_THEME_IS_DARK }
                            .padding(12.dp)
                    ) {
                        val isDarkTheme = LocalIsDarkTheme.current

                        Icon(
                            modifier = Modifier.align(Alignment.Center).size(24.dp),
                            painter = if (isDarkTheme)
                                painterResource("drawable/icon_dark_theme_moon_outline_24px.png")
                            else
                                painterResource("drawable/icon_light_theme_sun_outline_24px.png"),
                            contentDescription = null,
                            tint = Material3Theme.colorScheme.primary,
                        )
                    }
                }
                HeightSpacer(40.dp)
            }
            Spacer(modifier = Modifier.width(MD3Spec.margin.spacingOfWindowWidthDp(maxWidth.value).dp))
            MainScreenLayoutScreenHost(dest.value)
        }
    }
}

@Composable
fun MainScreenLayoutDrawerNavigationPanel(
    modifier: Modifier,
    onDestinationClicked: (MainDrawerDestination) -> Unit,
    currentDestinationId: String?
) {
    Box(
        modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeightSpacer(16.dp)
            listOf(
                modManagerMainScreenDrawerItem(),
                supportProjectMainScreenDrawerItem()
            ).also {
                LaunchedEffect(Unit) {
                    onDestinationClicked(it.first())
                }
            }.run {
                fastForEachIndexed { i, item ->
                    val isSelected = currentDestinationId == item.id
                    DrawerNavigationPanelItem(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 100.dp),
                        item = item,
                        isSelected = isSelected,
                        enabled = true,
                        onClick = { onDestinationClicked(item) }
                    )
                    if (i < lastIndex) {
                        HeightSpacer(12.dp)
                    }
                }
            }
            HeightSpacer(16.dp)
        }
    }
}

@Composable
fun MainScreenLayoutScreenHost(
    destinations: StableList<MainDrawerDestination>
) {
    if (destinations.isEmpty()) {
        HostNoDestinationSelected()
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .background(remember { Color(29, 24, 34) })
                .defaultSurfaceGestureModifiers()
        ) {
            destinations.fastForEach { dest ->
                key(dest.id) {
                    dest.content.invoke()
                }
            }
        }
    }
}

@Composable
private fun DrawerNavigationPanelItem(
    modifier: Modifier,
    item: MainDrawerDestination,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered = interactionSource.collectIsHoveredAsState().value
    val isFocused = interactionSource.collectIsFocusedAsState().value
    val isDragged = interactionSource.collectIsDraggedAsState().value
    val selectedAnimationProgress = animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(150)
    )
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp, minWidth = 80.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .clickable(
                enabled = !isSelected && enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (item.icon != NoOpPainter) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 56.dp, minHeight = 32.dp)
                    .clip(RoundedCornerShape(50))
                    .composed {
                        val rippleTheme = LocalRippleTheme.current
                        if (isHovered) {
                            Modifier
                                .background(color = rippleTheme.defaultColor().copy(alpha = rippleTheme.rippleAlpha().hoveredAlpha))
                        } else {
                            Modifier
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(
                            if (isSelected)
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Material3Theme.colorScheme.secondaryContainer)
                                    .graphicsLayer { alpha = selectedAnimationProgress.value }
                            else Modifier
                        )
                        .height(32.dp)
                        .width(56.dp * selectedAnimationProgress.value)
                )
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    painter = item.icon,
                    tint = item.iconTint ?: Color.Unspecified,
                    contentDescription = null
                )
            }
            HeightSpacer(4.dp)
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .alpha(if (enabled) 1f else 0.78f),
                text = item.name,
                style = Material3Theme.typography.labelMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                color = Material3Theme.colorScheme.onSurface
            )
        } else {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 80.dp, minHeight = 56.dp)
                    .clip(RoundedCornerShape(50))
                    .composed {
                        val rippleTheme = LocalRippleTheme.current
                        if (isHovered) {
                            Modifier
                                .background(color = rippleTheme.defaultColor().copy(alpha = rippleTheme.rippleAlpha().hoveredAlpha))
                        } else {
                            Modifier
                        }
                    }
            ) {
                var indicationWidth by remember { mutableStateOf(0.dp) }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .then(
                            if (isSelected)
                                Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Material3Theme.colorScheme.secondaryContainer)
                                    .graphicsLayer { alpha = selectedAnimationProgress.value }
                            else Modifier
                        )
                        .height(56.dp)
                        .width(indicationWidth * selectedAnimationProgress.value)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .composed {
                            val density = LocalDensity.current
                            Modifier.onGloballyPositioned { coord ->
                                with(density) {
                                    indicationWidth = coord.size.width.toDp()
                                }
                            }
                        }
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .alpha(if (enabled) 1f else 0.78f),
                        text = item.name,
                        style = Material3Theme.typography.labelMedium,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        color = Material3Theme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun HostNoDestinationSelected() {

}
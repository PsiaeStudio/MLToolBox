package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import dev.psiae.mltoolbox.composeui.HeightSpacer
import dev.psiae.mltoolbox.composeui.LocalApplication
import dev.psiae.mltoolbox.composeui.WidthSpacer
import dev.psiae.mltoolbox.composeui.gestures.defaultSurfaceGestureModifiers
import dev.psiae.mltoolbox.composeui.theme.md3.Material3Theme
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Spec
import dev.psiae.mltoolbox.uifoundation.themes.md3.incrementsDp
import dev.psiae.mltoolbox.uifoundation.themes.md3.padding


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModManagerMainScreen() {
    val app = LocalApplication.current
    val modManager = rememberModManager(app.modManager)
    val modManagerScreen = rememberModManagerScreenState(modManager)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(remember { Color(0XFF14140c) })
            .defaultSurfaceGestureModifiers()
    ) {
        CompositionLocalProvider(
            LocalIndication provides rememberRipple(),
        ) {
            if (!modManagerScreen.hasGameWorkingDirectory) {
                SelectGameWorkingDirectoryScreen(modManagerScreen)
                return@CompositionLocalProvider
            }
            if (modManagerScreen.changingWorkDir) {
                ChangeGameWorkingDirectory(modManagerScreen)
            }
            Column {
                BoxWithConstraints(modifier = Modifier
                    .padding(8.dp)
                    .defaultMinSize(minWidth = 1200.dp)
                    .defaultMinSize(minHeight = 36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shadow(elevation = 2.dp, RoundedCornerShape(4.dp))
                    .clickable { modManagerScreen.userInputChangeWorkingDir() }
                    .background(remember { Color(0xFF313128) })
                    .padding(MD3Spec.padding.incrementsDp(1).dp)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 2.dp)
                            .defaultMinSize(minWidth = with(LocalDensity.current) { constraints.minWidth.toDp() }),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        run {
                            val f = modManagerScreen.gameBinaryFile
                            val fp = modManagerScreen.gameBinaryFile?.absolutePath
                            val (fp1, fp2) = remember(f) {
                                run {
                                    var dash = false
                                    fp?.dropLastWhile { c -> !dash.also { dash = c == '\\' } }
                                } to run {
                                    var dash = false
                                    fp?.takeLastWhile { c -> !dash.also { dash = c == '\\' } }
                                }
                            }
                            val color = remember(f) {
                                fp?.let { Color(250, 250, 250) } ?: Color.White.copy(alpha = 0.78f)
                            }
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
                            tint = remember { Color(0xFFc9c8a5) }
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .then(if (modManagerScreen.checkingUE4SSInstallation) Modifier.alpha(0.1f) else Modifier)
                    ) {
                        if (modManagerScreen.isUE4SSNotInstalled) {
                            UE4SSNotInstalledUI(modManagerScreen)
                        } else {
                            DashBoardUI(modManagerScreen)
                        }
                    }
                    if (modManagerScreen.checkingUE4SSInstallation) {
                        val scroll = rememberScrollState()
                        val viewPortDp = with(LocalDensity.current) {
                            scroll.viewportSize.toDp()
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scroll),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(160.dp),
                                color = Color(0xFFc9cb78)
                            )
                            HeightSpacer((viewPortDp/10).coerceIn(30.dp, 100.dp))
                            Text(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                text = modManagerScreen.checkingUE4SSInstallationStatusMessage ?: "Checking UE4SS Installation ...",
                                style = Material3Theme.typography.titleMedium,
                                color = Color(252, 252, 252),
                                maxLines = 1
                            )
                            HeightSpacer(24.dp)
                        }
                    }
                }
            }
            if (modManagerScreen.installUE4SS) {
                InstallUE4SS(modManagerScreen)
            } else if (modManagerScreen.installUE4SSMod) {
                InstallUE4SSMods(modManagerScreen)
            }
        }
    }
}

@Composable
private fun DashBoardUI(
    modManagerScreenState: ModManagerScreenState
) {
    val scrollState = rememberScrollState()
    Row {
        Column(
            modifier = Modifier
                .weight(1f, false)
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            CommonsSection(modManagerScreenState)
            InstalledModListSection(modManagerScreenState)
        }
        VerticalScrollbar(
            modifier = Modifier
                .height(
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
                    hoverColor = Color.White.copy(alpha = 0.50f)
                )
            }
        )
    }
}

@Composable
private fun CommonsSection(
    modManagerScreenState: ModManagerScreenState
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        /*CommonsSectionHeader()
        HeightSpacer(4.dp)*/
        CommonsSectionContent(modManagerScreenState)
    }
}

@Composable
private fun CommonsSectionHeader(

) {
    Row(
        modifier = Modifier.height(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Common",
            style = Material3Theme.typography.titleSmall,
            color = Color(252, 252, 252),
            maxLines = 1
        )
    }
}

@Composable
private fun CommonsSectionContent(
    modManagerScreen: ModManagerScreenState
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        run {
            val enabled = true
            Box(
                modifier = Modifier
                    .alpha(if (enabled) 1f else 0.38f)
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF938F99).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(enabled = enabled) { modManagerScreen.userInputInstallUE4SS() }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Row(
                    Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(50)),
                        painter = painterResource("drawable/icon_re_ue4ss_repo_logo.png"),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    WidthSpacer(10.dp)
                    Text(
                        modifier = Modifier.alpha(if (enabled) 1f else 0.68f),
                        text = "Install UE4SS",
                        style = Material3Theme.typography.labelLarge,
                        color = Color(0xFFc9cb78),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        run {
            WidthSpacer(8.dp)
            val enabled = true
            Box(
                modifier = Modifier
                    .alpha(if (enabled) 1f else 0.38f)
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF938F99).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(enabled = enabled) { modManagerScreen.userInputInstallUE4SSMod() }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Row(
                    Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeightSpacer(24.dp)
                    Text(
                        modifier = Modifier.alpha(if (enabled) 1f else 0.68f),
                        text = "Install UE4SS Mod",
                        style = Material3Theme.typography.labelLarge,
                        color = Color(0xFFc9cb78),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        run {
            WidthSpacer(16.dp)
            val enabled = !modManagerScreen.launchingGame
            Box(
                modifier = Modifier
                    .alpha(if (enabled) 1f else 0.38f)
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF938F99).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable(enabled = enabled) { modManagerScreen.launchGame() }
                    .padding(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Row(
                    Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (modManagerScreen.launchingGame) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFFc9cb78),
                            strokeWidth = 1.dp
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(50)),
                            painter = painterResource("drawable/1.ico"),
                            contentDescription = null,
                            tint = Color.Unspecified
                        )
                    }
                    WidthSpacer(10.dp)
                    Text(
                        modifier = Modifier.alpha(if (enabled) 1f else 0.68f),
                        text = "Launch Manor Lords",
                        style = Material3Theme.typography.labelLarge,
                        color = Color(0xFFc9cb78),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun UE4SSNotInstalledUI(
    modManagerScreenState: ModManagerScreenState
) {
    val scroll = rememberScrollState()
    val viewPortDp = with(LocalDensity.current) {
        scroll.viewportSize.toDp()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .defaultMinSize(minWidth = 400.dp)
                .padding(horizontal = 48.dp)
                .align(Alignment.CenterHorizontally),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF46492f), contentColor = Color(0xFF2c0b12))
        ) {
            Column(
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier,
                    text = "UE4SS is not installed",
                    style = Material3Theme.typography.titleLarge,
                    color = Color(0xFFffb4ab),
                    maxLines = 1
                )
                HeightSpacer(8.dp)
                Row {
                    Text(
                        modifier = Modifier,
                        text = "[message]: ",
                        style = Material3Theme.typography.bodyMedium,
                        color = Color(0xFFffb4ab),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = (modManagerScreenState.ue4ssNotInstalledMessage ?: "no_message_provided"),
                        style = Material3Theme.typography.bodyMedium,
                        color = Color(0xFFffb4ab),
                        maxLines = 1
                    )
                }
                HeightSpacer(36.dp)
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFc2cd7c))
                            .clickable { modManagerScreenState.userInputRetryCheckUE4SSInstalled() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource("drawable/icon_reload_24px.png"),
                            contentDescription = null,
                            tint = Color(0xFF2d3400)
                        )
                        WidthSpacer(8.dp)
                        Text(
                            modifier = Modifier.weight(1f, false),
                            text = "RETRY",
                            style = Material3Theme.typography.labelLarge.copy(
                                baselineShift = BaselineShift(-0.1f)
                            ),
                            color = Color(0xFF2d3400),
                            maxLines = 1
                        )
                    }
                    WidthSpacer(16.dp)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFc2cd7c))
                            .clickable { modManagerScreenState.userInputInstallUE4SS() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f, false),
                            text = "INSTALL UE4SS",
                            style = Material3Theme.typography.labelLarge.copy(
                                baselineShift = BaselineShift(-0.1f)
                            ),
                            color = Color(0xFF2d3400),
                            maxLines = 1
                        )
                        WidthSpacer(12.dp)
                        Icon(
                            painter = painterResource("drawable/icon_arrow_right_24px.png"),
                            contentDescription = null,
                            tint = Color(0xFF2d3400)
                        )
                    }
                }
            }
        }
        HeightSpacer(24.dp)
    }
}
package dev.psiae.mltoolbox.composeui

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.WindowExceptionHandler
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import dev.psiae.mltoolbox.app.MLToolBoxApp
import dev.psiae.mltoolbox.composeui.core.ComposeUIContextImpl
import dev.psiae.mltoolbox.composeui.core.UIDispatchContextImpl
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.ui.MainUIDispatcher
import dev.psiae.mltoolbox.ui.UIFoundation
import dev.psiae.mltoolbox.composeui.main.MainScreen
import dev.psiae.mltoolbox.composeui.theme.md3.darkColorScheme
import dev.psiae.mltoolbox.uifoundation.themes.md3.MD3Theme
import java.awt.Frame
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.UIManager


class WindowsMainAwtWindow(
    internal val applicationScope: ApplicationScope
) : DesktopMainAwtWindow() {

    internal val titleBarBehavior = CustomWin32TitleBarBehavior(
        this,
        onCloseClicked = applicationScope::exitApplication
    )

    private val pane = ComposePanel()

    private var windowHandle: Long? = null

    init {
        System.setProperty("compose.swing.render.on.graphics", "true")
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (e: Exception) {
        }
        val window = this

        @OptIn(ExperimentalComposeUiApi::class)
        pane.exceptionHandler = WindowExceptionHandler { thr ->

            println("exceptionHandler=$thr")

            // TODO: we can dump exception here
            throw thr
        }

        pane.setContent {
            ProvideApplicationCompositionLocals(
                applicationScope
            ) {
                ProvideCoreUICompositonLocals {
                    ProvideWinCoreUICompositionLocals(
                        awtWindowWrapper = this@WindowsMainAwtWindow,
                    ) {
                        LocalContentColor
                        MaterialTheme(
                            colorScheme = MD3Theme.darkColorScheme
                        ) {
                            MainScreen()
                        }
                    }
                }
            }
        }

        contentPane.add(pane)

        window
            .apply {
                setSize(800, 600)
                title = "ManorLords Toolbox"
                iconImage = run {
                    val resourcePath = "drawable/icon_manorlords_logo_text.png"
                    val contextClassLoader = Thread.currentThread().contextClassLoader!!
                    val resource = contextClassLoader.getResourceAsStream(resourcePath)
                    requireNotNull(resource) {
                        "Resource $resourcePath not found"
                    }.use(::loadImageBitmap).toAwtImage()
                }

                titleBarBehavior.apply {
                    onResized()
                    onMoved()
                }

                addWindowStateListener { wE ->
                    if (window.extendedState == Frame.MAXIMIZED_BOTH) {
                        titleBarBehavior.onMaximizedBoth()
                    } else if (window.extendedState == Frame.NORMAL) {
                        titleBarBehavior.onRestore()
                    }
                }

                addComponentListener(
                    object : ComponentListener {
                        override fun componentResized(p0: ComponentEvent?) {
                            titleBarBehavior.onResized()
                        }

                        override fun componentMoved(p0: ComponentEvent?) {
                            titleBarBehavior.onMoved()
                        }

                        override fun componentShown(p0: ComponentEvent?) {
                        }

                        override fun componentHidden(p0: ComponentEvent?) {
                        }

                    }
                )
            }
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        titleBarBehavior.init(hWnd())
    }

    private fun prepareWindowHandle(): Long {
        val ptr = windowHandle
            ?: Pointer.nativeValue(Native.getWindowPointer(this)).also { windowHandle = it }
        return ptr
    }

    private fun hWnd(): WinDef.HWND = WinDef.HWND().apply { pointer = Pointer(prepareWindowHandle()) }
}



@Composable
private fun ProvideApplicationCompositionLocals(
    applicationScope: ApplicationScope,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalApplication provides MLToolBoxApp.getInstance(),
        LocalComposeApplicationScope provides applicationScope,
        content = content
    )
}

@Composable
private fun ProvideBasePlatformCoreUICompositionLocals(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalRippleTheme provides run {
            remember { object : RippleTheme {
                @Composable
                override fun defaultColor(): Color = RippleTheme.defaultRippleColor(
                    contentColor = remember { Color(250, 250,250) },
                    lightTheme = false
                )

                @Composable
                override fun rippleAlpha(): RippleAlpha = RippleTheme.defaultRippleAlpha(
                    contentColor = remember { Color(250, 250,250) },
                    lightTheme = false
                )
            } }
        },
        content
    )
}

@Composable
private fun ProvideDesktopCoreUICompositionLocals(
    content: @Composable () -> Unit
) {
    ProvideBasePlatformCoreUICompositionLocals(

    ) {
        CompositionLocalProvider(
            content = content
        )
    }
}

@Composable
private fun ProvideWinCoreUICompositionLocals(
    awtWindowWrapper: WindowsMainAwtWindow,
    content: @Composable () -> Unit
) {
    ProvideDesktopCoreUICompositionLocals(

    ) {
        CompositionLocalProvider(
            LocalAwtWindow provides awtWindowWrapper,
            LocalTitleBarBehavior provides awtWindowWrapper.titleBarBehavior,
            content = content
        )
    }
}


@Composable
private fun ProvideCoreUICompositonLocals(
    content: @Composable () -> Unit
) {
    val composeUIContext = remember {
        ComposeUIContextImpl(
            dispatchContext = UIDispatchContextImpl(
                mainDispatcher = UIFoundation.MainUIDispatcher
            )
        )
    }
    CompositionLocalProvider(
        LocalComposeUIContext provides composeUIContext,
        content = content
    )
}
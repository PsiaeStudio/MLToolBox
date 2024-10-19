package dev.psiae.mltoolbox.composeui.main

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.psiae.mltoolbox.app.MLToolBoxApp
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.*
import kotlin.system.exitProcess


fun MainGUI(
    app: MLToolBoxApp
) {
    prepareAWTThread()

    application {
        // we should already be in Swing EQ
        // let compose manage the lifecycle

        // TODO: complete it
        AwtWindow(
            visible = true,
            create = {
                println("AwtWindow: Create")
                PlatformMainAwtWindow()
                    .apply {
                        minimumSize = Dimension(16 * 42, 9 * 42)
                        size = Dimension(16 * 60, 9 * 60)

                        // first show at center
                        run {
                            val screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration)
                            val screenBounds = graphicsConfiguration.bounds
                            val size = IntSize(size.width, size.height)
                            val screenSize = IntSize(
                                screenBounds.width - screenInsets.left - screenInsets.right,
                                screenBounds.height - screenInsets.top - screenInsets.bottom
                            )
                            val location = Alignment.Center.align(size, screenSize, LayoutDirection.Ltr)

                            setLocation(
                                screenBounds.x + screenInsets.left + location.x,
                                screenBounds.y + screenInsets.top + location.y
                            )
                        }
                    }
                    .apply {
                        addWindowListener(
                            object : WindowListener {
                                override fun windowOpened(e: WindowEvent?) {
                                }
                                override fun windowClosing(e: WindowEvent?) {
                                    exitProcess(0)
                                }
                                override fun windowClosed(e: WindowEvent?) {
                                }
                                override fun windowIconified(e: WindowEvent?) {
                                }
                                override fun windowDeiconified(e: WindowEvent?) {
                                }
                                override fun windowActivated(e: WindowEvent?) {
                                }
                                override fun windowDeactivated(e: WindowEvent?) {
                                }
                            }
                        )
                    }
            },
            dispose = {
                it.dispose()
            },
            update = {
                println("AwtWindow: Update")
            },
        )
    }
}

private fun prepareAWTThread() {
    val code = 111
    val handler = Thread.UncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
        try {
            throwable.printStackTrace()
        } finally {
        }
        try {
            JOptionPane.showMessageDialog(JFrame().apply { size = Dimension(300, 300) },
                JTextArea()
                    .apply {
                        val renderText = try {
                            var lines = 0
                            throwable.stackTraceToString().takeWhile { char ->
                                !(char == '\n' && lines++ == 12)
                            }
                        } catch (t: Throwable) {
                            try {
                                throwable.toString()
                            } catch (t: Throwable) {
                                try {
                                    throwable.javaClass.name
                                } catch (t: Throwable) {
                                    "NoStackTrace"
                                }
                            }
                        }
                        text = "Error\n\n$renderText\n\nCTRL + C  to copy"
                        isEditable = false
                        lineWrap = false
                        setFont(UIManager.getDefaults().getFont("JOptionPane.font"))


                        // Add a Ctrl+C action to copy the error message to clipboard
                        val copyKeyStroke: KeyStroke =
                            KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
                        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(copyKeyStroke, "copy")
                        getActionMap().put("copy", object : AbstractAction() {
                            override fun actionPerformed(e: ActionEvent) {
                                val selection = StringSelection(renderText)
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
                            }
                        })
                    },
                "MLToolBox",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            exitProcess(code)
        }
    }
    try {
        EventQueue.invokeAndWait {
            val EDT = Thread.currentThread()
                .apply { uncaughtExceptionHandler = handler }
            if (EDT.uncaughtExceptionHandler !== handler) {
                handler.uncaughtException(Thread.currentThread(), IllegalStateException("Unable to Install UncaughtExceptionHandler on AWT EDT Thread, flag is not set"))
            }
        }
    } catch (t: Throwable) {
        handler.uncaughtException(Thread.currentThread(), IllegalStateException("Unable to Install UncaughtExceptionHandler on AWT EDT Thread", t))
    }
}
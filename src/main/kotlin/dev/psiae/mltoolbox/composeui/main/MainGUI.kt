package dev.psiae.mltoolbox.composeui.main

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.application
import com.sun.nio.file.ExtendedOpenOption
import dev.psiae.mltoolbox.app.MLToolBoxApp
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.ui.MainImmediateUIDispatcher
import dev.psiae.mltoolbox.ui.MainUIDispatcher
import dev.psiae.mltoolbox.ui.UIFoundation
import dev.psiae.mltoolbox.ui.provideMainThread
import kotlinx.coroutines.*
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.FileSystemException
import java.nio.file.StandardOpenOption
import javax.swing.*
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes


fun MainGUI(
    app: MLToolBoxApp
) {
    prepareAWTThread()
    prepareStdKtx()
    prepareUIFoundation()
    acquireProcessFileLock()
    prepareNativeLibs()

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
                            val screenInsets = Toolkit
                                .getDefaultToolkit()
                                .getScreenInsets(graphicsConfiguration)
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



fun querySystemOSBuildVersionStr(): String {
    var buildVersionStr = "UNKNOWN_OS"
    val osName = System.getProperty("os.name")
    when {
        osName.startsWith("Windows") -> {
            val out = StringBuilder()
            Runtime.getRuntime().exec("cmd /c ver").inputStream
                .bufferedReader().readLines().forEachIndexed { i, line ->
                    with(out) {
                        if (isNotEmpty())
                            if (i == 0) append("    ") else append(" ")
                        append(line)
                    }
                }
            buildVersionStr = out.toString().ifBlank { "Windows" }
        }
        osName.startsWith("Mac") -> {
            val out = StringBuilder()
            Runtime.getRuntime().exec("sw_vers -productName").inputStream
                .bufferedReader().readLines().forEachIndexed { i, line ->
                    with(out) {
                        if (isNotEmpty())
                            if (i == 0) append("    ") else append(" ")
                        append(line)
                    }
                }

            Runtime.getRuntime().exec("sw_vers -productVersion").inputStream
                .bufferedReader().readLines().forEachIndexed { i, line ->
                    with(out) {
                        if (isNotEmpty())
                            if (i == 0) append("    ") else append(" ")
                        append(line)
                    }
                }

            buildVersionStr = out.toString().ifBlank { "Mac" }
        }
        osName.startsWith("Linux") || osName.startsWith("LINUX") -> {
            val out = StringBuilder()
            val reader = jFile("/etc/os-release").bufferedReader()
            var line = reader.readLine()
            while (line != null) {
                if (line.startsWith("PRETTY_NAME=")) {
                    out.append(line.drop("PRETTY_NAME=".length).replace("\"", ""))
                    break
                }
                line = reader.readLine()
            }
            buildVersionStr = out.toString().ifBlank { "Linux" }
        }
    }
    return buildVersionStr
}

// TODO: FancyExceptionWindow, default as fallback

val DefaultExceptionWindow = { errorMsg: String, throwable: Throwable ->
    JOptionPane.showMessageDialog(JFrame().apply { size = Dimension(300, 300) },
        JTextArea()
            .apply {
                val renderText = try {
                    throwable.stackTraceToString()
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
                val textBuilder = StringBuilder()
                    .apply {
                        append("Error")
                        if (errorMsg.isNotEmpty()) {
                            append(": $errorMsg")
                            append("\n\n")
                        }
                        append(renderText)

                        run {
                            val jreName = System.getProperty("java.runtime.name")
                            val jreVersion = System.getProperty("java.runtime.version")
                            val jvmName = System.getProperty("java.vm.name")
                            val jvmVersion = System.getProperty("java.vm.version")

                            append("\n")
                            append("\n$jreName (build $jreVersion)")
                            append("\n$jvmName (build $jvmVersion)")

                            runCatching {
                                append("\n\n${querySystemOSBuildVersionStr()}")
                            }
                        }

                        append("\n\nCTRL + C  to copy")
                    }
                text = textBuilder.toString()
                isEditable = false
                lineWrap = false
                setFont(UIManager.getDefaults().getFont("JOptionPane.font"))
                // Add a Ctrl+C action to copy the error message to clipboard
                val copyKeyStroke: KeyStroke =
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().menuShortcutKeyMaskEx)
                getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(copyKeyStroke, "copy")
                getActionMap().put("copy", object : AbstractAction() {
                    override fun actionPerformed(e: ActionEvent) {
                        val selection = StringSelection(text)
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
                    }
                })
            },
        "MLToolBox",
        JOptionPane.ERROR_MESSAGE);
}

// TODO: FancySimpleErrorWindow, default as fallback


private val DefaultSimpleErrorWindow = { errorMsg: String ->
    JOptionPane.showMessageDialog(JFrame().apply { size = Dimension(300, 300) },
        JTextArea()
            .apply {
                text = errorMsg
            },
        "MLToolBox",
        JOptionPane.ERROR_MESSAGE);
}

private fun prepareAWTThread() {
    val handler = Thread.UncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
        try {
            throwable.printStackTrace()
        } finally {
        }
        try {
            DefaultExceptionWindow("exception on EDT thread", throwable)
        } finally {
            exitProcess(0)
        }
    }
    try {
        SwingUtilities.invokeAndWait {
            val EDT = Thread.currentThread()
                .apply { uncaughtExceptionHandler = handler }
            if (EDT.uncaughtExceptionHandler !== handler) {
                handler.uncaughtException(Thread.currentThread(), IllegalStateException("Unable to Install UncaughtExceptionHandler on AWT EDT Thread, flag is not set"))
            }
        }
    } catch (t: Throwable) {
        handler.uncaughtException(Thread.currentThread(), IllegalStateException("Unable to Install UncaughtExceptionHandler on AWT EDT Thread, cause_msg=${t.message}", t))
    }
}

// pre-check kotlinx stuff used for initialization
private fun prepareStdKtx() {
    try {
        runBlocking {
            GlobalScope.launch(Dispatchers.Default) { delay(1) }.join()
            CoroutineScope(SupervisorJob()).async(Dispatchers.IO) { delay(1) }.await()
        }
    } catch (e: Exception) {
        try {
            DefaultExceptionWindow("fail to prepareStdKtx", e)
        } finally {
            exitProcess(0)
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
private fun prepareUIFoundation() {
    try {
        runBlocking {
            launch(UIFoundation.MainImmediateUIDispatcher) {
                UIFoundation.provideMainThread()
            }.join()
        }
    } catch (e: Exception) {
        try {
            DefaultExceptionWindow("fail to prepareUIFoundation", e)
        } finally {
            exitProcess(0)
        }
    }
}


@OptIn(DelicateCoroutinesApi::class)
private fun acquireProcessFileLock() = runCatching {
    runBlocking {
        val processLockJob = CompletableDeferred<Boolean>()
        GlobalScope.launch(UIFoundation.MainUIDispatcher.immediate) {
            async(Dispatchers.IO) {
                runCatching {
                    jFile("mltoolboxapp").mkdir()
                    val processLockFile = jFile("mltoolboxapp\\process.lock")
                    val processLockFileNioPath = processLockFile.toPath()
                    val fileChannel = runCatching {
                        FileChannel
                            .open(
                                processLockFileNioPath,
                                StandardOpenOption.CREATE,
                                StandardOpenOption.WRITE,
                                ExtendedOpenOption.NOSHARE_READ,
                                ExtendedOpenOption.NOSHARE_WRITE,
                                ExtendedOpenOption.NOSHARE_DELETE
                            )
                    }.fold(
                        onSuccess = { it },
                        onFailure = { e ->
                            when (e) {
                                is FileSystemException -> {
                                    processLockJob.complete(false)
                                    DefaultSimpleErrorWindow("Application is already running")
                                    exitProcess(0)
                                }
                            }
                            throw e
                        }
                    )
                    processLockJob.complete(true)
                    // somebody please explain to me why tf it won't stay locked without these ?
                    // compiler ???
                    while (true) {
                        delay(Int.MAX_VALUE.toLong())
                        fileChannel.read(ByteBuffer.wrap(byteArrayOf()))
                    }
                }.onFailure { ex ->
                    runCatching { processLockJob.complete(false) }
                    DefaultExceptionWindow("Exception during processLock", ex)
                    exitProcess(0)
                }
            }.await()
        }
        runCatching {
            withTimeout(3500) { processLockJob.await() }
        }.fold(
            onSuccess = { locked ->
                if (!locked) {
                    runCatching {
                        delay(3.minutes.inWholeMilliseconds)
                        DefaultSimpleErrorWindow("Timeout waiting for process.lock failure to exit, this is a bug if there is no other app process already running")
                    }
                    exitProcess(0)
                }
            },
            onFailure = { ex ->
                if (ex is TimeoutCancellationException) {
                    DefaultSimpleErrorWindow("Timeout waiting for process.lock")
                    exitProcess(0)
                }
                throw ex
            }
        )
    }
}.onFailure { ex ->
    DefaultExceptionWindow("fail to processLock", ex)
    exitProcess(0)
}

private fun prepareNativeLibs() = runCatching {
    NativeLibsInitializer.init()
}.onFailure { e ->
    DefaultExceptionWindow("fail to prepareNativeLibs", e)
    exitProcess(0)
}
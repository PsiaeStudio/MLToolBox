package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.modmanager.launcher.ManorLordsVanillaLauncher
import dev.psiae.mltoolbox.utilskt.isNullOrNotActive
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.File

@Composable
fun rememberModManagerScreenState(
    modManagerComposeState: ModManagerComposeState
): ModManagerScreenState {
    val composeUIContext = LocalComposeUIContext.current
    val state = remember(modManagerComposeState) {
        ModManagerScreenState(modManagerComposeState, composeUIContext)
    }
    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }
    return state
}

class ModManagerScreenState(
    val modManagerComposeState: ModManagerComposeState,
    val uiContext: ComposeUIContext
) {
    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    val immediateDispatcher
        get() = uiContext.dispatchContext.mainDispatcher.immediate

    var gameBinaryFile by mutableStateOf<jFile?>(null)
        private set

    val hasGameWorkingDirectory by derivedStateOf { gameBinaryFile != null }

    var changingWorkDir by mutableStateOf(false)
        private set

    var installUE4SS by mutableStateOf(false)
        private set

    var installUE4SSMod by mutableStateOf(false)
        private set

    var launchingGame by mutableStateOf(false)
        private set

    var ue4ssInstallationCheckWorker: Job? = null
        private set

    var checkingUE4SSInstallation by mutableStateOf(false)
        private set

    var isUE4SSNotInstalled by mutableStateOf(false)
        private set

    var ue4ssNotInstalledMessage by mutableStateOf<String?>(null)
        private set

    var checkingUE4SSInstallationStatusMessage by mutableStateOf<String?>(null)
        private set

    var refreshDashboardWorker: Job? = null
        private set

    var installedModListState by mutableStateOf<InstalledModListState?>(null)

    var currentDrawerDestination by mutableStateOf("dashboard")


    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher)

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {
        if (gameBinaryFile != null) {
            refreshDashboard()
        }
    }

    suspend fun coroutineUIPublication(block: suspend () -> Unit) {
        withContext(uiContext.dispatchContext.mainDispatcher.immediate) {
            block()
        }
    }

    fun chosenGameBinaryFile(file: File?) {
        if (file != null) {
            gameBinaryFile = file
            onChosenGameBinaryFile(file)
        }
        changingWorkDir = false
    }

    fun onChosenGameBinaryFile(file: File?) {
        refreshDashboard()
    }

    fun runOnUiContext(block: () -> Unit) {
        coroutineScope.launch(uiContext.dispatchContext.mainDispatcher.immediate) { block() }
    }

    fun userInputChangeWorkingDir() {
        changingWorkDir = true
    }

    fun userInputInstallUE4SS() {
        installUE4SS = true
    }

    fun installUE4SSExit() {
        installUE4SS = false

        refreshDashboard()
    }

    fun userInputInstallUE4SSMod() {
        installUE4SSMod = true
    }

    fun userInputInstallUE4SSModExit() {
        installUE4SSMod = false

        refreshDashboard()
    }

    fun launchGame() {
        coroutineScope.launch(uiContext.dispatchContext.mainDispatcher.immediate) {
            if (launchingGame) return@launch
            launchingGame = true
            withContext(Dispatchers.IO) {
                gameBinaryFile?.absolutePath?.let {
                    Runtime.getRuntime().exec(it)
                }
            }
            launchingGame = false
        }
    }

    fun launchGameVanilla() {
        ManorLordsVanillaLauncher(coroutineScope, gameBinaryFile!!)
            .apply {
                launch()
            }
    }

    fun userInputRetryCheckUE4SSInstalled() {
        if (ue4ssInstallationCheckWorker.isNullOrNotActive())
            inputCheckUE4SSInstalled()
    }

    fun inputCheckUE4SSInstalled() {
        ue4ssInstallationCheckWorker?.cancel()
        ue4ssInstallationCheckWorker = coroutineScope.launch {
            doCheckUE4SSInstalled()
        }
    }

    fun requireGameBinaryFile(): jFile {
        return checkNotNull(gameBinaryFile) {
            "ModManager: gameBinaryFile not provided"
        }
    }

    fun refreshDashboard() {
        val last = refreshDashboardWorker?.apply { cancel() }
        refreshDashboardWorker = coroutineScope.launch {
            last?.apply {
                try { cancelAndJoin() } catch (_: CancellationException) {}
            }
            ue4ssInstallationCheckWorker?.apply {
                try { cancelAndJoin() } catch (_: CancellationException) {}
            }
            // TODO: redo changingWorkDir
            snapshotFlow { changingWorkDir }.first { !it }
            ue4ssInstallationCheckWorker?.apply {
                try { cancelAndJoin() } catch (_: CancellationException) {}
            }
            inputCheckUE4SSInstalled()
            installedModListState?.refreshSuspend()
            checkingUE4SSInstallation = false
        }
    }

    private suspend fun doCheckUE4SSInstalled() {
        checkingUE4SSInstallation = true
        checkingUE4SSInstallationStatusMessage = "Checking UE4SS Installation ..."

        ue4ssNotInstalledMessage = null
        val workingDir = gameBinaryFile?.parentFile
            ?: error("[ModManagerScreenState]: missing workingDir")
        withContext(Dispatchers.IO) {
            val dwmApi = jFile("$workingDir\\dwmapi.dll")
            if (!dwmApi.exists() || !dwmApi.isFile) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing dwmapi.dll"
                }
                return@withContext
            }
            val ue4ssFolder = jFile("$workingDir\\ue4ss")
            if (!ue4ssFolder.exists() || !ue4ssFolder.isDirectory) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss directory"
                }
                return@withContext
            }
            val ue4ssDll = jFile("$workingDir\\ue4ss\\ue4ss.dll")
            if (!ue4ssDll.exists() || !ue4ssDll.isFile) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss\\ue4ss.dll"
                }
                return@withContext
            }
            val modsFolder = jFile("$workingDir\\ue4ss\\Mods")
            if (!modsFolder.exists() || !modsFolder.isDirectory) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss\\Mods\\ directory"
                }
                return@withContext
            }
            val modsJson = jFile("$workingDir\\ue4ss\\Mods\\mods.json")
            if (!modsJson.exists() || !modsJson.isFile) {
                coroutineUIPublication {
                    isUE4SSNotInstalled = true
                    ue4ssNotInstalledMessage = "missing ue4ss\\Mods\\mods.json"
                }
                return@withContext
            }
            isUE4SSNotInstalled = false
        }
        checkingUE4SSInstallation = false
        checkingUE4SSInstallationStatusMessage = null
    }
}
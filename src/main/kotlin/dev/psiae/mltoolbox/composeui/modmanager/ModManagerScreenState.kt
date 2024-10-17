package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.utilskt.isNullOrNotActive
import kotlinx.coroutines.*
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

    var gameBinaryFile by mutableStateOf<jFile?>(null)
        private set

    val hasGameWorkingDirectory by derivedStateOf { gameBinaryFile != null }

    var changingWorkerDir by mutableStateOf(false)
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
            inputCheckUE4SSInstalled()
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
        changingWorkerDir = false
    }

    fun onChosenGameBinaryFile(file: File?) {
        inputCheckUE4SSInstalled()
    }

    fun runOnUiContext(block: () -> Unit) {
        coroutineScope.launch(uiContext.dispatchContext.mainDispatcher.immediate) { block() }
    }

    fun userInputChangeWorkingDir() {
        changingWorkerDir = true
    }

    fun userInputInstallUE4SS() {
        installUE4SS = true
    }

    fun installUE4SSExit() {
        installUE4SS = false

        inputCheckUE4SSInstalled()
    }

    fun userInputInstallUE4SSMod() {
        installUE4SSMod = true
    }

    fun userInputInstallUE4SSModExit() {
        installUE4SSMod = false

        inputCheckUE4SSInstalled()
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

    private suspend fun doCheckUE4SSInstalled() {
        checkingUE4SSInstallation = true
        checkingUE4SSInstallationStatusMessage = "Checking UE4SS Installation ..."

        isUE4SSNotInstalled = false
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
        }
        checkingUE4SSInstallation = false
        checkingUE4SSInstallationStatusMessage = null
    }
}
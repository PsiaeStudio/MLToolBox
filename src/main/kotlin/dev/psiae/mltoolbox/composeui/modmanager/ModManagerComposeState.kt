package dev.psiae.mltoolbox.composeui.modmanager

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.java.jFile
import dev.psiae.mltoolbox.modmanager.ModManager
import kotlinx.coroutines.*
import java.io.File

@Composable
fun rememberModManager(
    modManager: ModManager
): ModManagerComposeState  {
    val uiContext = LocalComposeUIContext.current
    val manager = remember(modManager, uiContext) {
        ModManagerComposeState(modManager, uiContext)
    }
    DisposableEffect(manager) {
        manager.stateEnter()
        onDispose {
            manager.stateExit()
        }
    }
    return manager
}

@Stable
class ModManagerComposeState(
    val modManager: ModManager,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    var ready by mutableStateOf(false)
        private set



    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher + lifetime)

        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    private fun init() {
        coroutineScope.launch(uiContext.dispatchContext.mainDispatcher.immediate) {
            ready = true
        }
    }
}
package dev.psiae.mltoolbox.composeui.modmanager.manageplayset.direct

import androidx.compose.runtime.*
import dev.psiae.mltoolbox.composeui.core.ComposeUIContext
import dev.psiae.mltoolbox.composeui.core.locals.LocalComposeUIContext
import dev.psiae.mltoolbox.composeui.modmanager.manageplayset.ManagePlaysetScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

@Composable
fun rememberManageDirectPlaysetScreenState(
    managePlaysetScreenState: ManagePlaysetScreenState
): ManageDirectPlaysetScreenState {
    val uiContext = LocalComposeUIContext.current
    val state = remember(managePlaysetScreenState) {
        ManageDirectPlaysetScreenState(managePlaysetScreenState, uiContext)
    }

    DisposableEffect(state) {
        state.stateEnter()
        onDispose { state.stateExit() }
    }

    return state
}

class ManageDirectPlaysetScreenState(
    val managePlaysetScreenState: ManagePlaysetScreenState,
    val uiContext: ComposeUIContext
) {

    private val lifetime = SupervisorJob()
    private var _coroutineScope: CoroutineScope? = null

    private val coroutineScope
        get() = requireNotNull(_coroutineScope) {
            "_coroutineScope is null"
        }

    fun stateEnter() {
        _coroutineScope = CoroutineScope(uiContext.dispatchContext.mainDispatcher.immediate)
        init()
    }

    fun stateExit() {
        lifetime.cancel()
        coroutineScope.cancel()
    }

    fun init() {

    }
}
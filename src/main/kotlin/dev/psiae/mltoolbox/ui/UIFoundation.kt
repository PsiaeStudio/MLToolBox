package dev.psiae.mltoolbox.ui

import dev.psiae.mltoolbox.core.MainImmediateCoroutineDispatcher
import kotlinx.coroutines.*
import javax.swing.SwingUtilities
import kotlin.coroutines.CoroutineContext

object UIFoundation {
}

val UIFoundation.MainUIDispatcher: MainCoroutineDispatcher get() = Swing.MAIN_DISPATCHER
val UIFoundation.MainImmediateUIDispatcher: MainImmediateCoroutineDispatcher get() = Swing.MAIN_DISPATCHER_IMMEDIATE

private object Swing {

    val MAIN_DISPATCHER: MainCoroutineDispatcher = SwingMainCoroutineDispatcher()
    val MAIN_DISPATCHER_IMMEDIATE: MainImmediateCoroutineDispatcher = SwingMainImmediateDispatcher()
    val MAIN_GLOBAL_SCOPE = CoroutineScope(MAIN_DISPATCHER + SupervisorJob())

    @OptIn(InternalCoroutinesApi::class)
    private class SwingMainCoroutineDispatcher(

    ):
        MainCoroutineDispatcher(),
        Delay by org.jetbrains.skiko.MainUIDispatcher as Delay {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            org.jetbrains.skiko.MainUIDispatcher.dispatch(context, block)
        }

        override val immediate: MainImmediateCoroutineDispatcher
            get() = MAIN_DISPATCHER_IMMEDIATE
    }

    @OptIn(InternalCoroutinesApi::class)
    private class SwingMainImmediateDispatcher(

    ) :
        MainImmediateCoroutineDispatcher(),
        Delay by MAIN_DISPATCHER as Delay
    {

        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (SwingUtilities.isEventDispatchThread()) block.run()
            else nonImmediate.dispatch(context, block)
        }

        override val nonImmediate: CoroutineDispatcher
            get() = MAIN_DISPATCHER

        override val immediate: MainImmediateCoroutineDispatcher
            get() = this
    }
}
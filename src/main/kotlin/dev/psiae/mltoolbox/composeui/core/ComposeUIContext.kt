package dev.psiae.mltoolbox.composeui.core

abstract class ComposeUIContext {

    abstract val dispatchContext: UIDispatchContext
}

class ComposeUIContextImpl(
    override val dispatchContext: UIDispatchContext
) : ComposeUIContext() {

}
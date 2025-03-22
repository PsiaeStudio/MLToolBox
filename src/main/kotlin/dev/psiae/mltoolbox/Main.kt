package dev.psiae.mltoolbox

import dev.psiae.mltoolbox.app.MLToolBoxApp
import dev.psiae.mltoolbox.composeui.main.MainGUI

suspend fun main(args: Array<String>) {
    val app = MLToolBoxApp.construct()
    if (args.isNotEmpty())
        handleMainArgs(app, args)
    else MainGUI(app)
}

private suspend fun handleMainArgs(
    app: MLToolBoxApp,
    args: Array<String>
) {
    // NO-OP
}
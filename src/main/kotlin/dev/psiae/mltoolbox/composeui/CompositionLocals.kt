package dev.psiae.mltoolbox.composeui

fun compositionLocalNotProvidedError(name: String): Nothing = error("composition local $name was not provided")
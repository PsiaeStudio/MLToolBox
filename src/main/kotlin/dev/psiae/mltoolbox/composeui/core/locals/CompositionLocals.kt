package dev.psiae.mltoolbox.composeui.core.locals

fun compositionLocalNotProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}

fun staticCompositionLocalNotProvidedFor(name: String): Nothing {
    error("StaticCompositionLocal $name not present")
}
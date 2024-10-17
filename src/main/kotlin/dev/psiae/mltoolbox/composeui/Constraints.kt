package dev.psiae.mltoolbox.composeui

import androidx.compose.ui.unit.Constraints

fun Constraints.noMinConstraints() = copy(minWidth = 0, minHeight = 0)
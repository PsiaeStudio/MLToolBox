package dev.psiae.mltoolbox.utilskt

import kotlinx.coroutines.Job

fun Job?.isNullOrNotActive() = this?.isActive != true
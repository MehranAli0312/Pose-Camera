package com.aipose.camera.posematch.util

private const val FIRST_STRONG_ISOLATE = '⁨'
private const val POP_DIRECTIONAL_ISOLATE = '⁩'

fun String.bidiIsolate(): String = "$FIRST_STRONG_ISOLATE$this$POP_DIRECTIONAL_ISOLATE"

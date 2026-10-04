package com.aipose.camera.posematch.domain.time

import java.util.TimeZone

private const val MILLIS_PER_DAY = 86_400_000L

fun localDayIndex(millis: Long): Long {
    val offset = TimeZone.getDefault().getOffset(millis)
    return (millis + offset) / MILLIS_PER_DAY
}

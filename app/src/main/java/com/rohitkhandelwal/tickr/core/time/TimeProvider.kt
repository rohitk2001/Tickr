package com.rohitkhandelwal.tickr.core.time

fun interface TimeProvider {
    fun now(): Long
}

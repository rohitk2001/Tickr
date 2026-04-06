package com.rohitkhandelwal.tickr.core.time

object SystemTimeProvider : TimeProvider {
    override fun now(): Long = System.currentTimeMillis()
}

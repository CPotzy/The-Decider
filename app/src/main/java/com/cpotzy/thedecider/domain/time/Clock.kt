package com.cpotzy.thedecider.domain.time

import java.time.Instant

fun interface Clock {
    fun now(): Instant

    companion object {
        val System: Clock = Clock { Instant.now() }
        fun fixed(at: Instant): Clock = Clock { at }
    }
}

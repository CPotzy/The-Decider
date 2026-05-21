package com.cpotzy.thedecider.domain.model

enum class Duration(val maxMinutes: Int) {
    QUICK(5),
    SHORT(15),
    MEDIUM(30),
    LONG(Int.MAX_VALUE);
}

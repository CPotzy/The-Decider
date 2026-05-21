package com.cpotzy.thedecider.domain.model

enum class Cadence(val cadenceDays: Long?) {
    DAILY(1L),
    BIDAILY(2L),
    WEEKLY(7L),
    BIWEEKLY(14L),
    MONTHLY(30L),
    BIMONTHLY(60L),
    ANYTIME(null),
    ONEOFF(null);
}

package com.aliucord.coreplugins.polls.creation

internal enum class Duration(val text: String, val value: Int) {
    ONE_HOUR("1 hour", 1),
    FOUR_HOURS("4 hours", 4),
    EIGHT_HOURS("8 hours", 8),
    ONE_DAY("24 hours", 24),
    THREE_DAYS("3 days", 72),
    ONE_WEEK("1 week", 168),
    TWO_WEEKS("2 weeks", 336),
}

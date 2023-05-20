package com.ted.gartenhelferlein.task

data class Task (
    val id: Int,
    val name: String,
    val description: String,
    val location: String = "",
    val frequency: java.time.Duration = java.time.Duration.ofDays(1),
    val duration: java.time.Duration = java.time.Duration.ofMinutes(0),
    var lastCompletion: java.time.LocalDateTime = java.time.LocalDateTime.now()
){
    val hasDuration: Boolean = duration.toMinutes().toInt() != 0
    private var secondLastCompletion: java.time.LocalDateTime = java.time.LocalDateTime.now()-frequency

    init {
        assert(!(frequency.isZero || frequency.isNegative)) { "Frequency must be positive" }
    }

    /**
     * Completes the task and updates the urgency and time left.
     */
    fun complete() {
        secondLastCompletion = lastCompletion
        lastCompletion = java.time.LocalDateTime.now()
    }

    fun revert() {
        lastCompletion = secondLastCompletion
    }

    fun urgency(): Float {
        return secLeft().toFloat() / (frequency.toMinutes()*60).toFloat()
    }

    fun isCompleted(): Boolean {
        return urgency() <= 0.7
    }

    fun secLeft(): Int {
        return -java.time.LocalDateTime.now().until(lastCompletion, java.time.temporal.ChronoUnit.SECONDS).toInt()
    }

    fun printLastCompleted(): String {
        return when (
            val secLeft = secLeft()
        ) {
            in 0..20 -> "Zuletzt gerade eben"
            in 21..59 -> "Zuletzt vor $secLeft Sekunden"
            in 60..119 -> "Zuletzt vor einer Minute"
            in 120..3600 -> "Zuletzt vor ${secLeft/60} Minuten"
            in 3600..7199 -> "Zuletzt vor einer Stunde"
            in 7200..86400 -> "Zuletzt vor ${secLeft/3600} Stunden"
            in 86400..172799 -> "Zuletzt vor einem Tag"
            in 172800..604800 -> "Zuletzt vor ${secLeft/86400} Tagen"
            in 604800..1209599 -> "Zuletzt vor einer Woche"
            in 1209600..2678400 -> "Zuletzt vor ${secLeft/604800} Wochen"
            else -> "Zuletzt vor ${secLeft/2678400} Monaten"
        }
    }

    fun printFrequency(): String {
        return when {
            frequency.toDays() == 7L -> "Wöchentlich"
            frequency.toDays() >= 2 -> "Alle ${frequency.toDays()} Tage"
            frequency.toHours() == 24L -> "Täglich"
            frequency.toHours() >= 2L -> "Alle ${frequency.toHours()} Stunden"
            frequency.toMinutes() == 60L -> "Stündlich"
            frequency.toMinutes() > 0L -> "Alle ${frequency.toMinutes()} Minuten"
            else -> "Häufigkeit"
        }
    }
}

package com.ted.gartenhelferlein.task

import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Datatype for a task stored in the server.
 * @param id The id of the task.
 * @param name The name of the task.
 * @param description The description of the task.
 * @param frequency The duration of the task in minutes.
 * @param lastCompleted The time in epoch seconds when the task was last completed.
 */
@Serializable
data class Task(
    val id: Int? = null,
    val name: String,
    val description: String,
    val frequency: Long, // in minutes
    val lastCompleted: Long // in epoch seconds
) {
    fun toTaskData(): TaskData {
        var adjFrequency = frequency
        if (frequency == 0L) {
            adjFrequency = 2L
        } else if (frequency < 0L) {
            adjFrequency = -frequency
        }
        return TaskData(
            id = id!!,
            name = name,
            description = description,
            frequency = Duration.ofMinutes(adjFrequency),
            lastCompletion = LocalDateTime.ofEpochSecond(lastCompleted, 0, ZoneOffset.UTC)
        )
    }
}

/**
 * Datatype for a task stored in the app.
 * @param id The id of the task.
 * @param name The name of the task.
 * @param description The description of the task.
 * @param location The location of the task.
 * @param frequency The frequency of the task.
 * @param duration The duration of the task.
 * @param lastCompletion The time when the task was last completed.
 */
data class TaskData (
    val id: Int,
    val name: String,
    val description: String,
    val location: String = "",
    val frequency: Duration = Duration.ofDays(1),
    val duration: Duration = Duration.ofMinutes(0),
    var lastCompletion: LocalDateTime = LocalDateTime.now()
){
    val hasDuration: Boolean = duration.toMinutes().toInt() != 0
    private var secondLastCompletion: LocalDateTime = LocalDateTime.now()-frequency

    init {
        assert(!(frequency.isZero || frequency.isNegative)) { "Frequency must be positive" }
    }

    /**
     * Completes the task and updates the urgency and time left.
     */
    fun complete() {
        secondLastCompletion = lastCompletion
        lastCompletion = LocalDateTime.now()
    }

    fun revert() {
        lastCompletion = secondLastCompletion
    }

    /**
     * Returns the urgency of the task, calculated by the fraction of time left until the task is due since the last completion.
     * @return The urgency of the task.
     */
    fun urgency(): Float {
        return secLeft().toFloat() / (frequency.toMinutes()*60).toFloat()
    }

    fun isCompleted(): Boolean {
        return urgency() <= 0.7
    }

    private fun secLeft(): Int {
        return -LocalDateTime.now().until(lastCompletion, java.time.temporal.ChronoUnit.SECONDS).toInt()
    }

    /**
     * Returns a string representation of the time left until the task is due.
     * @return A string representation of the time left until the task is due.
     */
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

    /**
     * Returns a string representation of the frequency of the task.
     * @return A string representation of the frequency of the task.
     */
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

    /**
     * Returns a Task object from the TaskData object.
     * @return Task(id, name, description, duration.toMinutes(), lastCompletion.toEpochSecond(java.time.ZoneOffset.UTC))
     */
    fun toTask(): Task {
        return Task(
            id = id,
            name = name,
            description = description,
            frequency = frequency.toMinutes(),
            lastCompleted = lastCompletion.toEpochSecond(ZoneOffset.UTC)
        )
    }
}
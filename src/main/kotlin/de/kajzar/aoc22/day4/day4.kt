package de.kajzar.aoc22.day4

import de.kajzar.aoc22.Task
import java.io.BufferedReader

typealias Assignment = IntRange

fun <T> List<T>.deconstructPair(): Pair<T, T> = first() to last()

fun String.parseAssignment(): Assignment {
    val (first, second) = this.split("-")
        .map { it.toInt() }
        .deconstructPair()
    return Assignment(first, second)
}

fun Assignment.fullyContains(other: Assignment): Boolean {
    return this.contains(other.first) && this.contains(other.last)
}

fun Assignment.overlapsWith(other: Assignment): Boolean {
    return contains(other.first) || contains(other.last)
            || other.contains(first) || other.contains(last)
}

fun Pair<Assignment, Assignment>.oneFullyContainsOther(): Boolean {
    val (a, b) = this
    return a.fullyContains(b) || b.fullyContains(a)
}

fun Pair<Assignment, Assignment>.overlap(): Boolean {
    val (a, b) = this
    return a.overlapsWith(b)
}

private fun BufferedReader.readAssignmentPairs(): Sequence<Pair<Assignment, Assignment>> {
    return lineSequence()
        .map { content ->
            content.split(",")
                .map { it.parseAssignment() }
                .deconstructPair()
        }
}

fun main() {
    // part 1
    Task(4)
        .readInput()
        .readAssignmentPairs()
        .count { it.oneFullyContainsOther() }
        .also { println("Count: $it") }

    // part 2
    Task(4)
        .readInput()
        .readAssignmentPairs()
        .count { it.overlap() }
        .also { println("Count: $it") }
}
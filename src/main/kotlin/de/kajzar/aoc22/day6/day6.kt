package de.kajzar.aoc22.day6

import de.kajzar.aoc22.Task
import java.io.BufferedReader

data class Datastream(
    private val stream: List<Char>
) {

    private fun marker(windowSize: Int): Int {
        for ((index, window) in stream.windowed(windowSize).withIndex()) {
            if (window.allDifferent()) {
                return index + window.size
            }
        }
        error("No marker found")
    }

    fun startOfPackage(): Int = marker(windowSize = 4)
    fun startOfMessage(): Int = marker(windowSize = 14)

    private fun List<Char>.allDifferent() = distinct().size == size
}

private fun BufferedReader.readStream(): Datastream {
    return Datastream(
        lineSequence()
            .joinToString(separator = "")
            .toCharArray()
            .toList()
    )
}

fun main() {
    // part 1
    val datastream = Task(6)
        .readInput()
        .readStream()

    datastream.startOfPackage().also { println("Package: $it") }
    datastream.startOfMessage().also { println("Message: $it") }
}
package de.kajzar.aoc22

class Task(private val day: Int) {
    fun readInput() = this::class.java.getResourceAsStream("/input/$day.txt").bufferedReader()
}

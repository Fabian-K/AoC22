package de.kajzar.aoc22.day1

import de.kajzar.aoc22.Task
import java.io.BufferedReader

data class Elve(
    val number: Int,
    val bag: List<Food>,
) {
    fun calories() = bag.sumOf { it.calories }
}

data class Food(val calories: Int)

private fun BufferedReader.parseElves() = sequence {
    var nextElveNumber = 1
    var nextElveBag = mutableListOf<Food>()

    for (line in lineSequence().iterator()) {
        if (line.isBlank()) {
            yield(Elve(nextElveNumber, nextElveBag))
            nextElveBag = mutableListOf()
            nextElveNumber += 1
        } else {
            nextElveBag.add(Food(line.toInt()))
        }
    }

    yield(Elve(nextElveNumber, nextElveBag))
}

fun main() {
    val task = Task(1)

    // part 1
    task.readInput()
        .parseElves()
        .maxBy { it.calories() }
        .let { println("Elve ${it.number} caries the most calories with ${it.calories()}") }

    // part 2
    task.readInput()
        .parseElves()
        .sortedByDescending { it.calories() }
        .take(3)
        .sumOf { it.calories() }
        .let { println("Top 3 elves carry $it calories") }
}
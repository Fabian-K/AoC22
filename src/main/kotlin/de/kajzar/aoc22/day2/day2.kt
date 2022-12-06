package de.kajzar.aoc22.day2

import de.kajzar.aoc22.Task
import java.io.BufferedReader


enum class Pick(
    val alias: List<Char>,
    private val beats: Char,
    val score: Int,
) {
    Rock(listOf('A', 'X'), 'C', 1),
    Paper(listOf('B', 'Y'), 'A', 2),
    Scissors(listOf('C', 'Z'), 'B', 3);

    fun beats(other: Pick): Boolean = beats() == other
    fun beats(): Pick = beats.toPick()
}

data class Round(
    val opponentPick: Pick,
    val ownPick: Pick,
) {
    fun score(): Int {
        var result = 0

        result += ownPick.score

        if (ownPick.beats(opponentPick)) {
            result += 6
        }
        if (opponentPick == ownPick) {
            result += 3
        }

        return result
    }
}

data class Strategy(
    val rounds: List<Round>
) {
    fun score() = rounds.sumOf { it.score() }
}

private fun BufferedReader.charPairSequence(): Sequence<Pair<Char, Char>> {
    return lineSequence()
        .map { l ->
            val parts = l.split(" ").map { it.toCharArray().single() }
            parts.first() to parts.last()
        }
}

private fun BufferedReader.parseStrategy(): Strategy {
    val rounds = charPairSequence()
        .map { (first, second) ->
            val opponentPick = first.toPick()
            val ownPick = second.toPick()
            Round(opponentPick, ownPick)
        }
        .toList()
    return Strategy(rounds)
}

enum class RequiredResult(val char: Char) {
    Loss('X'),
    Draw('Y'),
    Win('Z');

    fun getPickForInput(other: Pick): Pick {
        return when (this) {
            Loss -> other.beats()
            Draw -> other
            Win -> Pick.values().single { it.beats(other) }
        }
    }
}

private fun BufferedReader.parseStrategyPart2(): Strategy {
    val rounds = charPairSequence()
        .map { (first, second) ->
            val opponentPick = first.toPick()
            val requiredResult = second.toRequiredResult()
            Round(opponentPick, requiredResult.getPickForInput(opponentPick))
        }
        .toList()
    return Strategy(rounds)
}

private fun Char.toRequiredResult() = RequiredResult.values().single { it.char == this }
private fun Char.toPick() = Pick.values().single { it.alias.contains(this) }

fun main() {
    // part 1
    Task(2)
        .readInput()
        .parseStrategy()
        .let { println("Score Part 1: ${it.score()}") }

    // part 2
    Task(2)
        .readInput()
        .parseStrategyPart2()
        .let { println("Score Part 2: ${it.score()}") }
}
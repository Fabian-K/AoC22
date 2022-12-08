package de.kajzar.aoc22.day5

import de.kajzar.aoc22.Task
import java.io.BufferedReader
import java.util.Stack

typealias Item = Char
typealias Position = Int

data class Storage(
    private val stacks: Map<Position, Stack<Item>>
) {

    fun stack(position: Position) = stacks[position] ?: error("No stack at position $position")

    fun executeWithCrateMover9000(instructions: List<Instruction>) {
        for ((count, source, target) in instructions) {
            repeat(count) {
                val item = stack(source).pop()
                stack(target).push(item)
            }
        }
    }

    fun executeWithCrateMover9001(instructions: List<Instruction>) {
        for ((count, source, target) in instructions) {
            val movingItems = mutableListOf<Item>()
            repeat(count) { movingItems.add(stack(source).pop()) }
            movingItems.reversed().forEach { stack(target).push(it) }
        }
    }

    fun topOfStacks(): List<Item> {
        return stacks.values.map { it.peek() }
    }

}

data class Instruction(
    val count: Int,
    val source: Position,
    val target: Position,
)

data class InitialState(
    val storage: Storage,
    val instructions: List<Instruction>,
)

private fun BufferedReader.readInitialState(): InitialState {

    val lines = readLines()

    // read state
    val initialStateLines = lines
        .takeWhile { it.isNotEmpty() }

    // stack position -> item position
    val itemPositions = (1..9).associateWith { ((it - 1) * 4) + 1 }

    val storage = Storage(
        stacks = (1..9).associateWith { Stack() }
    )

    // build up storage from bottom to top
    initialStateLines.reversed()
        .drop(1) // "header"
        .forEach {
            // extract item from string and add to stack
            for ((targetStack, position) in itemPositions) {
                val item = it.toCharArray()[position]
                if (!item.isWhitespace()) {
                    storage.stack(targetStack).push(item)
                }
            }
        }

    // parse instructions
    val instructions = lines.dropWhile { it.isNotEmpty() }
        .drop(1) // separator between state and instructions
        .map { it.parseInstruction() }

    return InitialState(storage, instructions)
}

private fun String.parseInstruction(): Instruction {
    val numbers = split(" ")
        .mapNotNull { it.toIntOrNull() }
    return Instruction(numbers[0], numbers[1], numbers[2])
}

fun main() {
    // part 1
    val (storage, instructions) = Task(5)
        .readInput()
        .readInitialState()

    storage.executeWithCrateMover9000(instructions)

    storage.topOfStacks()
        .joinToString(separator = "")
        .also { println("Result: $it") }

    // part 2
    val (storage2, instructions2) = Task(5)
        .readInput()
        .readInitialState()

    storage2.executeWithCrateMover9001(instructions2)

    storage2.topOfStacks()
        .joinToString(separator = "")
        .also { println("Result: $it") }
}
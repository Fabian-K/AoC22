package de.kajzar.aoc22.day11

import de.kajzar.aoc22.Task
import java.io.BufferedReader


typealias Item = Long
typealias LevelOfWorried = Long
typealias MonkeyNumber = Int

data class Monkey(
    val number: MonkeyNumber,
    val items: MutableList<Item>,
    val op: (Item) -> LevelOfWorried,
    val test: (LevelOfWorried) -> Boolean,
    val nextMonkey: (Boolean) -> MonkeyNumber,
) {
    override fun toString(): String {
        return "Monkey(number=$number, items=$items)"
    }

    fun receive(item: Item) = items.add(item)
}

fun LevelOfWorried.whenBored(coolDownFactor: Int) = floorDiv(coolDownFactor)

private fun BufferedReader.parseMonkeys(): List<Monkey> {
    val lines = readLines()

    // split input by monkey
    val monkeyLines = buildList {
        val monkey = mutableListOf<String>()
        for (line in lines) {
            if (line.isBlank()) {
                add(monkey.toList())
                monkey.clear()
            } else {
                monkey.add(line)
            }
        }
        if (monkey.isNotEmpty())
            add(monkey.toList())
    }

    // map to monkey
    return monkeyLines
        .map { lines ->
            val number = lines[0]
                .substringAfter("Monkey ")
                .substringBefore(":")
                .toInt()

            val items = lines[1]
                .substringAfter(":")
                .split(",")
                .map { it.trim().toLong() }

            Monkey(
                number = number,
                items = items.toMutableList(),
                op = lines[2].toOperation(),
                test = lines[3].toTest(),
                nextMonkey = lines.subList(4, 6).toNextMonkey()
            )
        }
}

private fun List<String>.toNextMonkey(): (Boolean) -> MonkeyNumber {
    val nextOnTrue = first().substringAfterLast(" ").toInt()
    val nextOnFalse = last().substringAfterLast(" ").toInt()
    return { t ->
        if (t) {
            nextOnTrue
        } else {
            nextOnFalse
        }
    }
}

private fun String.toTest(): (LevelOfWorried) -> Boolean {
    val divisor = this.substringAfterLast(" ").toInt()
    return { level -> level.mod(divisor) == 0 }
}

enum class MathOperator {
    MULTIPLY, ADD;

    fun perform(a: Long, b: Long): Long = when (this) {
        MULTIPLY -> (a * b).also { if (it < 0) error("Overflow! for $a & $b") }
        ADD -> (a + b).also { if (it < 0) error("Overflow! for $a & $b") }
    }
}

fun String.toMathOperator() = when (this) {
    "*" -> MathOperator.MULTIPLY
    "+" -> MathOperator.ADD
    else -> error("No math operator for $this")
}

class Game(
    val monkeys: List<Monkey>,
    val coolDownFactor: Int,
    val onAfterRound: (round: Int) -> Unit = { _ -> },
    val onItemInspection: (monkey: MonkeyNumber, item: Item) -> Unit = { _, _ -> },
) {

    private var nextRound = 1

    fun playRound() {
        val round = nextRound++

        for (monkey in monkeys) {
            for (item in monkey.items) {
                onItemInspection(monkey.number, item)
                val worryLevel = monkey.op(item)
                val adjustedWorryLevel = worryLevel.whenBored(coolDownFactor)

                val testResult = monkey.test(adjustedWorryLevel)
                val nextMonkey = monkey.nextMonkey(testResult)

                monkeys.single { it.number == nextMonkey }
                    .receive(adjustedWorryLevel)
            }
            // monkey threw all items -> clear
            monkey.items.clear()
        }

        onAfterRound(round)
    }
}

private fun String.toOperation(): (Item) -> LevelOfWorried {
    val parts = substringAfter("= ").split(" ")

    val argumentA = parts.first()
    val operation = parts[1].toMathOperator()
    val argumentB = parts.last()

    return { item ->
        fun String.resolve() = if (this == "old") {
            item
        } else {
            this.toLong()
        }

        operation.perform(argumentA.resolve(), argumentB.resolve())
    }
}

class MonkeyObserver {
    private val inspections = mutableMapOf<MonkeyNumber, Int>()

    fun onItemInspection(monkey: MonkeyNumber) {
        val count = inspections[monkey] ?: 0
        inspections[monkey] = count + 1
    }

    fun monkeyBusiness(): Int {
        val byMonkey = inspections.toList()
            .sortedByDescending { (_, count) -> count }

        return byMonkey.take(2)
            .map { (_, inspections) -> inspections }
            .reduce { a, b -> a * b }
    }

}

fun main() {

    val monkeys = Task(11)
        .readInput()
        .parseMonkeys()

    val monkeyObserver = MonkeyObserver()

    Game(
        monkeys = monkeys,
        coolDownFactor = 3,
        onItemInspection = { monkey, _ -> monkeyObserver.onItemInspection(monkey) }
    ).apply {
        repeat(20) {
            playRound()
        }
    }

    monkeyObserver.monkeyBusiness()
        .also { println(it) }
}
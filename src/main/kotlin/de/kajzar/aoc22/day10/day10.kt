package de.kajzar.aoc22.day10

import de.kajzar.aoc22.Task
import java.io.*

sealed interface Instruction {
    fun toExecution(currentCycle: Int): Execution
}

data class Add(val amount: Int) : Instruction {
    override fun toExecution(currentCycle: Int): Execution = Execution(this, currentCycle + 2)
}

object Noop : Instruction {
    override fun toExecution(currentCycle: Int): Execution = Execution(this, currentCycle + 1)
}

data class Execution(
    val instruction: Instruction,
    val finishedAt: Int,
)

data class Reading(
    val cycle: Int,
    val value: Int,
) {
    fun signalStrength() = cycle * value
}

data class RegisterHistory(
    val data: MutableMap<Int, Int> = mutableMapOf() // cycle to value
) {
    fun track(cycle: Int, register: Int) {
        data[cycle] = register
    }

    fun read(cycles: List<Int>) = cycles.map {
        Reading(it, data[it] ?: error("No history for cycle $it"))
    }
}

class CPU(
    val onCycle: (cycle: Int, register: Int) -> Unit = { _, _ -> }
) {
    private var register: Int = 1
    private var cycle: Int = 0

    private var queue: MutableList<Instruction> = mutableListOf()
    private var execution: Execution? = null

    private fun tick() {

        // start of cycle
        // if no execution -> take next from queue
        if (execution == null) {
            execution = queue.removeFirstOrNull()?.toExecution(cycle)
        }

        // cycle
        cycle++
        onCycle(cycle, register)

        // end of cycle if current execution is finished -> apply effects & clear
        execution?.let {
            if (it.finishedAt == cycle) {
                when (it.instruction) {
                    is Add -> register += it.instruction.amount
                    else -> {}
                }
                execution = null
            }
        }
    }

    fun addInstructions(instructions: List<Instruction>) {
        queue.addAll(instructions)
    }

    fun execute() {
        while (queue.isNotEmpty() || execution != null) {
            tick()
        }
    }
}

class CRT(
    private val print: (s: String) -> Unit = { s: String -> kotlin.io.print(s) }
) {

    fun onCycle(cycle: Int, register: Int) {
        val spriteRange = register - 1..register + 1
        val screenPosition = (cycle - 1) % 40

        if (screenPosition == 0)
            this.print(System.lineSeparator())

        if (screenPosition in spriteRange) {
            this.print("#")
        } else {
            this.print(".")
        }

    }
}

private fun BufferedReader.parseInstructions(): List<Instruction> {
    return readLines()
        .map {
            val parts = it.split(" ")

            when (parts.first()) {
                "noop" -> Noop
                "addx" -> Add(parts.last().toInt())
                else -> error("Unknown instruction $it")
            }
        }
}

fun main() {
    val instructions = Task(10)
        .readInput()
        .parseInstructions()

    // part 1
    val history = RegisterHistory()
    CPU(onCycle = { cycle, register -> history.track(cycle, register) }).apply {
        addInstructions(instructions)
        execute()
    }

    history.read(listOf(20, 60, 100, 140, 180, 220))
        .sumOf { it.signalStrength() }
        .also { println(it) }

    // part 2
    val crt = CRT()
    CPU(onCycle = { cycle, register -> crt.onCycle(cycle, register) }).apply {
        addInstructions(instructions)
        execute()
    }

}
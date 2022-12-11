package de.kajzar.aoc22.day9

import de.kajzar.aoc22.Task
import java.io.BufferedReader
import kotlin.math.abs


enum class Direction {
    U, R, L, D;

    fun by(amount: Int) = when (this) {
        U -> Motion(0, amount)
        D -> Motion(0, -amount)
        R -> Motion(amount, 0)
        L -> Motion(-amount, 0)
    }
}

data class Position(val x: Int, val y: Int) {
    override fun toString(): String = "$x/$y"
    fun touches(other: Position): Boolean {
        return abs(x - other.x) <= 1 && abs(y - other.y) <= 1
    }

    fun moveBy(motion: Motion): Position = Position(x + motion.dx, y + motion.dy)
    fun motionTo(other: Position) = Motion(other.x - x, other.y - y)
}

data class Motion(val dx: Int, val dy: Int) {
    fun steps(): List<Motion> = buildList {
        var remainingMotion = copy()
        while (remainingMotion != Motion(0, 0)) {
            val stepMotion = remainingMotion.capped()
            remainingMotion -= stepMotion
            add(stepMotion)
        }
    }

    private operator fun minus(other: Motion): Motion = Motion(this.dx - other.dx, this.dy - other.dy)

    fun capped(): Motion {
        return Motion(dx.capped(), dy.capped())
    }
}

fun Int.capped(): Int {
    if (this >= 1) return 1
    if (this <= -1) return -1
    return 0
}

data class Tracker(private val positions: MutableList<Position> = mutableListOf()) {
    fun track(position: Position) = positions.add(position)
    fun visitedPositions() = positions
}

data class Rope(var segments: List<Position>) {
    fun head() = segments.first()
    fun tail() = segments.last()
    fun move(motion: Motion) {

        val updatedSegments = mutableListOf<Position>()

        for ((i, segment) in segments.withIndex()) {
            // move head by motion
            if (i == 0) {
                updatedSegments.add(segment.moveBy(motion))
                continue
            }

            // remaining follow previous
            val previous = updatedSegments[i - 1]
            if (!segment.touches(previous)) {
                val tailMotion = segment.motionTo(previous)
                    .capped()
                updatedSegments.add(segment.moveBy(tailMotion))
            } else {
                updatedSegments.add(segment)
            }
        }

        segments = updatedSegments
    }

    fun segmentsAt(position: Position) = segments.count { it == position }
}

class Simulation(
    ropeSegments: Int,
    private val afterStep: (s: Simulation) -> Unit = {},
    private val afterMotion: (s: Simulation) -> Unit = {},
) {
    val rope = Rope((0 until ropeSegments).map { Position(0, 0) })

    val tailTracker = Tracker().apply {
        track(rope.tail())
    }

    fun run(motions: List<Motion>) = apply {
        for (motion in motions) {
            run(motion)
            afterMotion(this)
        }
    }

    fun dump(xRange: IntRange, yRange: IntRange) = apply {
        for (y in yRange.reversed()) {
            val line = buildString {
                for (x in xRange) {
                    val segments = rope.segmentsAt(Position(x, y))
                    if (segments == 0)
                        append("*")
                    else
                        append("$segments")
                }
            }
            println(line)
        }
        println("-".repeat(xRange.count()))
    }

    private fun run(motion: Motion) {
        // split motion into steps and apply individually
        for (step in motion.steps()) {
            rope.move(step)
            tailTracker.track(rope.tail())
            afterStep(this)
        }
    }
}

private fun BufferedReader.parseMotions(): List<Motion> {
    return readLines()
        .map {
            val parts = it.split(" ")
            val direction = Direction.valueOf(parts.first())
            val amount = parts.last().toInt()
            direction.by(amount)
        }
}

fun main() {
    val motions = Task(9)
        .readInput()
        .parseMotions()

    // part 1
    Simulation(ropeSegments = 2)
        .run(motions)
        .tailTracker.visitedPositions().distinct()
        .also { println("Positions: ${it.count()}") }

    // part 2
    Simulation(ropeSegments = 10)
        .run(motions)
        .tailTracker.visitedPositions().distinct()
        .also { println("Positions: ${it.count()}") }
}
package de.kajzar.aoc22.day14

import de.kajzar.aoc22.Task
import java.io.BufferedReader

typealias Coordinates = Pair<Int, Int>

fun Coordinates.x() = first
fun Coordinates.y() = second

typealias Path = List<Coordinates>

fun Path.coordinates(): List<Coordinates> {
    return this.windowed(2)
        .flatMap {
            val a = it.minBy { it -> it.x() + it.y() }
            val b = it.maxBy { it -> it.x() + it.y() }

            buildList {
                for (x in (a.x()..b.x())) {
                    for (y in (a.y()..b.y())) {
                        add(x to y)
                    }
                }
            }
        }
}

enum class Type {
    AIR, SAND, ROCK;

    override fun toString(): String = when (this) {
        AIR -> "."
        SAND -> "*"
        ROCK -> "#"
    }
}

data class Point(
    val coordinates: Coordinates,
    var type: Type,
)

class Grid(
    private val minX: Int,
    private val maxX: Int,
    private val maxY: Int,
    private val rows: Map<Int, MutableMap<Int, Point>> = (0..maxY).associateWith { mutableMapOf() },
) {

    private fun isInfiniteWidth() = minX == Int.MIN_VALUE
    fun dump() {
        val minX = rows.values.minOf { it.keys.minOrNull() ?: Int.MAX_VALUE }
        val maxX = rows.values.maxOf { it.keys.maxOrNull() ?: Int.MIN_VALUE }

        (0..maxY).forEach { y ->
            (minX..maxX).forEach { x ->
                print(get(x to y)?.type)
            }
            println()
        }
    }

    fun get(coordinates: Coordinates): Point? {
        val point = rows.get(coordinates.y())?.get(coordinates.x())
        if (point != null)
            return point

        if (coordinates.y() > maxY)
            return null

        if (coordinates.x() !in minX..maxX)
            return null

        if (isInfiniteWidth() && coordinates.y() == maxY)
            return Point(coordinates, Type.ROCK)
                .also { rows[it.coordinates.y()]?.put(it.coordinates.x(), it) }

        return Point(coordinates, Type.AIR)
            .also { rows[it.coordinates.y()]?.put(it.coordinates.x(), it) }
    }

    fun set(c: Coordinates, type: Type) {
        val point = get(c) ?: error("Invalid $c")
        point.type = type
    }
}

class Cave private constructor(
    private val grid: Grid,
) {
    companion object {
        fun create(rockPaths: List<Path>, infinite: Boolean = false): Cave {
            val maxX = rockPaths.maxOf { it.maxOf { it.x() } }
            val maxY = rockPaths.maxOf { it.maxOf { it.y() } }

            val grid = if (infinite) {
                Grid(Int.MIN_VALUE, Int.MAX_VALUE, maxY + 2)
            } else {
                Grid(0, maxX, maxY)
            }

            // mark rocks
            rockPaths.flatMap { it.coordinates() }
                .forEach { c -> grid.set(c, Type.ROCK) }

            return Cave(grid)
        }
    }

    fun dump() = grid.dump()

    fun addSandAt(source: Coordinates): Coordinates? {
        var currentPosition = source

        while (true) {
            val nextPoints = currentPosition.nextPositions()
                .mapNotNull { grid.get(it) }

            // check for overflow
            if (nextPoints.isEmpty()) return null

            val nextNonBlocked = nextPoints.firstOrNull { p -> p.type == Type.AIR }

            if (nextNonBlocked == null) {
                grid.get(currentPosition)?.type = Type.SAND
                return currentPosition
            }

            currentPosition = nextNonBlocked.coordinates
        }
    }
}

private fun Coordinates.nextPositions() = listOf(
    x() to y() + 1, // below
    x() - 1 to y() + 1, // below-left
    x() + 1 to y() + 1, // below-right
)

fun main() {
    val rockPaths = Task(14)
        .readInput()
        .parsePaths()

    // part 1
    Cave.create(rockPaths).apply {
        generateSequence { addSandAt(500 to 0) }
            .takeWhile { it != null }
            .count()
            .also { println(it) }
    }

    // part 2
    Cave.create(rockPaths, infinite = true).apply {
        generateSequence { addSandAt(500 to 0) }
            .takeWhile { it != 500 to 0 }
            .count()
            .also { println(it + 1) }
    }
}

private fun BufferedReader.parsePaths(): List<Path> = readLines()
    .map { it -> it.split(" -> ").map { it.parseCoordinates() } }

private fun String.parseCoordinates(): Coordinates {
    val parts = split(",")
    return parts.first().toInt() to parts.last().toInt()
}
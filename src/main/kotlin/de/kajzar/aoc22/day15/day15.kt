package de.kajzar.aoc22.day15

import de.kajzar.aoc22.Task
import de.kajzar.aoc22.day14.x
import de.kajzar.aoc22.day15.Type.*
import java.io.BufferedReader
import kotlin.math.abs

typealias Coordinates = Pair<Int, Int>

fun Coordinates.x() = first
fun Coordinates.y() = second
fun distance(a: Coordinates, b: Coordinates) = abs(a.x() - b.x()) + abs(a.y() - b.y())

enum class Type(val char: Char) {
    UNKNOWN('.'),
    AIR('*'),
    SENSOR('S'),
    BEACON('B'),
}

data class Input(
    val sensor: Coordinates,
    val closestBeacon: Coordinates,
)

private fun BufferedReader.parseInput(): List<Input> = readLines()
    .map { it ->
        val numbers = "-?(\\d+)".toRegex()
            .findAll(it)
            .toList()
            .map { it.value.toInt() }

        Input(
            numbers[0] to numbers[1],
            numbers[2] to numbers[3],
        )
    }

// can this be rewritten using "effects On"? -> faster?
private fun List<Input>.row(y: Int, rangeX: IntRange = xRange()): List<Type> = rangeX.map { x -> point(x to y) }

private fun List<Input>.point(coordinates: Coordinates): Type {
    if (any { it.sensor == coordinates })
        return SENSOR

    if (any { it.closestBeacon == coordinates })
        return BEACON

    for ((sensor, closestBeacon) in this) {
        val sensorBeaconDistance = distance(sensor, closestBeacon)
        val sensorCoordinates = distance(sensor, coordinates)
        if (sensorCoordinates <= sensorBeaconDistance)
            return AIR
    }

    return UNKNOWN
}

fun List<Input>.xRange(): IntRange {
    val ranges = map { it.xRange() }
    return ranges.minOf { it.first }..ranges.maxOf { it.last }
}

fun Input.xRange(): IntRange {
    val d = distance(sensor, closestBeacon)
    return sensor.x() - d..sensor.x() + d
}

fun Input.effectsOn(targetY: Int): IntRange {
    val y = sensor.y()

    if (y == targetY) return xRange()

    val d = distance(sensor, closestBeacon) // range around the beacon
    val dY = abs(targetY - y)

    if (dY > d) return IntRange.EMPTY

    val dEffect = d - dY
    return (sensor.x() - dEffect)..(sensor.x() + dEffect)
}

private fun Coordinates.tuningFrequency() = 4000000L * x() + y()

private fun List<Input>.searchForUnknown(xRange: IntRange, yRange: IntRange): Coordinates {
    for (y in yRange) {
        // compute effects on row y
        val effects = this.map { it.effectsOn(y) }
            .filter { !it.isEmpty() }

        var nextUncovered = xRange.first
        do {
            val covering = effects.firstOrNull { nextUncovered in it }
            if (covering != null) {
                nextUncovered = covering.last + 1
            }
        } while (covering != null)

        if (nextUncovered in xRange) {
            return nextUncovered to y
        }
    }
    error("No unknown")
}

fun main() {
    val input = Task(15)
        .readInput()
        .parseInput()

    // part 1
    input.row(2000000)
        .count { it !in setOf(BEACON, UNKNOWN) }
        .also { println(it) }

    // part 2
    input.searchForUnknown(
        xRange = 0..4000000,
        yRange = 0..4000000,
    )
        .also {
            println("$it: ${it.tuningFrequency()}")
        }
}


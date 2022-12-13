package de.kajzar.aoc22.day12

import de.kajzar.aoc22.Task
import java.io.BufferedReader

typealias Height = Int

data class Position(val x: Int, val y: Int) {
    override fun toString() = "$x/$y"
}

data class Heightmap(val grid: List<List<Height>>) {
    fun reachable(position: Position): List<Position> {
        return listOf(
            Position(position.x, position.y + 1),
            Position(position.x, position.y - 1),
            Position(position.x + 1, position.y),
            Position(position.x - 1, position.y),
        )
            .filter { (x, y) -> x in grid.indices && y in grid.first().indices }
            .filter { next -> height(next) - height(position) <= 1 }
    }

    private fun height(position: Position) = grid[position.x][position.y]

    fun positions() = sequence {
        for ((x, rows) in grid.withIndex()) {
            for ((y, p) in rows.withIndex()) {
                yield(Position(x, y) to p)
            }
        }
    }
}

data class QueueItem(
    val item: Position,
    val path: List<Position>
)

class Pathfinding(
    val map: Heightmap,
) {
    fun shortestPath(
        start: Position,
        end: Position,
    ): List<Position>? {
        val queue = mutableListOf(QueueItem(start, listOf(start)))
        val visited = mutableSetOf<Position>()

        while (queue.isNotEmpty()) {
            val queueItem = queue.removeFirst()

            val item = queueItem.item
            val path = queueItem.path

            visited.add(item)

            if (item == end)
                return path

            map.reachable(item)
                .filterNot { p -> p in visited }
                .filterNot { p -> queue.any { it.item == p } }
                .forEach { p ->
                    queue.add(QueueItem(p, path.plus(p)))
                }
        }

        return null
    }

}

fun List<Position>.steps() = count() - 1

data class Input(val heightmap: Heightmap, val start: Position, val end: Position)

private fun BufferedReader.parse(): Input {
    var start: Position? = null
    var end: Position? = null

    val heightMapping = ('a'..'z').mapIndexed { i, c -> c to i }
        .toMap()
        .plus('S' to 0)
        .plus('E' to 25)

    val rows = mutableListOf<List<Height>>()
    for ((rowIndex, line) in readLines().withIndex()) {
        val items = mutableListOf<Height>()
        for ((colIndex, c) in line.withIndex()) {
            when (c) {
                'S' -> start = Position(rowIndex, colIndex)
                'E' -> end = Position(rowIndex, colIndex)
            }
            items.add(heightMapping[c] ?: error("No mapping for $c"))
        }
        rows.add(items)
    }

    return Input(
        heightmap = Heightmap(rows),
        start = start ?: error("No start"),
        end = end ?: error("No end"),
    )
}

fun main() {
    val (heightmap, start, end) = Task(12)
        .readInput()
        .parse()

    // part 1
    Pathfinding(heightmap)
        .shortestPath(start, end)
        ?.also { println(it.steps()) }

    // part 2
    heightmap.positions()
        .filter { (_, height) -> height == 0 }
        .minOf { (position, _) ->
            Pathfinding(heightmap)
                .shortestPath(position, end)
                ?.steps() ?: Int.MAX_VALUE
        }
        .also { println("$it") }
}
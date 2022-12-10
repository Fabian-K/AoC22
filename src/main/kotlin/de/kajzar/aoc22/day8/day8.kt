package de.kajzar.aoc22.day8

import de.kajzar.aoc22.Task
import java.io.BufferedReader


data class Tree(
    val x: Int,
    val y: Int,
    val height: Int,
)

data class Grid(
    val rows: List<List<Tree>>
) {

    fun row(i: Int) = rows[i]
    fun column(i: Int) = rows.map { it[i] }
    fun rows() = rows.indices.map { row(it) }
    fun columns() = rows.indices.map { column(it) }
    fun viewpoints() = buildList {
        addAll(rows())
        addAll(rows().map { it.reversed() })
        addAll(columns())
        addAll(columns().map { it.reversed() })
    }

    fun visible() = buildSet {
        for (viewpoint in viewpoints()) {
            addAll(viewpoint.visible())
        }
    }

    fun viewpointsFromTree(tree: Tree) = listOf(
        row(tree.x).filter { it.y > tree.y }, // to right
        row(tree.x).reversed().filter { it.y < tree.y }, // to left
        column(tree.y).filter { it.x > tree.x }, // to bottom
        column(tree.y).reversed().filter { it.x < tree.x }, // to top
    )

    fun trees() = rows.flatten()

    fun scenicScore(tree: Tree): Int {
        return viewpointsFromTree(tree)
            .map { it.visibleFromTreehouse(tree.height).count() }
            .reduce { a, b -> a * b }
    }
}

fun List<Tree>.visible(): List<Tree> {
    val visible = mutableListOf<Tree>()
    for (tree in this) {
        val maxVisible = visible.maxOfOrNull { it.height } ?: -1
        if (tree.height > maxVisible) {
            visible.add(tree)
        }
    }
    return visible
}

fun List<Tree>.visibleFromTreehouse(height: Int) = buildList {
    for (tree in this@visibleFromTreehouse) {
        add(tree)
        if (tree.height >= height)
            return@buildList
    }
}

private fun BufferedReader.parseGrid(): Grid {
    val rows = readLines()
        .mapIndexed { rowIndex, it ->
            it.toCharArray()
                .mapIndexed { colIndex, c ->
                    Tree(rowIndex, colIndex, c.digitToInt())
                }
        }
        .toList()
    return Grid(rows)
}

fun main() {
    val grid = Task(8)
        .readInput()
        .parseGrid()

    // part 1
    grid.visible().count()
        .also { println(it) }

    // part 2
    val bestTree = grid.trees()
        .maxBy { grid.scenicScore(it) }

    println("$bestTree with " + grid.scenicScore(bestTree))
}
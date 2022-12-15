package de.kajzar.aoc22.day13

import de.kajzar.aoc22.Task
import java.io.BufferedReader
import java.util.*

enum class ComparisonResult {
    RIGHT, WRONG, UNKNOWN
}

fun compare(left: Entry, right: Entry): ComparisonResult {
    if (left is IntegerEntry && right is IntegerEntry)
        return compare(left, right)

    if (left is ListEntry && right is ListEntry)
        return compare(left, right)

    if (left is IntegerEntry && right is ListEntry)
        return compare(ListEntry(mutableListOf(left)), right)

    if (left is ListEntry && right is IntegerEntry)
        return compare(left, ListEntry(mutableListOf(right)))

    error("Unexpected entries: $left/$right")
}

fun compare(left: IntegerEntry, right: IntegerEntry): ComparisonResult {
    if (left.value < right.value) return ComparisonResult.RIGHT
    if (left.value > right.value) return ComparisonResult.WRONG
    return ComparisonResult.UNKNOWN
}

fun compare(leftList: ListEntry, rightList: ListEntry): ComparisonResult {
    for (l in leftList.entries.indices) {
        val left = leftList.entries[l]
        val right = rightList.entries.getOrNull(l) ?: return ComparisonResult.WRONG /* right out of items */
        val result = compare(left, right)
        if (result != ComparisonResult.UNKNOWN)
            return result
    }

    // left out of items
    if (leftList.entries.count() < rightList.entries.count())
        return ComparisonResult.RIGHT

    return ComparisonResult.UNKNOWN
}

sealed interface Entry

data class ListEntry(val entries: MutableList<Entry> = mutableListOf()) : Entry {
    override fun toString(): String = "$entries"
}

data class IntegerEntry(val value: Int) : Entry {
    override fun toString(): String = "$value"
}

data class PacketPair(val index: Int, val pair: Pair<ListEntry, ListEntry>) {
    fun isInCorrectOrder(): Boolean {
        val (a, b) = pair
        return compare(a, b) == ComparisonResult.RIGHT
    }
}

private fun BufferedReader.parseSignal(): List<PacketPair> {
    return readLines()
        .sublistsByEmptyLine()
        .mapIndexed { i, list ->
            PacketPair(
                index = i + 1,
                pair = list.first().toEntry() to list.last().toEntry()
            )
        }
}

private fun String.toEntry(): ListEntry {
    val stack = Stack<ListEntry>()

    for (c in itemized()) {
        when (c) {
            // new list
            "[" -> stack.add(ListEntry())
            // end top of stack
            "]" -> {
                // last level -> return result
                if (stack.count() == 1) return stack.single()

                // add top of stack as item to stack entry below
                val next = stack.pop()
                stack.peek().entries.add(next)
            }
            // add item
            else -> stack.peek().entries.add(IntegerEntry(c.toInt()))
        }
    }
    error("invalid input")
}

private fun String.itemized(): List<String> = buildList {
    var nextItem = StringBuilder()

    fun addNextItemIfAvailable() {
        if (nextItem.isNotEmpty())
            add(nextItem.toString())
        nextItem = StringBuilder()
    }

    for (c in this@itemized) {
        when (c) {
            '[', ']' -> {
                addNextItemIfAvailable()
                add(c.toString())
            }

            ',' -> addNextItemIfAvailable()
            else -> nextItem.append(c)
        }
    }
}

private fun List<String>.sublistsByEmptyLine(): List<List<String>> {
    val result = mutableListOf<List<String>>()

    val next = mutableListOf<String>()
    for (line in this) {
        if (line.isBlank()) {
            result.add(next.toList())
            next.clear()
        } else {
            next.add(line)
        }
    }
    if (next.isNotEmpty())
        result.add(next.toList())

    return result
}

private fun <E> MutableList<E>.swap(a: Int, b: Int) {
    val tmp = this[a]
    this[a] = this[b]
    this[b] = tmp
}

private fun MutableList<ListEntry>.sorted() = apply {
    do {
        var changed = false
        this.windowed(2)
            .forEachIndexed { index, it ->
                if (compare(it.first(), it.last()) == ComparisonResult.WRONG) {
                    this.swap(index, index + 1)
                    changed = true
                    return@forEachIndexed
                }
            }
    } while (changed)
}

fun main() {

    // part 1
    Task(13)
        .readInput()
        .parseSignal()
        .filter { it.isInCorrectOrder() }
        .sumOf { it.index }
        .also { println(it) }

    // part 2
    val dividerPackets = listOf("[[2]]", "[[6]]")
        .map { it.toEntry() }

    val sortedPackets = Task(13)
        .readInput()
        .parseSignal()
        .flatMap { it.pair.toList() }
        .plus(dividerPackets)
        .toMutableList()
        .sorted()

    dividerPackets.map { sortedPackets.indexOf(it) + 1 }
        .reduce { a, b -> a * b }
        .also { println(it) }
}
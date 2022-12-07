package de.kajzar.aoc22.day3

import de.kajzar.aoc22.Task
import java.io.BufferedReader

data class Rucksack(
    val compartments: List<Compartment>,
) {

    fun distinctItems() = compartments.flatMap { it.distinctItems() }.toSet()

    fun distinctItemsInAllCompartments(): Set<Item> {
        return compartments.map { it.distinctItems() }
            .reduce { a, b -> a.intersect(b) }
    }
}

data class Compartment(
    val content: List<Item>,
) {
    fun distinctItems() = content.toSet()
}

typealias Item = Char

fun Item.priority() = (('a'..'z') + ('A'..'Z')).indexOf(this) + 1

data class Group(
    val members: List<Rucksack>
) {
    fun badgeItem(): Item {
        return members.map { it.distinctItems() }
            .reduce { a, b -> a.intersect(b) }
            .single()
    }
}

private fun BufferedReader.parseRucksacks(): Sequence<Rucksack> {
    return lineSequence()
        .map { content ->
            val items = content.toCharArray().asList()
            val compartments = items.chunked(items.size / 2)
                .map { Compartment(it) }
            Rucksack(compartments)
        }
}

private fun BufferedReader.parseGroups() = parseRucksacks()
    .chunked(3)
    .map { Group(it) }

fun main() {
    // part 1
    Task(3)
        .readInput()
        .parseRucksacks()
        .sumOf { rucksack -> rucksack.distinctItemsInAllCompartments().sumOf { it.priority() } }
        .also { println("Sum: $it") }

    // part 2
    Task(3)
        .readInput()
        .parseGroups()
        .sumOf { group -> group.badgeItem().priority() }
        .also { println("Sum: $it") }
}
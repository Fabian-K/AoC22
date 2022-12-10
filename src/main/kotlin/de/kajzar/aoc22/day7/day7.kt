package de.kajzar.aoc22.day7

import de.kajzar.aoc22.Task
import java.io.BufferedReader

sealed interface Command
object LS : Command {
    override fun toString() = "LS"

    fun directories(output: List<String>): List<FolderName> {
        return output.filter { it.isDir() }
            .map { it.removePrefix("dir ") }
    }

    fun files(output: List<String>): List<File> {
        return output.filter { !it.isDir() }
            .map { it.parseFile() }
    }

    private fun String.isDir() = startsWith("dir ")

    private fun String.parseFile(): File {
        val parts = split(" ")
        return File(parts[1], parts[0].toLong())
    }

}

data class CD(val target: String) : Command

data class File(val name: String, val size: Long)

typealias FolderName = String

data class Folder(
    val parent: Folder?,
    val name: FolderName?,
    val files: MutableList<File> = mutableListOf(),
    val folders: MutableList<Folder> = mutableListOf(),
) {
    override fun toString() = "/" + pathSegments().joinToString("/")
    private fun pathSegments(): List<String> = buildList {
        parent?.pathSegments()?.let { addAll(it) }
        name?.let { add(it) }
    }

    fun size(): Long = files.sumOf { it.size } +
            folders.sumOf { it.size() }

    fun folders(): List<Folder> = buildList {
        add(this@Folder)
        addAll(folders.flatMap { it.folders() })
    }
}

class Replay(
    private val filesystem: Folder = Folder(
        parent = null,
        name = null,
    ),
    private var context: Folder? = filesystem,
) {
    fun reconstructFilesystem(history: List<CommandExecution>): Folder {
        history.forEach { replay(it) }
        return filesystem
    }

    private fun replay(cmd: CommandExecution) {
        when (cmd.command) {
            is CD -> {
                context = when (cmd.command.target) {
                    "/" -> filesystem
                    ".." -> context?.parent ?: error("No parent")
                    else -> context?.folders?.single { it.name == cmd.command.target }
                }
            }

            is LS -> {
                // populate directories & files
                val directories = cmd.command.directories(cmd.output)
                val files = cmd.command.files(cmd.output)

                context?.folders?.addAll(directories.map {
                    Folder(
                        parent = context!!,
                        name = it,
                    )
                })
                context?.files?.addAll(files)
            }
        }
    }
}

fun String.parseCommand(): Command {
    val cmd = removePrefix("$ ")
    return when {
        cmd.startsWith("cd") -> CD(cmd.removePrefix("cd "))
        cmd.startsWith("ls") -> LS

        else -> error("Failed to parse $this")
    }
}

data class CommandExecution(
    val command: Command,
    val output: List<String>,
)

private fun BufferedReader.readCommandHistory() = buildList {
    var command: Command? = null
    var output = mutableListOf<String>()

    lineSequence().forEach { line ->
        if (line.startsWith('$')) {
            // add previous command
            command?.let { add(CommandExecution(it, output)) }

            // start new
            command = line.parseCommand()
            output = mutableListOf()
        } else {
            output.add(line)
        }
    }

    // add last
    command?.let { add(CommandExecution(it, output)) }
}

fun main() {
    // part 1
    val commandHistory = Task(7)
        .readInput()
        .readCommandHistory()

    val filesystem = Replay()
        .reconstructFilesystem(commandHistory)

    // part 1
    filesystem.folders()
        .filter { it.size() <= 100_000 }
        .sumOf { it.size() }
        .also { println(it) }

    // part 2
    val currentlyFree = 70_000_000 - filesystem.size()
    val toClear = 30_000_000 - currentlyFree

    filesystem.folders()
        .filter { it.size() >= toClear }
        .minBy { it.size() }
        .also { println(it.size()) }
}
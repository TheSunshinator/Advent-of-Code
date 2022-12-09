package day07.standardlibrary

import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("07", "test_input")
    val realInput = readInput("07", "input")

    val testFileSystem = buildFileSystem(testInput)
    val realFileSystem = buildFileSystem(realInput)

    testFileSystem.asSequence()
        .filter { it.size <= 100000 }
        .sumOf { it.size }
        .also(::println) shouldBe 95437

    realFileSystem.asSequence()
        .filter { it.size <= 100000 }
        .sumOf { it.size }
        .let(::println)

    val testSpaceToFree = testFileSystem.size - 40_000_000
    testFileSystem.asSequence()
        .sortedBy { it.size }
        .first { it.size >= testSpaceToFree }
        .size
        .also(::println) shouldBe 24933642

    val realSpaceToFree = realFileSystem.size - 40_000_000
    realFileSystem.asSequence()
        .sortedBy { it.size }
        .first { it.size >= realSpaceToFree }
        .size
        .let(::println)
}

private fun buildFileSystem(commands: List<String>) = Folder("/", null).apply {
    commands.fold(this) { currentFolder, command ->
        if (command == "$ ls") currentFolder
        else changeDirectoryRegex.matchEntire(command)?.groupValues?.get(1)?.let { directory ->
            when (directory) {
                "/" -> this
                ".." -> currentFolder.parent!!
                else -> Folder(directory, currentFolder)
                    .also { currentFolder + it }
            }
        } ?: directoryListRegex.matchEntire(command)?.groupValues?.get(1)?.let { name ->
            currentFolder + Folder(name, currentFolder)
        } ?: fileListRegex.matchEntire(command)!!.groupValues.let { (_, size, name) ->
            currentFolder + File(name, size.toLong())
        }
    }
}

private val changeDirectoryRegex = "\\A\\$ cd (.+)\\z".toRegex()
private val directoryListRegex = "\\Adir (.+)\\z".toRegex()
private val fileListRegex = "\\A(\\d+) (.+)\\z".toRegex()

private sealed interface System {
    val name: String
    val size: Long
}

private class File(
    override val name: String,
    override val size: Long,
): System {
    override fun equals(other: Any?): Boolean = other is File && other.name == name
    override fun hashCode(): Int = name.hashCode()
}

private class Folder(
    override val name: String,
    val parent: Folder?,
): System, Iterable<System> {
    val content = mutableSetOf<System>()
    override val size: Long by lazy { content.sumOf { it.size } }
    operator fun plus(part: System): Folder = apply { content.add(part) }
    override fun iterator() = iterator {
        yield(this@Folder)
        yieldAll(
            content.flatMap { system -> system.let { it as? Folder }?.asSequence().orEmpty() }
        )
    }
    override fun equals(other: Any?): Boolean = other is File && other.name == name
    override fun hashCode(): Int = name.hashCode()
}
package day07.arrow

import arrow.core.foldMap
import arrow.core.toOption
import arrow.optics.Fold
import arrow.optics.Getter
import arrow.optics.Optional
import arrow.optics.optics
import arrow.typeclasses.Monoid
import io.kotest.matchers.shouldBe
import java.lang.Long.min
import utils.readInput

fun main() {
    val testInput = readInput("07", "test_input")
    val realInput = readInput("07", "input")

    val testFileSystem = buildFileSystem(testInput)
    val realFileSystem = buildFileSystem(realInput)

    Fold.system().folder.size
        .filter { it <= 100000 }
        .fold(Monoid.long(), testFileSystem)
        .also(::println) shouldBe 95437

    Fold.system().folder.size
        .filter { it <= 100000 }
        .fold(Monoid.long(), realFileSystem)
        .let(::println)

    val testSpaceToFree = testFileSystem.size - 40_000_000
    Fold.system().folder.size
        .filter { it >= testSpaceToFree }
        .fold(Monoid.min, testFileSystem)
        .also(::println) shouldBe 24933642

    val realSpaceToFree = realFileSystem.size - 40_000_000
    Fold.system().folder.size
        .filter { it >= realSpaceToFree }
        .fold(Monoid.min, realFileSystem)
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

@optics
sealed interface System {
    val name: String
    val size: Long

    companion object
}

@optics
data class File(
    override val name: String,
    override val size: Long,
) : System {
    override fun equals(other: Any?): Boolean = other is File && other.name == name
    override fun hashCode(): Int = name.hashCode()

    companion object
}

@optics
data class Folder(
    override val name: String,
    val parent: Folder?,
) : System, Iterable<System> {
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

    companion object
}

private fun Fold.Companion.system(): Fold<System, System> = object : Fold<System, System> {
    override fun <R> foldMap(M: Monoid<R>, source: System, map: (focus: System) -> R): R = when (source) {
        is File -> map(source)
        is Folder -> source.asSequence().foldMap(M, map)
    }
}

private inline val Fold<System, Folder>.size: Fold<System, Long>
    inline get() = plus(Getter { it.size })

private inline fun Fold<System, Long>.filter(crossinline predicate: (Long) -> Boolean) = plus(
    Optional(
        getOption = { source -> source.takeIf(predicate).toOption() },
        set = { _, focus -> focus }
    )
)

private inline val Monoid.Companion.min: Monoid<Long>
    get() = object : Monoid<Long> {
        override fun empty(): Long = Long.MAX_VALUE
        override fun Long.combine(b: Long): Long = min(this, b)
    }

package year2022.day05

import arrow.core.identity
import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("05", "test_input")
    val realInput = readInput("05", "input")

    val testStart = testInput.takeWhile(String::isNotEmpty)
    val testMovements = testInput.takeLastWhile(String::isNotEmpty)
        .let(::parseMovements)

    val realStart = realInput.takeWhile(String::isNotEmpty)
    val realMovements = realInput.takeLastWhile(String::isNotEmpty)
        .let(::parseMovements)

    testStart.toStackList()
        .applyMovements(testMovements, List<Char>::asReversed)
        .joinToString(separator = "") { it.last().toString() }
        .also(::println) shouldBe "CMZ"
    realStart.toStackList()
        .applyMovements(realMovements, List<Char>::asReversed)
        .joinToString(separator = "") { it.last().toString() }
        .let(::println)

    testStart.toStackList()
        .applyMovements(testMovements)
        .joinToString(separator = "") { it.last().toString() }
        .also(::println) shouldBe "MCD"
    realStart.toStackList()
        .applyMovements(realMovements)
        .joinToString(separator = "") { it.last().toString() }
        .let(::println)
}

private fun List<String>.toStackList(): List<MutableList<Char>> = buildList {
    val nthStackSequence = 1..this@toStackList.maxOf { it.length } step 4
    val stackSequence = this@toStackList.indices.reversed().asSequence().drop(1)

    nthStackSequence.mapTo(this) { stackIndex ->
        stackSequence.map { this@toStackList[it].getOrNull(stackIndex) }
            .takeWhile{ it != ' ' }
            .filterNotNull()
            .toMutableList()
    }
}

private val movementRegex = "\\Amove (\\d+) from (\\d+) to (\\d+)\\z".toRegex()

private fun parseMovements(input: List<String>): Sequence<Movement> {
    return input.asSequence()
        .mapNotNull { movementRegex.find(it) }
        .map {
            val (_, quantity, start, end) = it.groupValues
            Movement(start.toInt() - 1, end.toInt() - 1, quantity.toInt())
        }
}

private fun List<MutableList<Char>>.applyMovements(
    movements: Sequence<Movement>,
    transform: List<Char>.() -> List<Char> = ::identity,
) = movements.fold(this) { cargo, movement ->
    val movingItems = cargo[movement.start].run { subList(size - movement.quantity, size) }
    cargo[movement.end].addAll(movingItems.transform())
    movingItems.clear()
    cargo
}

data class Movement(
    val start: Int,
    val end: Int,
    val quantity: Int,
)
package year2022.day12

import io.kotest.matchers.shouldBe
import utils.MapDetails
import utils.detailsSequence
import utils.findShortestRoute
import utils.readInput

fun main() {
    val testInput = readInput("12", "test_input").map(String::toList)
    val realInput = readInput("12", "input").map(String::toList)

    findShortestRoute(
        map = testInput,
        start = testInput.detailsSequence().first { it.value == 'S' }.position,
        end = testInput.detailsSequence().first { it.value == 'E' }.position,
        movementCost = ::movementCost,
    )!!.cost
        .also(::println) shouldBe 31

    findShortestRoute(
        map = realInput,
        start = realInput.detailsSequence().first { it.value == 'S' }.position,
        end = realInput.detailsSequence().first { it.value == 'E' }.position,
        movementCost = ::movementCost,
    )!!.cost
        .let(::println)

    testInput.detailsSequence()
        .filter { it.value.adjustedValue == 'a' }
        .map { start ->
            findShortestRoute(
                map = testInput,
                start = start.position,
                end = testInput.detailsSequence().first { it.value == 'E' }.position,
                movementCost = ::movementCost,
            )?.cost
        }
        .filterNotNull()
        .min()
        .also(::println) shouldBe 29

    realInput.detailsSequence()
        .filter { it.value.adjustedValue == 'a' }
        .map { start ->
            findShortestRoute(
                map = realInput,
                start = start.position,
                end = realInput.detailsSequence().first { it.value == 'E' }.position,
                movementCost = ::movementCost,
            )?.cost
        }
        .filterNotNull()
        .min()
        .let(::println)
}

private fun movementCost(start: MapDetails<Char>, end: MapDetails<Char>): Int? {
    val startValue = start.value.adjustedValue
    val endValue = end.value.adjustedValue
    return when {
        endValue - startValue <= 1 -> 1
        else -> null
    }
}

private val Char.adjustedValue
    get() = when (this) {
        'E' -> 'z'
        'S' -> 'a'
        else -> this
    }
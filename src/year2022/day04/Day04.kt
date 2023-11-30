package year2022.day04

import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("04", "test_input")
    val realInput = readInput("04", "input")

    parseInput(testInput)
        .count { (firstRange, secondRange) -> fullyContains(firstRange, secondRange) }
        .also(::println) shouldBe 2

    parseInput(realInput)
        .count { (firstRange, secondRange) -> fullyContains(firstRange, secondRange) }
        .let(::println)

    parseInput(testInput)
        .count { (firstRange, secondRange) -> partiallyContains(firstRange, secondRange) }
        .also(::println) shouldBe 4

    parseInput(realInput)
        .count { (firstRange, secondRange) -> partiallyContains(firstRange, secondRange) }
        .let(::println)
}

private val parsingRegex = "\\A(\\d+)-(\\d+),(\\d+)-(\\d+)\\z".toRegex()
private fun parseInput(input: List<String>): Sequence<Pair<IntRange, IntRange>> {
    return input.asSequence()
        .mapNotNull { parsingRegex.matchEntire(it) }
        .map { matchResult ->
            val (_, firstStart, firstEnd, secondStart, secondEnd) = matchResult.groupValues
            firstStart.toInt()..firstEnd.toInt() to secondStart.toInt()..secondEnd.toInt()
        }
}

private fun partiallyContains(firstRange: IntRange, secondRange: IntRange): Boolean {
    return firstRange.first in secondRange
        || firstRange.last in secondRange
        || secondRange.first in firstRange
        || secondRange.last in firstRange
}

private fun fullyContains(firstRange: IntRange, secondRange: IntRange): Boolean {
    return (secondRange.first <= firstRange.first && firstRange.last <= secondRange.last)
        || (firstRange.first <= secondRange.first && secondRange.last <= firstRange.last)
}

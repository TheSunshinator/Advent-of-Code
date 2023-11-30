package year2022.day01

import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("01", "test_input")
    val realInput = readInput("01", "input")

    val testCaloriesPerElf = testInput.caloriesPerElf()
    val realCaloriesPerElf = realInput.caloriesPerElf()

    testCaloriesPerElf.max()
        .also(::println) shouldBe 24000

    realCaloriesPerElf.max()
        .let(::println)

    testCaloriesPerElf.sortedDescending()
        .asSequence()
        .take(3)
        .sum()
        .also(::println) shouldBe 45000

    realCaloriesPerElf.sortedDescending()
        .asSequence()
        .take(3)
        .sum()
        .let(::println)
}

private fun List<String>.caloriesPerElf(): List<Long> {
    return asSequence()
        .map(String::toLongOrNull)
        .fold(mutableListOf(0L)) { accumulator, number ->
            if (number == null) accumulator.add(0)
            else accumulator[accumulator.lastIndex] += number
            accumulator
        }
}
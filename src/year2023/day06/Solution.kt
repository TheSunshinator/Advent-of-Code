package year2023.day06

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.product
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 6)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(288),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(71503),
            algorithm = ::part2,
        ),
    )
}

private fun part1(input: List<String>): Long {
    return parseMultipleRaces(input)
        .asSequence()
        .map { (time, highScore) ->
            val wins = (time / 2 downTo 1).asSequence()
                .map { timeHeld -> (time - timeHeld) * timeHeld }
                .takeWhile { it > highScore }
                .count() * 2
            if (time % 2L == 0L) wins - 1 else wins
        }
        .product()
}

private fun parseMultipleRaces(input: List<String>): List<Pair<Long, Long>> {
    val (time, distance) = input
    return time.toIntSequence()
        .zip(distance.toIntSequence())
        .toList()
}

private fun String.toIntSequence() = splitToSequence(" +".toRegex()).drop(1).map { it.toLong() }

private fun part2(input: List<String>): Int {
    val (time, highScore) = parseSingleRace(input)
    val wins = (time / 2 downTo 1).asSequence()
        .map { timeHeld -> (time - timeHeld) * timeHeld }
        .takeWhile { it > highScore }
        .count() * 2
    return if (time % 2L == 0L) wins - 1 else wins
}

private fun parseSingleRace(input: List<String>) = Pair(
    trimInput(input[0]).toLong(),
    trimInput(input[1]).toLong(),
)

private fun trimInput(input: String) = input.drop(9).replace(" +".toRegex(), "")

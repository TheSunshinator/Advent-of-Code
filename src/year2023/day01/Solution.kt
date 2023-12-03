package year2023.day01

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 1, "test_input_part2")

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(142, 209),
            algorithm = ::solutionPart1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(142, 281),
            algorithm = ::solutionPart2,
        ),
    )
}

private fun solutionPart1(input: List<String>): Long {
    return input.asSequence()
        .map(numberRegex::findAll)
        .map { matches -> matches.map { it.value.toInt() }.toList() }
        .filter { it.isNotEmpty() }
        .map { "${it.first()}${it.last()}".toLong() }
        .sum()
}

private val numberRegex = "\\d".toRegex()

private fun solutionPart2(input: List<String>) = input.sumOf {
    val first = firstNumberDigitOrLetterRegex.find(it)?.groupValues?.get(1) ?: return@sumOf 0
    val last = lastNumberDigitOrLetterRegex.find(it)?.groupValues?.get(1) ?: return@sumOf 0
    "${first.toNumber()}${last.toNumber()}".toLong()
}

private val firstNumberDigitOrLetterRegex = ".*?(\\d|one|two|three|four|five|six|seven|eight|nine).*".toRegex()
private val lastNumberDigitOrLetterRegex = ".*(\\d|one|two|three|four|five|six|seven|eight|nine).*".toRegex()

private fun String.toNumber() = toIntOrNull() ?: when (this) {
    "one" -> 1
    "two" -> 2
    "three" -> 3
    "four" -> 4
    "five" -> 5
    "six" -> 6
    "seven" -> 7
    "eight" -> 8
    else -> 9
}

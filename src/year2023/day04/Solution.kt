package year2023.day04

import arrow.core.nonEmptyListOf
import kotlin.math.pow
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 4, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(13),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(30),
            algorithm = ::part2,
        ),
    )
}

private fun parse(input: List<String>): List<Int> = input.map { line ->
    winningNumberRegex.findAll(line).count()
}

private val winningNumberRegex = "(?<= )(\\d+)(?= .*\\|.* \\1(?!\\d))".toRegex()

private fun part1(input: List<Int>): Long {
    return input.sumOf { if (it == 0) 0L else 2.0.pow(it - 1).toLong() }
}

private fun part2(input: List<Int>): Long {
    val counts = MutableList(input.size) { 1L }

    input.forEachIndexed { index, winningNumberCount ->
        (1..winningNumberCount).asSequence()
            .map { it + index }
            .forEach { indexToAddCopies ->
                counts[indexToAddCopies] += counts[index]
            }
    }

    return counts.reduce(Long::plus)
}

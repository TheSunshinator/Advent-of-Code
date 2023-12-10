package year2023.day09

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 9, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(114),
            algorithm = getSolution(::nextNumber),
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(2),
            algorithm = getSolution(::previousNumber),
        ),
    )
}

private fun parse(input: List<String>): List<List<Long>> = input.map { line ->
    line.splitToSequence(' ').map { it.toLong() }.toList()
}

private fun getSolution(computeNumber: (List<Long>) -> Long): (List<List<Long>>) -> Long = { input ->
    input.asSequence()
        .map(computeNumber)
        .sum()
}

private fun nextNumber(numberSequence: List<Long>) = computeSteps(numberSequence).sumOf { it.last() }

private fun computeSteps(numberSequence: List<Long>): List<List<Long>> {
    return generateSequence(numberSequence) { previousSequence ->
        previousSequence.zipWithNext { a, b -> b - a }
    }
        .takeWhile { sequence -> sequence.any { it != 0L } }
        .toList()
        .asReversed()
}

private fun previousNumber(numberSequence: List<Long>): Long {
    return computeSteps(numberSequence).asSequence()
        .map { it.first() }
        .fold(0L) { nextLinePreviousNumber, currentFirstNumber -> currentFirstNumber - nextLinePreviousNumber }
}

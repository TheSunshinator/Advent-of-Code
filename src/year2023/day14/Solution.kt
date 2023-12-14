package year2023.day14

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.findCycle
import utils.readInputs
import utils.runAlgorithm
import utils.transpose

fun main() {
    val (realInput, testInputs) = readInputs(2023, 14)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(136),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(64),
            algorithm = ::part2,
        ),
    )
}

private fun part1(input: List<String>): Long {
    return input.transpose().asSequence()
        .flatMap(rockRegex::findAll)
        .sumOf { rockPositions ->
            val rockCount = rockPositions.value.count { it == 'O' }
            rockPositions.range.asSequence()
                .take(rockCount)
                .sumOf { input.first().length - it }
                .toLong()
        }
}

private val rockRegex = "(?<=\\A|#)[^#]*?O[^#]*?(?=\\z|#)".toRegex()

private fun part2(input: List<String>): Long {
    val cycle = generateSequence(input, ::executeCycle).findCycle()
    val endStateIndex = (1000_000_000 - cycle.loopStart) % cycle.loopSize + cycle.loopStart
    return cycle.steps[endStateIndex].northLoad()
}

private fun executeCycle(startState: List<String>): List<String> {
    return startState.transpose()
        .moveAllRocks(left = true)
        .transpose()
        .moveAllRocks(left = true)
        .transpose()
        .moveAllRocks(left = false)
        .transpose()
        .moveAllRocks(left = false)
}

private fun List<String>.moveAllRocks(left: Boolean): List<String> = map { line ->
    line.splitToSequence(rockZoneRegex).joinToString(separator = "") { zone ->
        zone.asSequence()
            .sortedWith { a, b ->
                when {
                    a == 'O' -> 1
                    b == 'O' -> -1
                    else -> 0
                }
            }
            .joinToString(separator = "")
            .let { if (left) it.reversed() else it }
    }
}

private fun List<String>.northLoad(): Long {
    return asReversed()
        .asSequence()
        .withIndex()
        .sumOf { (index, line) ->
            (index + 1) * line.count { it == 'O' }.toLong()
        }
}

private val rockZoneRegex = "(?<=#)|(?=#)".toRegex()

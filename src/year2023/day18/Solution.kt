package year2023.day18

import arrow.core.nonEmptyListOf
import utils.Direction
import utils.Point
import utils.ProblemPart
import utils.applyPickTheorem
import utils.move
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 18)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(62),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(952408144115),
            algorithm = ::part2,
        ),
    )
}
private fun part1(input: List<String>): Long {
    return parsePart1(input).fold(mutableListOf(Point())) { points, movement ->
        generateSequence(points.last()) { it.move(movement.direction) }
            .take(movement.steps + 1)
            .drop(1)
            .let(points::addAll)
        points
    }.applyPickTheorem()
}

private fun parsePart1(input: List<String>): List<Input> {
    return input.asSequence()
        .mapNotNull(parsingRegex1::matchEntire)
        .map { match ->
            Input(
                steps = match.groupValues[2].toInt(),
                direction = when (match.groupValues[1]) {
                    "R" -> Direction.Right
                    "L" -> Direction.Left
                    "U" -> Direction.Up
                    else -> Direction.Down
                },
            )
        }
        .toList()
}

private val parsingRegex1 = "([A-Z]) (\\d+) .*".toRegex()

private fun part2(input: List<String>): Long {
    return parsePart2(input).fold(mutableListOf(Point())) { points, movement ->
        generateSequence(points.last()) { it.move(movement.direction) }
            .take(movement.steps + 1)
            .drop(1)
            .let(points::addAll)
        points
    }.applyPickTheorem()
}

private fun parsePart2(input: List<String>): List<Input> {
    return input.asSequence()
        .mapNotNull(parsingRegex2::matchEntire)
        .map { match ->
            Input(
                steps = match.groupValues[1].toInt(16),
                direction = when (match.groupValues[2]) {
                    "0" -> Direction.Right
                    "2" -> Direction.Left
                    "3" -> Direction.Up
                    else -> Direction.Down
                },
            )
        }
        .toList()
}

private val parsingRegex2 = ".* \\(#(\\p{XDigit}{5})(\\d)\\)".toRegex()

private data class Input(
    val steps: Int,
    val direction: Direction,
)

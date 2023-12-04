package year2023.day03

import arrow.core.nonEmptyListOf
import utils.Point
import utils.ProblemPart
import utils.neighbors
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 3, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(4361),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(467835),
            algorithm = ::part2,
        ),
    )
}

private fun parse(input: List<String>): Input {
    return input.asSequence()
        .map(parsingRegex::findAll)
        .flatMapIndexed { row, elements ->
            elements.map { match ->
                match.value.toLongOrNull()
                    ?.let { Number(row, match.range, it) }
                    ?: Symbol(match.value.first(), Point(row, match.range.first))
            }
        }
        .partition { it is Number }
        .let { (numbers, symbols) ->
            Input(
                numbers = numbers as List<Number>,
                symbols = symbols as List<Symbol>,
            )
        }
}

private val parsingRegex = "(\\d+|[^\\d.])".toRegex()

private fun part1(input: Input): Long {
    return input.symbols.asSequence()
        .flatMap { symbol ->
            symbol.position.adjacentNumbers(input.numbers)
        }
        .distinct()
        .sumOf { it.value }
}

private fun Point.adjacentNumbers(numbers: List<Number>): Sequence<Number> {
    return neighbors(includeDiagonal = true)
        .mapNotNull { coordinates ->
            numbers.find { it.row == coordinates.x && coordinates.y in it.columns }
        }
        .distinct()
}

private fun part2(input: Input): Long {
    return input.symbols.asSequence()
        .filter { it.value == '*' }
        .map { symbol ->
            symbol.position
                .adjacentNumbers(input.numbers)
                .map { it.value }
                .toList()
        }
        .filter { it.size == 2 }
        .map { it.reduce(Long::times) }
        .sum()
}

private data class Input(
    val numbers: List<Number>,
    val symbols: List<Symbol>,
)

private data class Number(
    val row: Int,
    val columns: IntRange,
    val value: Long,
)

private data class Symbol(
    val value: Char,
    val position: Point,
)

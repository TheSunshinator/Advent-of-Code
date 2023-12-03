package year2023.day02

import arrow.core.nonEmptyListOf
import kotlin.math.max
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 2, transform = ::parseInput)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(8),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(2286),
            algorithm = ::part2,
        ),
    )
}

private fun parseInput(input: List<String>): List<Match> = input.mapIndexed { index, match ->
    Match(
        number = index + 1,
        games = match.replace(prefixRegex, "")
            .splitToSequence(';')
            .map(colorResultRegex::findAll)
            .map { games ->
                games.associate { it.groupValues[2] to it.groupValues[1].toInt() }
            }
            .toList()
    )
}

private val prefixRegex = "Game \\d+: ".toRegex()
private val colorResultRegex = "(\\d+) (red|green|blue)".toRegex()

private fun part1(input: List<Match>): Int {
    return input.asSequence()
        .filterNot { match ->
            match.games.any {
                it.getOrDefault("red", 0) > 12
                    || it.getOrDefault("green", 0) > 13
                    || it.getOrDefault("blue", 0) > 14
            }
        }
        .sumOf { it.number }
}

private fun part2(input: List<Match>): Long {
    return input.asSequence()
        .map { match ->
            match.games.fold(Triple(0L, 0L, 0L)) { accumulator, game ->
                Triple(
                    max(accumulator.first, game.getOrDefault("red", 0).toLong()),
                    max(accumulator.second, game.getOrDefault("green", 0).toLong()),
                    max(accumulator.third, game.getOrDefault("blue", 0).toLong()),
                )
            }
        }
        .sumOf { it.first * it.second * it.third }
}

data class Match(
    val number: Int,
    val games: List<GameResult>,
)
typealias GameResult = Map<String, Int>

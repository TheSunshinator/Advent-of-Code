package year2023.day11

import arrow.core.nonEmptyListOf
import io.kotest.matchers.shouldBe
import kotlin.math.max
import kotlin.math.min
import utils.Point
import utils.ProblemPart
import utils.combinations
import utils.manhattanDistanceTo
import utils.permutations
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 11, transform = ::parse)

    computeDistances(testInputs.head, 10) shouldBe 1030
    computeDistances(testInputs.head, 100) shouldBe 8410

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(374),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(82000210),
            algorithm = ::part2,
        ),
    )
}

private fun parse(input: List<String>): Universe {
    val galaxies = input.asSequence()
        .flatMapIndexed { y, line -> line.asSequence().mapIndexed { x, char -> char to Point(x, y) } }
        .mapNotNull { (char, coordinates) -> if (char == '#') coordinates else null }
        .toSet()


    return Universe(
        galaxies = galaxies,
        emptyRows = input.indices.minus(galaxies.mapTo(mutableSetOf()) { it.y }).toSet(),
        emptyColumns = input.indices.minus(galaxies.mapTo(mutableSetOf()) { it.x }).toSet(),
    )
}

private data class Universe(
    val galaxies: Set<Point>,
    val emptyRows: Set<Int>,
    val emptyColumns: Set<Int>,
)

private fun part1(input: Universe): Long = computeDistances(input, 2)

private fun computeDistances(input: Universe, expansionRatio: Long): Long {
    return input.galaxies.combinations(2)
        .map { it.iterator() }
        .map(getDistanceMappingFunction(input.emptyRows, input.emptyColumns, expansionRatio))
        .sum()
}

private fun getDistanceMappingFunction(
    emptyRows: Set<Int>,
    emptyColumns: Set<Int>,
    expansionRatio: Long,
): (Iterator<Point>) -> Long = { iterator ->
    val a = iterator.next()
    val b = iterator.next()
    val xRange = min(a.x, b.x)..max(a.x, b.x)
    val yRange = min(a.y, b.y)..max(a.y, b.y)
    (a manhattanDistanceTo b) +
        (emptyColumns.count { it in xRange } + emptyRows.count { it in yRange }) * (expansionRatio - 1)
}

private fun part2(input: Universe): Long = computeDistances(input, 1_000_000)

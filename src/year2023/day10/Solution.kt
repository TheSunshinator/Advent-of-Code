package year2023.day10

import arrow.core.nonEmptyListOf
import io.kotest.matchers.collections.shouldHaveSize
import utils.Point
import utils.ProblemPart
import utils.get
import utils.neighbors
import utils.print
import utils.readInputs
import utils.runAlgorithm
import utils.x
import utils.y

fun main() {
    val (realInput, testInputs) = readInputs(
        2023,
        10,
        "test_input_2",
        "test_input_3",
        "test_input_4",
    )

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(8, 22, 70, 80),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(1, 4, 8, 10),
            algorithm = ::part2,
        ),
    )
}

private fun part1(input: List<String>): Int = computeLoop(input).size / 2

private fun computeLoop(input: List<String>): List<Point> {
    val start = input.asSequence()
        .mapIndexedNotNull { y, row ->
            row.asSequence()
                .mapIndexedNotNull { x, char -> if (char == 'S') x else null }
                .firstOrNull()
                ?.let { Point(it, y) }
        }
        .first()
    val (firstPoint, secondPoint) = start.neighbors()
        .filter { it.x in input.first().indices && it.y in input.indices }
        .filter { neighbor -> start in neighbor.pipeNeighbors(input[neighbor]) }
        .toList()
        .also { it shouldHaveSize 2 }

    return computeNextStepFunction(input)
        .let {
            generateSequence(mutableListOf(start, firstPoint), it)
                .zip(generateSequence(mutableListOf(start, secondPoint), it))
        }
        .takeWhile { (path1, path2) ->
            path1.last() != path2.last() && path1[path1.lastIndex - 1] != path2[path2.lastIndex]
        }
        .last()
        .let { (path1, path2) ->
            path1 + path2.asReversed().asSequence().drop(if (path1.last() == path2.last()) 1 else 2)
        }
}

private fun Point.pipeNeighbors(pipeShape: Char): List<Point> = when (pipeShape) {
    '|' -> listOf(moveUp(this), moveDown(this))
    '-' -> listOf(moveLeft(this), moveRight(this))
    'F' -> listOf(moveRight(this), moveDown(this))
    'J' -> listOf(moveUp(this), moveLeft(this))
    '7' -> listOf(moveLeft(this), moveDown(this))
    'L' -> listOf(moveUp(this), moveRight(this))
    else -> emptyList()
}

private val moveLeft = Point.x.lift(Int::dec)
private val moveRight = Point.x.lift(Int::inc)
private val moveUp = Point.y.lift(Int::dec)
private val moveDown = Point.y.lift(Int::inc)

private fun computeNextStepFunction(input: List<String>): (MutableList<Point>) -> MutableList<Point>? = { path ->
    val newElement = path.last().pipeNeighbors(input[path.last()])
        .single { it != path[path.lastIndex - 1] }

    path.apply { add(newElement) }
}

private fun part2(input: List<String>): Int {
    val loop = computeLoop(input).toSet()

    val standardMap = input.mapIndexed { y, line ->
        line.asSequence()
            .mapIndexed { x, char ->
                when {
                    Point(x, y) !in loop -> '.'
                    char == 'S' -> when {
                        Point(x, y + 1) in loop && Point(x, y - 1) in loop -> '|'
                        Point(x, y + 1) in loop && Point(x + 1, y) in loop -> 'F'
                        Point(x, y + 1) in loop && Point(x - 1, y) in loop -> '7'
                        Point(x, y - 1) in loop && Point(x + 1, y) in loop -> 'L'
                        Point(x, y - 1) in loop && Point(x - 1, y) in loop -> 'J'
                        else -> '-'
                    }
                    else -> char
                }
            }
            .joinToString(separator = "")
    }

    val horizontalInterior = standardMap.asSequence()
        .findInnerPoints(wrappedHorizontalZones)
        .toSet()

    val verticalInterior = standardMap.first().indices.asSequence()
        .map { column -> standardMap.asSequence().map { it[column] }.joinToString(separator = "") }
        .findInnerPoints(wrappedVerticalZones)
        .map { Point(it.y, it.x) }
        .toSet()

    return horizontalInterior.intersect(verticalInterior).size
}

private fun Sequence<String>.findInnerPoints(insideRegex: Regex): Sequence<Point> = flatMapIndexed { y, line ->
    insideRegex.findAll(line)
        .filterIndexed { index, _ -> index % 2 == 0 }
        .flatMap { match ->
            val space = match.groups[1]!!
            space.range.asSequence().mapNotNull { x -> if (line[x] == '.') x else null }
        }
        .map { x -> Point(x, y) }
}

private val wrappedHorizontalZones = "(?:\\||F-*J|L-*7)(.*?)(?=\\||F-*J|L-*7)".toRegex()
private val wrappedVerticalZones = "(?:-|7\\|*L|F\\|*J)(.*?)(?=-|7\\|*L|F\\|*J)".toRegex()

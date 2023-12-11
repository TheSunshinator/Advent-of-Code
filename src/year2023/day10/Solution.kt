package year2023.day10

import arrow.core.nonEmptyListOf
import io.kotest.matchers.collections.shouldHaveSize
import java.util.LinkedList
import utils.Point
import utils.ProblemPart
import utils.get
import utils.neighbors
import utils.readInputs
import utils.runAlgorithm
import utils.x
import utils.y

fun main() {
    val (realInput, testInputs) = readInputs(2023, 10)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(8),
            algorithm = ::part1,
        ),
//        part2 = ProblemPart(
//            expectedResultsForTests = nonEmptyListOf(TODO()),
//            algorithm = { TODO() },
//        ),
    )
}

private fun part1(input: List<String>): Int {
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
            if (path1.last() == path2.last()) path1.size - 1 else path1.size - 2
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

package year2022.day08

import utils.Point
import utils.coordinates
import utils.get
import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("08", "test_input").map { it.toList() }
    val realInput = readInput("08", "input").map { it.toList() }

    allVisibilityPathSequence(testInput)
        .flatMap(::toVisiblePoints)
        .toSet()
        .size
        .also(::println) shouldBe 21

    allVisibilityPathSequence(realInput)
        .flatMap(::toVisiblePoints)
        .toSet()
        .size
        .let(::println)

    scenicScoreSequence(testInput).max()
        .also(::println) shouldBe 8

    scenicScoreSequence(realInput).max()
        .let(::println)
}

private fun allVisibilityPathSequence(input: List<List<Char>>): Sequence<Sequence<Pair<Point, Char>>> = sequence {
    val xCoordinates = input.indices
    val yCoordinates = input.first().indices

    xCoordinates.flatMap { x ->
        sequenceOf(
            yCoordinates.asSequence().map { y -> Point(x, y).withHeight(input) },
            yCoordinates.reversed().asSequence().map { y -> Point(x, y).withHeight(input) },
        )
    }.let { yieldAll(it) }

    yCoordinates.flatMap { y ->
        sequenceOf(
            xCoordinates.asSequence().map { x -> Point(x, y).withHeight(input) },
            xCoordinates.reversed().asSequence().map { x -> Point(x, y).withHeight(input) },
        )
    }.let { yieldAll(it) }
}

private fun toVisiblePoints(path: Sequence<Pair<Point, Char>>): Sequence<Point> {
    return path.fold(mutableListOf<Pair<Point, Char>>()) { visiblePoints, position ->
        val currentHeight = visiblePoints.lastOrNull()?.second
        if (currentHeight == null || position.second > currentHeight) visiblePoints.add(position)
        visiblePoints
    }.asSequence().map { it.first }
}

private fun scenicScoreSequence(input: List<List<Char>>): Sequence<Int> {
    return input.coordinates()
        .filter { it.x != 0 && it.y != 0 }
        .map { scenicScore(it, input) }
}

private fun Point.withHeight(input: List<List<Char>>) = to(input[this])

private fun scenicScore(start: Point, map: List<List<Char>>) : Int {
    return sequenceOf(
        start.x.minus(1).downTo(0).map { map[it][start.y] },
        start.x.plus(1).until(map.size).map { map[it][start.y] },
        start.y.minus(1).downTo(0).map { map[start.x][it] },
        start.y.plus(1).until(map.first().size).map { map[start.x][it] },
    ).map { treeSequence ->
        treeSequence.indexOfFirst { it >= map[start] }
            .takeIf { it >= 0 }
            ?.plus(1)
            ?: treeSequence.size
    }
        .fold(1, Int::times)
}

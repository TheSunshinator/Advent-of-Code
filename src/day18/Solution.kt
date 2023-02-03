package day18

import io.kotest.matchers.shouldBe
import utils.Point3d
import utils.cartesianProduct
import utils.manhattanDistanceTo
import utils.minMaxOf
import utils.neighbors
import utils.readInput

fun main() {
    val testInput = readInput("18", "test_input").mapTo(mutableSetOf(), String::toPoint)
    val realInput = readInput("18", "input").mapTo(mutableSetOf(), String::toPoint)

    countFaces(testInput)
        .also(::println) shouldBe 64

    countFaces(realInput)
        .let(::println)

    filledDroplet(testInput)
        .let(::countFaces)
        .also(::println) shouldBe 58

    filledDroplet(realInput)
        .let(::countFaces)
        .also(::println)
}

private fun String.toPoint(): Point3d {
    return splitToSequence(',')
        .mapTo(mutableListOf(), String::toInt)
        .let { (x, y, z) -> Point3d(x, y, z) }
}

private fun countFaces(input: Iterable<Point3d>): Int {
    return groupings.zip(sortings)
        .flatMap { (grouping, sorting) ->
            val firstSelector = grouping.first
            val secondSelector = grouping.second
            input.groupBy {
                it.firstSelector() to it.secondSelector()
            }
                .values
                .map { it.sortedWith(sorting).zipWithNext() }
        }
        .sumOf { row ->
            row.count { (first, second) ->
                first.manhattanDistanceTo(second) > 1
            }.let { 2 * (it + 1) }
        }
}

private val groupings: Sequence<Pair<Point3d.() -> Int, Point3d.() -> Int>> = sequenceOf(
    Point3d::x to Point3d::y,
    Point3d::x to Point3d::z,
    Point3d::y to Point3d::z,
)

private val sortings = sequenceOf(
    compareBy(Point3d::z),
    compareBy(Point3d::y),
    compareBy(Point3d::x),
)

private fun filledDroplet(input: Set<Point3d>): Set<Point3d> {
    val xRange = input.minMaxOf { it.x }.let { (it.first - 1)..(it.second + 1) }
    val yRange = input.minMaxOf { it.y }.let { (it.first - 1)..(it.second + 1) }
    val zRange = input.minMaxOf { it.z }.let { (it.first - 1)..(it.second + 1) }
    val candidates = (xRange cartesianProduct yRange cartesianProduct zRange.asSequence())
        .mapTo(mutableSetOf()) { (point2d, z) -> Point3d(point2d.x, point2d.y, z) }

    return generateSequence(setOf(Point3d(0, 0, 0))) { points ->
        points.flatMapTo(mutableSetOf()) { it.neighbors(includeThis = true) }
            .minus(input)
            .intersect(candidates)
            .takeUnless { it.size == points.size }
    }
        .flatten()
        .toSet()
        .let(candidates::minus)
}

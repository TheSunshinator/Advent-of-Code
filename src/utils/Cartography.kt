package utils

import arrow.optics.copy
import arrow.optics.optics
import kotlin.math.abs
import kotlin.math.sign

@optics
data class Point(val x: Int, val y: Int) {

    constructor(): this(0, 0)

    companion object
}

sealed interface Direction {
    sealed interface Horizontal: Direction
    sealed interface Vertical: Direction

    object Up: Vertical
    object Down: Vertical
    object Left: Horizontal
    object Right: Horizontal
}

fun Point.neighbors(
    includeThis: Boolean = false,
    includeDiagonal: Boolean = false,
): Sequence<Point> = sequence {
    if (includeDiagonal) yield(Point(x - 1, y - 1))
    yield(Point(x, y - 1))
    if (includeDiagonal) yield(Point(x + 1, y - 1))
    yield(Point(x - 1, y))
    if (includeThis) yield(this@neighbors)
    yield(Point(x + 1, y))
    if (includeDiagonal) yield(Point(x - 1, y + 1))
    yield(Point(x, y + 1))
    if (includeDiagonal) yield(Point(x + 1, y + 1))
}

infix fun Point.move(direction: Direction) = when (direction) {
    Direction.Left -> copy { Point.x transform Int::dec }
    Direction.Right -> copy { Point.x transform Int::inc }
    Direction.Down -> copy { Point.y transform Int::inc }
    Direction.Up -> copy { Point.y transform Int::dec }
}

operator fun <T> List<List<T>>.get(p: Point) = this[p.x][p.y]
operator fun <T> List<MutableList<T>>.set(p: Point, value: T) {
    this[p.x][p.y] = value
}
fun <T> List<List<T>>.getOrElse(p: Point, defaultValue: (Point) -> T) : T {
    return if (p.x in indices && p.y in this[p.x].indices) this[p]
    else defaultValue(p)
}

data class MapDetails<T>(
    val position: Point,
    val value: T,
)

fun <T> List<List<T>>.detailsAt(position: Point) = MapDetails(position, this[position])
fun <T> List<List<T>>.detailsSequence(): Sequence<MapDetails<T>> {
    return coordinates().map { MapDetails(it, this[it]) }
}

infix fun Point.sequenceTo(other: Point): Sequence<Point> {
    return when {
        x == other.x -> if (y == other.y) sequenceOf(this)
        else IntProgression.fromClosedRange(y, other.y, step = (other.y - y).sign)
            .asSequence()
            .map { Point(x, it) }
        y == other.y -> IntProgression.fromClosedRange(x, other.x, step = (other.x - x).sign)
            .asSequence()
            .map { Point(it, y) }
        else -> throw UnsupportedOperationException("Only horizontal lines are supported")
    }
}

infix fun Point.manhattanDistanceTo(other: Point): Int = abs(other.x - x) + abs(other.y - y)

fun Set<Point>.print() {
    if (isEmpty()) {
        println("Set is empty")
        return
    }

    val ys = groupBy { it.y }
    val xRange = minMaxOf { it.x }?.let { it.first..it.second } ?: return
    ys.keys.minMax()
        ?.let { it.first..it.second }
        ?.asSequence()
        ?.map { y ->
            xRange.map { Point(it, y) }
        }
        ?.forEach { row ->
            row.forEach { point ->
                print(if (point in this) "█" else "_")
            }
            println()
        }
}

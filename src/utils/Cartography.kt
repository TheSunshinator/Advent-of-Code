package utils

import arrow.optics.copy
import arrow.optics.optics
import kotlin.math.abs
import kotlin.math.sign

@optics
data class Point(val x: Int, val y: Int) {
    constructor() : this(0, 0)

    companion object
}

sealed interface Direction {
    sealed interface Horizontal : Direction
    sealed interface Vertical : Direction

    fun rotateClockwise() = when (this) {
        Left -> Up
        Up -> Right
        Right -> Down
        Down -> Left
    }

    fun rotateAntiClockwise() = when (this) {
        Left -> Down
        Down -> Right
        Right -> Up
        Up -> Left
    }

    fun opposite() = when (this) {
        Left -> Right
        Down -> Up
        Right -> Left
        Up -> Down
    }

    object Up : Vertical {
        override fun toString() = "Direction.Up"
    }

    object Down : Vertical {
        override fun toString() = "Direction.Down"
    }

    object Left : Horizontal {
        override fun toString() = "Direction.Left"
    }

    object Right : Horizontal {
        override fun toString() = "Direction.Right"
    }
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
operator fun List<String>.get(p: Point) = this[p.y][p.x]
operator fun <T> List<MutableList<T>>.set(p: Point, value: T) {
    this[p.x][p.y] = value
}

fun <T> List<List<T>>.getOrElse(p: Point, defaultValue: (Point) -> T): T {
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
    val xRange = minMaxOfOrNull { it.x }?.let { it.first..it.second } ?: return
    ys.keys.minMaxOrNull()
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

@optics
data class Point3d(val x: Int = 0, val y: Int = 0, val z: Int = 0) {
    companion object
}

infix fun Point3d.manhattanDistanceTo(other: Point3d): Int = abs(other.x - x) + abs(other.y - y) + abs(other.z - z)

fun Point3d.neighbors(
    includeThis: Boolean = false,
    includeDiagonal: Boolean = false,
): Sequence<Point3d> {
    return sequenceOf(
        Point3d.x,
        Point3d.y,
        Point3d.z,
    )
        .mapTo(mutableListOf()) { lens ->
            sequenceOf(-1, 0, 1)
                .map { increment -> lens.lift { it + increment } }
        }
        .let { (xModifications, yModifications, zModifications) ->
            xModifications cartesianProduct yModifications cartesianProduct zModifications
        }
        .map { (otherModifications, modifyZ) ->
            val (modifyX, modifyY) = otherModifications
            modifyX(this)
                .let(modifyY)
                .let(modifyZ)
        }
        .let { neighborSequence ->
            if (includeThis) neighborSequence else neighborSequence.filterNot { it == this }
        }
        .let { neighborSequence ->
            if (includeDiagonal) neighborSequence else neighborSequence.filter { it manhattanDistanceTo this < 2 }
        }
}

fun Set<Point3d>.print3d() {
    if (isEmpty()) {
        println("Set is empty")
        return
    }
    val zs = groupBy { it.z }
    val xRange = minMaxOfOrNull { it.x }?.let { it.first..it.second } ?: return
    val yRange = minMaxOfOrNull { it.y }?.let { it.first..it.second } ?: return
    val plane = xRange cartesianProduct yRange
    zs.keys.minMaxOrNull()
        ?.let { it.first..it.second }
        ?.asSequence()
        ?.map { plane.filterTo(mutableSetOf()) { (x, y) -> Point3d(x, y, it) in this } }
        ?.forEach { zPlane ->
            zPlane.print()
            println()
        }
}

// Counts number of points inside a loop
fun List<Point>.applyShoelaceTheorem(): Long {
    check(first() == last()) { "Shoelace theorem must be applied on a loop" }
    val xSequence = asSequence().map { it.x.toLong() }
    val ySequence = asSequence().map { it.y.toLong() }
    return (
        xSequence.zip(ySequence.drop(1), Long::times).sum() - ySequence.zip(xSequence.drop(1), Long::times).sum()
    ) / 2
}

// Compute area defined by loop, including points in this
fun List<Point>.applyPickTheorem(): Long {
    check(first() == last()) { "Shoelace theorem must be applied on a loop" }
    return applyShoelaceTheorem() + (size - 1) / 2 + 1
}

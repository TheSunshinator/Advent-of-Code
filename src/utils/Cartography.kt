package utils

import arrow.optics.optics

@optics
data class Point(val x: Int, val y: Int) {

    constructor(): this(0, 0)
    fun neighbors(
        includeThis: Boolean = false,
        includeDiagonal: Boolean = false,
    ) = sequence {
        if (includeDiagonal) yield(Point(x - 1, y - 1))
        yield(Point(x, y - 1))
        if (includeDiagonal) yield(Point(x + 1, y - 1))
        yield(Point(x - 1, y))
        if (includeThis) yield(this@Point)
        yield(Point(x + 1, y))
        if (includeDiagonal) yield(Point(x - 1, y + 1))
        yield(Point(x, y + 1))
        if (includeDiagonal) yield(Point(x + 1, y + 1))
    }

    companion object
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

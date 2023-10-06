package day22

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.optics.optics
import io.kotest.matchers.shouldBe
import utils.Direction
import utils.Point
import utils.cyclical
import utils.readInput
import utils.y

fun main() {
    val (testMaze, testRoute) = readInput("22", "test_input").parse()
    val (realMaze, realRoute) = readInput("22", "input").parse()

    testMaze.runMazeRoute(testRoute)
        .computePassword()
        .also(::println) shouldBe 6032

    realMaze.runMazeRoute(realRoute)
        .computePassword()
        .let(::println)

    val testCube = readInput("22", "test_input_converted_part_2").parseCube(4)
    val realCube = realMaze.parseCube(50)

    testCube.runCubeRoute(testRoute)
        .computePassword()
        .also(::println) shouldBe 5031

    realCube.runCubeRoute(testRoute)
        .computePassword()
        .let(::println)
}

private fun List<String>.parse(): Pair<List<String>, List<Either<Int, TurnDirection>>> {
    return takeWhile { it.isNotBlank() } to last().split(splittingRouteRegex).map {
        when (it) {
            "R" -> TurnDirection.CLOCKWISE.right()
            "L" -> TurnDirection.ANTI_CLOCKWISE.right()
            else -> it.toInt().left()
        }
    }
}

private val splittingRouteRegex = "(?<=\\d)(?=[RL])|(?<=[RL])(?=\\d)".toRegex()

private enum class TurnDirection {
    CLOCKWISE, ANTI_CLOCKWISE
}

@optics
data class Position(
    val coordinates: Point,
    val direction: Direction,
) {
    companion object
}

private fun List<String>.runMazeRoute(route: List<Either<Int, TurnDirection>>): Position {
    val startPosition = Position(
        coordinates = Point(first().indexOfFirst { it != ' ' }, 0),
        Direction.Right,
    )
    return route.fold(startPosition, ::executeDirective)
}

private fun List<String>.executeDirective(position: Position, directive: Either<Int, TurnDirection>): Position {
    return when (directive) {
        is Either.Left -> Position.coordinates.modify(position) { currentPosition ->
            when (position.direction) {
                Direction.Right -> this[currentPosition.y].asSequence()
                    .withIndex()
                    .getStopCoordinate(currentPosition.x, directive.value)
                    .let { Point(it, currentPosition.y) }

                Direction.Left -> this[currentPosition.y]
                    .withIndex()
                    .reversed()
                    .asSequence()
                    .getStopCoordinate(
                        this[currentPosition.y].length - currentPosition.x - 1,
                        directive.value
                    )
                    .let { Point(it, currentPosition.y) }

                Direction.Down -> asSequence()
                    .map { it.getOrElse(currentPosition.x) { ' ' } }
                    .withIndex()
                    .getStopCoordinate(currentPosition.y, directive.value)
                    .let { Point(currentPosition.x, it) }

                Direction.Up -> asSequence()
                    .map { it.getOrElse(currentPosition.x) { ' ' } }
                    .withIndex()
                    .toList()
                    .reversed()
                    .asSequence()
                    .getStopCoordinate(size - currentPosition.y - 1, directive.value)
                    .let { Point(currentPosition.x, it) }
            }
        }

        is Either.Right -> Position.direction.modify(
            position,
            if (directive.value == TurnDirection.CLOCKWISE) Direction::rotateClockwise
            else Direction::rotateAntiClockwise
        )
    }
}

private fun Sequence<IndexedValue<Char>>.getStopCoordinate(startCoordinate: Int, movementLength: Int): Int {
    return cyclical()
        .drop(startCoordinate)
        .getStop(movementLength)
        .index
}

private fun Sequence<IndexedValue<Char>>.getStop(movementLength: Int): IndexedValue<Char> {
    return filterNot { it.value == ' ' }
        .take(movementLength + 1)
        .takeWhile { it.value != '#' }
        .last()
}

private fun Position.computePassword() = 1000 * (coordinates.y + 1) + 4 * (coordinates.x + 1) + when (direction) {
    Direction.Right -> 0
    Direction.Down -> 1
    Direction.Left -> 2
    Direction.Up -> 3
}

private fun List<String>.parseCube(size: Int): Map<Int, Map<Direction, List<String>>> {
    return mapOf(
        1 to asSequence()
            .take(size)
            .mapTo(mutableListOf()) { it.substring(size until 2 * size) }
            .run {
                 generateSequence(Direction.Up to this) {

                 }
                     .take(4)
                     .toMap()
            },
        2 to asSequence()
            .take(size)
            .mapTo(mutableListOf()) { it.substring(2 * size) },
        3 to asSequence()
            .drop(size)
            .take(size)
            .mapTo(mutableListOf()) { it.substring(size) },
        4 to asSequence()
            .drop(2 * size)
            .take(size)
            .mapTo(mutableListOf()) { it.take(size) },
        5 to asSequence()
            .drop(2 * size)
            .take(size)
            .mapTo(mutableListOf()) { it.substring(size) },
        6 to drop(3 * size),
    )
}

private fun Map<Int, List<String>>.runCubeRoute(route: List<Either<Int, TurnDirection>>): CubePosition {
    val startPosition = Position(
        coordinates = Point(0, 0),
        Direction.Right,
    )
    return route.fold(CubePosition(1, startPosition), ::executeDirective)
}

@optics
data class CubePosition(
    val face: Int,
    val position: Position,
) {
    companion object
}

private fun Map<Int, List<String>>.executeDirective(
    position: CubePosition,
    directive: Either<Int, TurnDirection>,
): CubePosition = when (directive) {
    is Either.Left -> when (position.face) {
        1 -> when (position.position.direction) {
            Direction.Left -> {
                getValue(1).asSequence()

                TODO()
            }
            Direction.Right -> {
                getValue(1)[position.position.coordinates.y]
                    .asSequence()
                    .withIndex()
                    .map { (x, content) ->
                        CubePosition(1, Position(Point(x, position.position.coordinates.y), Direction.Right)) to content
                    }
                    .plus(
                        getValue(2)[position.position.coordinates.y]
                            .asSequence()
                            .withIndex()
                            .map { (x, content) ->
                                CubePosition(2, Position(Point(x, position.position.coordinates.y), Direction.Right)) to content
                            }
                    )
                    .plus(
                        getValue(5).run {
                            val realY = size - position.position.coordinates.y
                            this[realY]
                                .withIndex()
                                .reversed()
                                .asSequence()
                                .map { (x, content) ->
                                    CubePosition(5, Position(Point(x, realY), Direction.Left)) to content
                                }
                        }
                    )
                    .plus(
                        getValue(4).run {
                            val realY = size - position.position.coordinates.y
                            this[realY]
                                .withIndex()
                                .reversed()
                                .asSequence()
                                .map { (x, content) ->
                                    CubePosition(4, Position(Point(x, realY), Direction.Left)) to content
                                }
                        }
                    )
                    .cyclical()
                    .drop(position.position.coordinates.x)
                    .filterNot { it.second == ' ' }
                    .take(directive.value + 1)
                    .takeWhile { it.second != '#' }
                    .last()
                    .first
            }
            Direction.Down -> TODO()
            Direction.Up -> TODO()
        }
        2 -> { TODO() }
        3 -> { TODO() }
        4 -> { TODO() }
        5 -> { TODO() }
        else -> { TODO() }
    }
    is Either.Right -> CubePosition.position.direction.modify(
        position,
        if (directive.value == TurnDirection.CLOCKWISE) Direction::rotateClockwise
        else Direction::rotateAntiClockwise
    )
}

private fun Map<Int, List<String>>.movementSequence(
    face: Int,
    faceOrientation: Direction,
    position: Position,
    direction: Direction,
): Sequence<CubePosition> {
    val faceMaze = getValue(face)
    val (flatPosition, flatDirection) = when (faceOrientation) {
        Direction.Up -> position to direction
        Direction.Down -> Position.coordinates.y.modify(position) { faceMaze.size - it } to if (direction is Direction.Vertical) direction.opposite() else direction
        Direction.Left -> TODO()
        Direction.Right -> TODO()
    }
}



private fun CubePosition.computePassword(): Int {
    return TODO()
}
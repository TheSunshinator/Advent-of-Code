package day14

import io.kotest.matchers.shouldBe
import utils.Point
import utils.not
import utils.readInput
import utils.sequenceTo

fun main() {
    val testInput = readInput("14", "test_input")
    val realInput = readInput("14", "input")

    val testWalls = parseRockWalls(testInput)
    val testInitialState = testWalls.initialState()
    val isInTestAbyss = testWalls.getIsInAbyssPredicate

    sandFallingSequence(
        testInitialState,
        isFinal = { isFallingInAbyss(isInTestAbyss) },
        isValid = !isInTestAbyss,
    )
        .count()
        .minus(1)
        .also(::println) shouldBe 24

    val realWalls = parseRockWalls(realInput)
    val initialState = realWalls.initialState()
    val isInAbyss = realWalls.getIsInAbyssPredicate

    sandFallingSequence(
        initialState,
        isFinal = { isFallingInAbyss(isInAbyss) },
        isValid = !isInAbyss,
    )
        .count()
        .minus(1)
        .let(::println)

    sandFallingSequence(
        testInitialState,
        isFinal = { lastFallenGrain == Point(500, 0) },
        isValid = testWalls.getIsAboveFloorPredicate,
    )
        .count()
        .also(::println) shouldBe 93

    sandFallingSequence(
        initialState,
        isFinal = { lastFallenGrain == Point(500, 0) },
        isValid = realWalls.getIsAboveFloorPredicate,
    )
        .count()
        .let(::println)
}

private fun parseRockWalls(input: List<String>): Set<Point> {
    return input.asSequence()
        .map(pointRegex::findAll)
        .flatMapTo(mutableSetOf()) { pointStops ->
            pointStops.map {
                Point(it.groupValues[1].toInt(), it.groupValues[2].toInt())
            }
                .windowed(2) { (start, end) -> start sequenceTo end }
                .flatten()
        }
}

private val pointRegex = "(\\d+),(\\d+)".toRegex()

private data class State(
    val walls: Set<Point>,
    val sand: Set<Point>,
    val lastGrainPath: List<Point>,
) {
    val lastFallenGrain
        get() = lastGrainPath.last()
}

private fun Set<Point>.initialState(): State {
    return filter { it.x == 500 }
        .minBy { it.y }
        .let { Point(500, 0) sequenceTo it }
        .toList()
        .dropLast(1)
        .let {
            State(
                walls = this,
                sand = setOf(it.last()),
                lastGrainPath = it,
            )
        }
}

private val Set<Point>.getIsInAbyssPredicate: Point.() -> Boolean
    get() {
        val xRange = minOf { it.x }..maxOf { it.x }
        val maxY = maxOf { it.y }
        return { x !in xRange || y >= maxY }
    }

private fun sandFallingSequence(
    initialState: State,
    isFinal: State.() -> Boolean,
    isValid: Point.() -> Boolean,
) = generateSequence(initialState) { lastState ->
    if (lastState.isFinal()) null
    else {
        val projectedGrain = lastState.lastGrainPath[lastState.lastGrainPath.lastIndex - 1]
        val nextGrainPath = lastState.lastGrainPath
            .dropLast(1)
            .plus(
                generateSequence(projectedGrain){ previousGrain ->
                    previousGrain.fallPrioritySequence.firstOrNull {
                        it !in lastState.walls
                            && it !in lastState.sand
                            && it.isValid()
                    }
                }
                    .drop(1)
                    .takeWhile(isValid)
            )

        State(
            walls = lastState.walls,
            sand = lastState.sand + nextGrainPath.last(),
            lastGrainPath = nextGrainPath,
        )
    }
}

private val Point.fallPrioritySequence
    get() = sequenceOf(
        Point(x, y + 1),
        Point(x - 1, y + 1),
        Point(x + 1, y + 1),
    )

private val Set<Point>.getIsAboveFloorPredicate: Point.() -> Boolean
    get() {
        val floorY = maxOf { it.y } + 2
        return { y < floorY }
    }

private fun State.isFallingInAbyss(isInAbyss: Point.() -> Boolean): Boolean {
    return lastFallenGrain.fallPrioritySequence
        .firstOrNull { it !in walls && it !in sand }
        ?.isInAbyss() == true
}
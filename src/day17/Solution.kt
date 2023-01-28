package day17

import arrow.optics.copy
import arrow.optics.optics
import io.kotest.matchers.shouldBe
import kotlin.math.max
import utils.Direction
import utils.Point
import utils.continuing
import utils.cyclical
import utils.mapSecond
import utils.move
import utils.readInput
import utils.x
import utils.y

fun main() {
    val testInput = readInput("17", "test_input").first()
    val realInput = readInput("17", "input").first()

    findCycle(testInput)
        .totalHeight(2022L)
        .also(::println) shouldBe 3068L

    findCycle(realInput)
        .totalHeight(2022L)
        .let(::println)

    findCycle(testInput)
        .totalHeight(1000000000000)
        .also(::println) shouldBe 1514285714288L

    findCycle(realInput)
        .totalHeight(1000000000000)
        .let(::println)
}

private fun findCycle(completePushSequence: String): Cycle {
    val testRockMovements = completePushSequence.asRockMovementSequence()
    val shapeSequence = Shape.values().asSequence().cyclical().continuing()

    return generateSequence(emptyList<RockFallPattern>()) { previousState ->
        val lastRockFallPattern = previousState.lastOrNull()
        val startingPosition = Point(2, (lastRockFallPattern?.towerHeight?.unaryMinus() ?: 0) - 3)
        val initialRock = Rock(
            startingPosition,
            shapeSequence.first()
        )
        val (restingRock, pushSequence) = initialRock.fall(testRockMovements, previousState)
        val rockTop = restingRock.occupiedSpace.minOf { it.y }
        val fallenRock = RockFallPattern(
            restingRock = restingRock,
            pushSequence = pushSequence!!,
            towerHeight = if (lastRockFallPattern == null) 1 - rockTop
            else max(lastRockFallPattern.towerHeight, 1 - rockTop)
        )
        previousState + fallenRock
    }
        .mapNotNull { it.toCycleOrNull() }
        .first()
}

private fun Rock.fall(
    testRockMovements: Sequence<IndexedValue<Direction>>,
    previousState: List<RockFallPattern>,
): Pair<Rock, Pair<IndexedValue<Direction>, IndexedValue<Direction>>?> {
    return generateSequence(this to null as Pair<IndexedValue<Direction>, IndexedValue<Direction>>?) { (rock, pushSequence) ->
        val currentMovement = testRockMovements.first()
        val movement = currentMovement.value
        val movedRock = rock.move(movement)
        val isInvalidPosition = movedRock.occupiedSpace.any { rockPart ->
            rockPart.x !in 0 until 7
                || rockPart.y > 0
                || rockPart in previousState.flatMap { it.restingRock.occupiedSpace }
        }

        if (movement == Direction.Down && isInvalidPosition) null
        else (if (isInvalidPosition) rock else movedRock) to (
            pushSequence?.mapSecond { currentMovement } ?: (currentMovement to currentMovement)
        )
    }.last()
}

private fun String.asRockMovementSequence(): Sequence<IndexedValue<Direction>> {
    return asSequence()
        .map { if (it == '>') Direction.Right else Direction.Left }
        .withGravity()
        .withIndex()
        .cyclical()
        .continuing()
}

private fun Sequence<Direction.Horizontal>.withGravity() = flatMap { sequenceOf(it, Direction.Down) }

@optics
data class Rock(
    val reference: Point,
    val shape: Shape,
) {
    val occupiedSpace = occupiedSpaceFrom(reference, shape)

    companion object
}

enum class Shape {
    FLAT_LINE, PLUS, CORNER, TALL_LINE, SQUARE
}

private fun Rock.move(direction: Direction) = Rock.reference.modify(this) { it move direction }

private fun occupiedSpaceFrom(reference: Point, shape: Shape): Set<Point> = when (shape) {
    Shape.FLAT_LINE -> generateSequence(reference, Point.x.lift(Int::inc)).take(4).toSet()
    Shape.PLUS -> setOf(
        Point.x.modify(reference, Int::inc),
        Point.y.modify(reference, Int::dec),
        reference.copy {
            Point.x transform { it + 2 }
            Point.y transform Int::dec
        },
        reference.copy {
            Point.x transform Int::inc
            Point.y transform { it - 2 }
        },
    )

    Shape.CORNER -> setOf(
        reference,
        Point.x.modify(reference, Int::inc),
        Point.x.modify(reference) { it + 2 },
        reference.copy {
            Point.x transform { it + 2 }
            Point.y transform Int::dec
        },
        reference.copy {
            Point.x transform { it + 2 }
            Point.y transform { it - 2 }
        },
    )

    Shape.TALL_LINE -> generateSequence(reference, Point.y.lift(Int::dec)).take(4).toSet()
    Shape.SQUARE -> setOf(
        reference,
        Point.x.modify(reference, Int::inc),
        reference.copy {
            Point.x transform Int::inc
            Point.y transform Int::dec
        },
        Point.y.modify(reference, Int::dec),
    )
}

private fun List<RockFallPattern>.toCycleOrNull(): Cycle? {
    val reference = lastOrNull() ?: return null
    val cycleStart = asSequence()
        .withIndex()
        .lastOrNull { (index, potentialCycleStart) ->
            potentialCycleStart != reference
                && potentialCycleStart.pushSequence.first.index == reference.pushSequence.first.index
                && potentialCycleStart.pushSequence.second.index == reference.pushSequence.second.index
                && potentialCycleStart.restingRock.reference.x == reference.restingRock.reference.x
                && potentialCycleStart.restingRock.shape == reference.restingRock.shape
                && topTower(lastIndex) == topTower(index)
        }?.index
    return if (cycleStart != null && cycleStart in indices) Cycle(
        preCycle = take(cycleStart + 1),
        cycle = subList(cycleStart + 1, size),
    ) else null
}

private fun List<RockFallPattern>.topTower(rockCount: Int): Set<Point> {
    val heightDifference = this[rockCount].towerHeight - last().towerHeight
    return subList(rockCount - 9, rockCount + 1)
        .asSequence()
        .flatMap { it.restingRock.occupiedSpace }
        .mapTo(mutableSetOf(), Point.y.lift { it + heightDifference })
}

private fun Cycle.totalHeight(rockCount: Long): Long {
    if (rockCount <= preCycle.size) return preCycle[rockCount.toInt() - 1].towerHeight.toLong()
    val heightAtCycleStart = preCycle.last().towerHeight
    val cycleHeight = cycle.last().towerHeight - heightAtCycleStart
    val cycleCount = (rockCount - preCycle.size) / cycle.size
    val missingRocks = (rockCount - preCycle.size) % cycle.size
    val missingHeight =
        if (missingRocks == 0L) 0L else cycle[missingRocks.toInt() - 1].towerHeight.toLong() - heightAtCycleStart
    return heightAtCycleStart + cycleCount * cycleHeight + missingHeight
}

@optics
data class RockFallPattern(
    val restingRock: Rock,
    val pushSequence: Pair<IndexedValue<Direction>, IndexedValue<Direction>>,
    val towerHeight: Int = 0,
) {
    companion object
}

private data class Cycle(
    val preCycle: List<RockFallPattern>,
    val cycle: List<RockFallPattern>,
)

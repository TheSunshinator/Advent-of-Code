package day09

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.optics.Lens
import arrow.optics.optics
import io.kotest.matchers.shouldBe
import kotlin.math.sign
import utils.Point
import utils.neighbors
import utils.readInput
import utils.x
import utils.y

fun main() {
    val testInput = readInput("09", "test_input").asInstructionSequence()
    val realInput = readInput("09", "input").asInstructionSequence()

    testInput.computeFinalState(2).visitedPoints.size
        .also(::println) shouldBe 13

    realInput.computeFinalState(2).visitedPoints.size
        .let(::println)

    testInput.computeFinalState(10).visitedPoints.size
        .also(::println) shouldBe 1

    realInput.computeFinalState(10).visitedPoints.size
        .let(::println)
}

private fun List<String>.asInstructionSequence(): Sequence<Char> {
    return asSequence()
        .map { it.split(" ") }
        .flatMap { (direction, quantity) ->
            generateSequence { direction.first() }
                .take(quantity.toInt())
        }
}

private fun Sequence<Char>.computeFinalState(ropeLength: Int): State {
    val initialState = State(
        NonEmptyList(Point(), List(ropeLength - 1) { Point() }),
        emptySet(),
    )
    return fold(initialState) { state, direction ->
        val newHeadPosition = moveState.getValue(direction)(state)
        val rope = state.rope.tail.fold(nonEmptyListOf(newHeadPosition)) { rope, oldPosition ->
            val newPreviousPosition = rope.last()
            val newPosition = if (
                newPreviousPosition in oldPosition.neighbors(includeThis = true, includeDiagonal = true)
            ) oldPosition
            else Point(
                oldPosition.x + (newPreviousPosition.x - oldPosition.x).sign,
                oldPosition.y + (newPreviousPosition.y - oldPosition.y).sign
            )
            rope + newPosition
        }
        State(
            rope,
            visitedPoints = state.visitedPoints + rope.last()
        )
    }
}

@optics
data class State(
    val rope: NonEmptyList<Point>,
    val visitedPoints: Set<Point>,
) {
    companion object
}

private val moveState: Map<Char, (State) -> Point> = buildMap {
    val newHeadComputation: (Lens<Point, Int>, map: (Int) -> Int) -> (State) -> Point = { lens, transform ->
        { lens.modify(it.rope.head, transform) }
    }
    put('U', newHeadComputation(Point.y, Int::dec))
    put('D', newHeadComputation(Point.y, Int::inc))
    put('R', newHeadComputation(Point.x, Int::inc))
    put('L', newHeadComputation(Point.x, Int::dec))
}
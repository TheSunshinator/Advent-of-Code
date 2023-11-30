package year2022.day15

import io.kotest.matchers.shouldBe
import kotlin.math.max
import utils.Point
import utils.manhattanDistanceTo
import utils.plusOrMinus
import utils.readInput
import utils.size

fun main() {
    val testEmptySpots = readInput("15", "test_input").toSensors().computeEmptySpots()
    val emptySpots = readInput("15", "input").toSensors().computeEmptySpots()

    testEmptySpots.getValue(10)
        .sumOf { it.size }
        .also(::println) shouldBe 26

    emptySpots.getValue(2000000)
        .sumOf { it.size }
        .let(::println)

    testEmptySpots.asSequence()
        .first { it.key in 0..20 && it.value.size > 1 }
        .let { (y, confirmedX) -> Point(confirmedX.first().last + 1, y) }
        .let { it.x * 4000000 + it.y }
        .also(::println) shouldBe 56000011

    emptySpots.asSequence()
        .first { it.key in 0..4000000 && it.value.size > 1 }
        .let { (y, confirmedX) -> Point(confirmedX.first().last + 1, y) }
        .let { it.x * 4000000L + it.y }
        .let(::println)
}

private fun List<String>.toSensors(): List<Sensor> {
    return asSequence()
        .mapNotNull(parsingRegex::matchEntire)
        .map { it.groupValues }
        .mapTo(mutableListOf()) { (_, sensorX, sensorY, beaconX, beaconY) ->
            Sensor(
                Point(sensorX.toInt(), sensorY.toInt()),
                Point(beaconX.toInt(), beaconY.toInt()),
            )
        }
}

private val parsingRegex = "Sensor at x=(\\d+), y=(\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)".toRegex()

private data class Sensor(
    val position: Point,
    val closestBeacon: Point,
) {
    val distance = position manhattanDistanceTo closestBeacon
    val emptySpotsConfirmed: Map<Int, IntRange> = (0..2 * distance).associate { row ->
        Pair(
            position.y - distance + row,
            if (row <= distance) position.x plusOrMinus row
            else position.x.plusOrMinus(2 * distance - row)
        )
    }
}

private fun List<Sensor>.computeEmptySpots(): Map<Int, List<IntRange>> {
    return asSequence()
        .map { it.emptySpotsConfirmed }
        .flatMap { it.entries }
        .groupBy { it.key }
        .mapValues { (_, ranges) ->
            ranges.asSequence()
                .map { it.value }
                .simplifyRanges()
        }
}

private fun Sequence<IntRange>.simplifyRanges(): List<IntRange> = sortedBy { it.first }.fold(mutableListOf()) { rangeAccumulator, range ->
    val lastRange = rangeAccumulator.lastOrNull()
    when {
        lastRange == null || lastRange.last < range.first - 1 -> rangeAccumulator.add(range)
        else -> rangeAccumulator[rangeAccumulator.lastIndex] = IntRange(
            lastRange.first,
            max(range.last, lastRange.last)
        )
    }
    rangeAccumulator
}

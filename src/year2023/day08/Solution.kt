package year2023.day08

import arrow.core.nonEmptyListOf
import io.kotest.matchers.shouldBe
import utils.ProblemPart
import utils.cyclical
import utils.findCycle
import utils.leastCommonMultiple
import utils.leastCommonMultipleOf
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 8, "test_input_2", transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(2, 6),
            algorithm = { (steps, map) -> part1(steps, map) },
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(2, 3),
            algorithm = { (steps, map) -> part2(steps, map) },
        ),
    )
}

private fun parse(input: List<String>): Pair<String, Map<String, Pair<String, String>>> {
    return input.first() to input.asSequence()
        .drop(2)
        .mapNotNull(parsingRegex::matchEntire)
        .map { it.groupValues }
        .associate { (_, nodeId, leftNodeId, rightNodeId) ->
            nodeId to (leftNodeId to rightNodeId)
        }
}

private val parsingRegex = "([A-Z]{3}) = \\(([A-Z]{3}), ([A-Z]{3})\\)".toRegex()

private fun part1(steps: String, map: Map<String, Pair<String, String>>): Int {
    return steps.asSequence().cyclical()
        .runningFold("AAA") { currentNode, direction ->
            val (leftNode, rightNode) = map.getValue(currentNode)
            if (direction == 'L') leftNode else rightNode
        }
        .takeWhile { it != "ZZZ" }
        .count()
}

private fun part2(steps: String, map: Map<String, Pair<String, String>>): Long {
    val endCyclePositions = map.mapValues { (start, _) ->
        steps.fold(start) { currentPosition, nextStep ->
            val (left, right) = map.getValue(currentPosition)
            if (nextStep == 'R') right else left
        }
    }

    return map.keys.asSequence()
        .filter { it.last() == 'A' }
        .map { generateSequence(it, endCyclePositions::get).findCycle() }
        .leastCommonMultipleOf { it.loopSize.toLong() }
        .let { it * steps.length }
}

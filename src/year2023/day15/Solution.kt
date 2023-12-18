package year2023.day15

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 15, transform = List<String>::first)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(1320),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(145),
            algorithm = ::part2,
        ),
    )
}

private fun part1(input: String) = input.splitToSequence(',').sumOf(::computeHash)

private fun computeHash(step: String): Long {
    return step.asSequence()
        .map { it.code }
        .fold(0L) { hash, value -> (hash + value) * 17 % 256 }
}

private fun part2(input: String): Long {
    return input.splitToSequence(',')
        .mapNotNull(operationRegex::matchEntire)
        .buildBoxes()
        .asSequence()
        .map { box ->
            box.asSequence()
                .mapIndexed { index, lens -> (index + 1) * lens.value }
                .sum()
        }
        .withIndex()
        .sumOf { (index, boxValue) -> (index + 1) * boxValue }
}

private fun Sequence<MatchResult>.buildBoxes(): List<Map<String, Long>> {
    return fold(List(256) { mutableMapOf<String, Long>() }) { boxes, operation ->
        val lensId = operation.groupValues[1]
        val targetBox = computeHash(lensId).toInt()

        if (operation.groupValues[2] == "-") boxes[targetBox].remove(lensId)
        else boxes[targetBox][lensId] = operation.groupValues[3].toLong()

        boxes
    }
}

private val operationRegex = "([a-z]+)(?:(-)|=(\\d+))".toRegex()

package year2023.day12

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.findAllWithOverlap
import utils.readInputs
import utils.runAlgorithm
import utils.withMemoization

fun main() {
    val (realInput, testInputs) = readInputs(2023, 12, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(21),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(525152),
            algorithm = ::part2,
        ),
    )
}

private fun parse(input: List<String>): List<Input> = input.map { line ->
    val (record, groups) = line.split(' ')
    Input(
        record = record,
        groups = groups.splitToSequence(',').map { it.toInt() }.toList()
    )
}

private fun part1(input: List<Input>): Long = input.sumOf(possibilityCounter::invoke)

private val possibilityCounter: DeepRecursiveFunction<Input, Long> = withMemoization { currentState ->
    val firstGroupSize = currentState.groups.firstOrNull()
    when {
        firstGroupSize == null -> if (currentState.record.any { it == '#' }) 0 else 1
        currentState.unknownIndexes.isEmpty() -> if (currentState.missingDamaged == 0) {
            currentState.groups
                .joinToString(separator = "\\.+", prefix = "\\.*", postfix = "\\.*") { "#{$it}" }
                .toRegex()
                .matches(currentState.record)
                .let { if (it) 1 else 0 }
        } else 0

        else -> "[?#]{$firstGroupSize}(?!#)".toRegex()
            .findAllWithOverlap(currentState.record)
            .filterNot {
                !currentState.record.matches("[^#]{${it.range.first}}.*".toRegex())
            }
            .map { matchResult ->
                val nextChar = currentState.record.getOrNull(matchResult.range.last + 1)
                Input(
                    currentState.record.drop(matchResult.range.last + 1).let {
                        if (nextChar == null) it else it.drop(1)
                    },
                    currentState.groups.drop(1),
                )
            }
            .sumOf { callRecursive(it) }
    }
}

private fun part2(input: List<Input>): Long {
    return input.asSequence()
        .map { currentInput ->
            Input(
                generateSequence { currentInput.record }
                    .take(5)
                    .joinToString(separator = "?"),
                buildList {
                    repeat(5) { addAll(currentInput.groups) }
                }
            )
        }
        .sumOf(possibilityCounter::invoke)
}

private data class Input(
    val record: String,
    val groups: List<Int>,
) {
    val unknownIndexes = record.asSequence().withIndex()
        .mapNotNull { (index, char) -> if (char == '?') index else null }
        .toSet()
    val missingDamaged = groups.sum() - record.count { it == '#' }
}

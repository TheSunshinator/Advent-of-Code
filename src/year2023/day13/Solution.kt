package year2023.day13

import arrow.core.Either
import arrow.core.identity
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm
import utils.transpose

fun main() {
    val (realInput, testInputs) = readInputs(2023, 13, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(405),
            algorithm = getSolution(reflectionError = 0),
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(400),
            algorithm = getSolution(reflectionError = 1),
        ),
    )
}

private fun parse(input: List<String>): List<List<String>> {
    return input.fold(mutableListOf(mutableListOf<String>())) { accumulator, line ->
        if (line.isEmpty()) accumulator.add(mutableListOf())
        else accumulator.last().add(line)
        accumulator
    }
}

private fun getSolution(reflectionError: Int): (List<List<String>>) -> Long = { input ->
    input.asSequence()
        .map { findMirror(it, reflectionError = reflectionError) }
        .sumOf { mirrorLocation ->
            mirrorLocation?.fold(
                ifLeft = ::identity,
                ifRight = { 100 * it },
            ) ?: 0
        }
}

private fun findMirror(pattern: List<String>, reflectionError: Int = 0): Either<Long, Long>? {
    return pattern.findMirror(reflectionError)?.right()
        ?: pattern.transpose().findMirror(reflectionError)?.left()
}

private fun List<String>.findMirror(reflectionError: Int): Long? {
    return (1..lastIndex).asSequence()
        .filter { potentialMirrorIndex ->
            val sideA = ((potentialMirrorIndex - 1)downTo 0).joinToString(separator = "") { this[it] }
            val sideB = (potentialMirrorIndex..lastIndex).joinToString(separator = "") { this[it] }
            sideA.zip(sideB).count { (a, b) -> a != b } == reflectionError
        }
        .singleOrNull()
        ?.toLong()
}

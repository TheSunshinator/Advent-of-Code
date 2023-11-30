package utils

import arrow.core.NonEmptyList
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeSameSizeAs
import io.kotest.matchers.shouldBe
import kotlin.time.measureTimedValue

fun <I, R> runAlgorithm(
    realInput: I,
    testInputs: NonEmptyList<I>,
    part1: ProblemPart<I, R>,
    part2: ProblemPart<I, R>? = null,
): Pair<R, R?> {
    part1.expectedResultsForTests shouldBeSameSizeAs testInputs
    part2?.expectedResultsForTests?.shouldBeSameSizeAs(testInputs)

    return Pair(
        runAlgorithm(
            realInput,
            testInputs.zip(part1.expectedResultsForTests).toMap(),
            part1.algorithm,
        ),
        if (part2 == null) null else runAlgorithm(
            realInput,
            testInputs.zip(part2.expectedResultsForTests).toMap(),
            part2.algorithm,
        )
    )
}

private fun <I, R> runAlgorithm(
    realInput: I,
    testInputs: Map<I, R>,
    algorithm: (I) -> R,
): R {
    assertSoftly {
        testInputs.entries.forEachIndexed { index, (input, expectedResult) ->
            withClue({ "Algorithm failed for input $index" }) {
                algorithm(input) shouldBe expectedResult
            }
        }
    }
    val (result, executionTime) = measureTimedValue { algorithm(realInput) }

    println("Result is $result. Found in $executionTime")

    return result
}

data class ProblemPart<I, R>(
    val expectedResultsForTests: NonEmptyList<R>,
    val algorithm: (I) -> R,
)

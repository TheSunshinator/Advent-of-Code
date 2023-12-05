package year2023.day05

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.intersect
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 5, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(35),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(46),
            algorithm = ::part2,
        ),
    )
}

private fun parse(input: List<String>) = Input(
    seeds = input.first()
        .drop(7)
        .split(' ')
        .map { it.toLong() },
    mappings = input.drop(2).fold(mutableListOf(mutableListOf<Pair<LongRange, Long>>())) { accumulator, line ->
        if (line.isEmpty()) accumulator.add(mutableListOf())
        else if (!line.first().isLetter()) line.splitToSequence(' ')
            .map { it.toLong() }
            .toList()
            .let { (end, start, length) ->
                accumulator.last().add(
                    Pair(
                        (start until (start + length)),
                        end - start,
                    )
                )
            }

        accumulator
    }
)

private fun part1(input: Input): Long {
    return input.seeds.asSequence()
        .map { seed ->
            input.mappings.fold(seed) { location, mapping ->
                val offset = mapping.firstOrNull { location in it.first }?.second ?: 0L
                location + offset
            }
        }
        .min()
}

private fun part2(input: Input): Long {
    val seeds = input.seeds.chunked(2) { (start, length) -> start until (start + length) }
    return input.mappings
        .fold(seeds) { seedsPositions, mapping ->
            seedsPositions.flatMap { seedRange ->
                val newSeedRanges = mapping.mapNotNull { (range, offset) ->
                    (range intersect seedRange)?.to(offset)
                }
                val unmovedSeeds = newSeedRanges.fold(listOf(seedRange)) { unusedRanges, (usedRange) ->
                    unusedRanges.flatMap { unusedRange ->
                        val intersect = unusedRange intersect usedRange
                        if (intersect == null) listOf(unusedRange) else {
                            val startRange = unusedRange.first until usedRange.first
                            val endRange = (usedRange.last + 1)..unusedRange.last
                            listOfNotNull(startRange.takeUnless { it.isEmpty() }, endRange.takeUnless { it.isEmpty() })
                        }
                    }
                }

                newSeedRanges.mapTo(unmovedSeeds.toMutableList()) {
                    (it.first.first + it.second)..(it.first.last + it.second)
                }
            }
        }
        .minOf { it.first }
}

private data class Input(
    val seeds: List<Long>,
    val mappings: List<List<Pair<LongRange, Long>>>,
)

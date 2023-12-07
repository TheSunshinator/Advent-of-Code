package year2023.day07

import arrow.core.NonEmptyList
import arrow.core.identity
import arrow.core.nonEmptyListOf
import arrow.core.toNonEmptyListOrNull
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm

fun main() {
    val (realInput, testInputs) = readInputs(2023, 7)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(6440),
            algorithm = ::part1,
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(5905),
            algorithm = ::part2,
        ),
    )
}

private fun parse(
    input: List<String>,
    jValue: Int,
    type: (List<Int>) -> HandType,
): List<PokerHand> = input.map { line ->
    val (hand, bid) = line.split(' ')
    val cards = hand.map {
        when (it) {
            in '2'..'9' -> it - '2' + 2
            'T' -> 10
            'J' -> jValue
            'Q' -> 12
            'K' -> 13
            else -> 14
        }
    }.toNonEmptyListOrNull()!!
    PokerHand(
        cards,
        type(cards),
        bid.toLong(),
    )
}

private fun part1(input: List<String>): Long = algorithm(
    input = input,
    jValue = 11,
    parseHandType = {
        it.groupingBy(::identity)
            .eachCount()
            .toHandType()
    },
)

private fun algorithm(
    input: List<String>,
    jValue: Int,
    parseHandType: (List<Int>) -> HandType,
): Long {
    return parse(
        input,
        jValue = jValue,
        type = parseHandType,
    )
        .asSequence()
        .sortedWith(
            compareByDescending<PokerHand> { it.type }
                .thenComparator { a, b ->
                    a.cards.asSequence()
                        .zip(b.cards.asSequence()) { cardA, cardB -> cardA.compareTo(cardB) }
                        .first { it != 0 }
                }
        )
        .withIndex()
        .sumOf { (index, hand) -> (index + 1) * hand.bid }
}

private fun part2(input: List<String>): Long = algorithm(
    input = input,
    jValue = 1,
    parseHandType = { hand ->
        val cardCounts = hand.groupingBy(::identity).eachCount()
        val adjustedHand = cardCounts[1]?.let { jokerCount ->
            val bestCard = cardCounts.entries
                .filterNot { it.key == 1 }
                .maxByOrNull { it.value }
                ?.key
            if (bestCard == null) cardCounts
            else cardCounts.asSequence()
                .filterNot { it.key == 1 }
                .associate { (key, count) -> key to if (key == bestCard) count + jokerCount else count }
        } ?: cardCounts
        adjustedHand.toHandType()
    },
)

private fun Map<Int, Int>.toHandType() = when (size) {
    1 -> HandType.Five
    2 -> if (containsValue(4)) HandType.Four else HandType.FullHouse
    3 -> if (containsValue(3)) HandType.Three else HandType.TwoPairs
    4 -> HandType.OnePair
    else -> HandType.One
}
private data class PokerHand(
    val cards: NonEmptyList<Int>,
    val type: HandType,
    val bid: Long,
)

enum class HandType { Five, Four, FullHouse, Three, TwoPairs, OnePair, One }

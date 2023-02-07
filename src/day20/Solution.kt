package day20

import io.kotest.matchers.shouldBe
import utils.cyclical
import utils.readInput

fun main() {
    val testInput = readInput("20", "test_input").map(String::toLong)
    val realInput = readInput("20", "input").map(String::toLong)

    testInput.asSequence()
        .withIndex()
        .run { runMixing(toList()) }
        .map { it.value }
        .let(::computeResult)
        .also(::println) shouldBe 3

    realInput.asSequence()
        .withIndex()
        .run { runMixing(toList()) }
        .map { it.value }
        .let(::computeResult)
        .let(::println)

    testInput.asSequence()
        .map { it * 811589153L }
        .withIndex()
        .run {
            cyclical()
                .take(testInput.size * 10)
                .runMixing(toList())
        }
        .map { it.value }
        .let(::computeResult)
        .also(::println) shouldBe 1623178306L

    realInput.asSequence()
        .map { it * 811589153L }
        .withIndex()
        .run {
            cyclical()
                .take(realInput.size * 10)
                .runMixing(toList())
        }
        .map { it.value }
        .let(::computeResult)
        .let(::println)
}

private fun Sequence<IndexedValue<Long>>.runMixing(initial: List<IndexedValue<Long>>): MutableList<IndexedValue<Long>> {
    return fold(initial.toMutableList()) { accumulator, element ->
        accumulator.apply {
            val startIndex = indexOf(element)
            val newIndex = startIndex + element.value

            removeAt(startIndex)
            val adjustedIndex = if (newIndex > 0) newIndex % size
            else newIndex % size + size
            add(adjustedIndex.toInt(), element)
        }
    }
}

private fun computeResult(input: List<Long>): Long {
    return generateSequence(input.indexOf(0)) { it + 1000 }
        .drop(1).take(3)
        .map { it % input.size }
        .sumOf(input::get)
}

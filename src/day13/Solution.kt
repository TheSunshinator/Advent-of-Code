package day13

import com.google.gson.JsonElement
import io.kotest.matchers.shouldBe
import utils.RecursiveList
import utils.parseRecursiveList
import utils.readInput

fun main() {
    val testInput = readInput("13", "test_input").packetSequence()
    val realInput = readInput("13", "input").packetSequence()

    testInput.chunked(2)
        .withIndex()
        .sumOf { (index, values) ->
            val (first, second) = values
            if (first < second) index + 1 else 0
        }
        .also(::println) shouldBe 13

    realInput.chunked(2)
        .withIndex()
        .sumOf { (index, values) ->
            val (first, second) = values
            if (first < second) index + 1 else 0
        }
        .let(::println)

    val firstDecoderKey = parseRecursiveList("[[2]]", elementParsing = JsonElement::getAsInt)
    val secondDecoderKey = parseRecursiveList("[[6]]", elementParsing = JsonElement::getAsInt)

    testInput.sortedWith(packetOrderComparator)
        .let { packets ->
            val indexOfFirstDecoder = packets.indexOfFirst { it >= firstDecoderKey } + 1
            val indexOfLastDecoder = packets.drop(indexOfFirstDecoder)
                .indexOfFirst { it >= secondDecoderKey }
                .plus(indexOfFirstDecoder + 2)
            indexOfFirstDecoder * indexOfLastDecoder
        }
        .also(::println) shouldBe 140

    realInput.sortedWith(packetOrderComparator)
        .let { packets ->
            val indexOfFirstDecoder = packets.indexOfFirst { it >= firstDecoderKey } + 1
            val indexOfLastDecoder = packets.drop(indexOfFirstDecoder)
                .indexOfFirst { it >= secondDecoderKey }
                .plus(indexOfFirstDecoder + 2)
            indexOfFirstDecoder * indexOfLastDecoder
        }
        .let(::println)

}

private fun List<String>.packetSequence(): Sequence<RecursiveList<Int>> {
    return asSequence()
        .filter(String::isNotBlank)
        .map { parseRecursiveList(it, elementParsing = JsonElement::getAsInt) }
}

private operator fun RecursiveList<Int>.compareTo(other: RecursiveList<Int>): Int {
    return if (this is RecursiveList.Element && other is RecursiveList.Element) value.compareTo(other.value)
    else {
        val o1List = this.let { it as? RecursiveList.NestedList }
            ?: RecursiveList.NestedList(listOf(this))
        val o2List = other.let { it as? RecursiveList.NestedList }
            ?: RecursiveList.NestedList(listOf(other))

        o1List.asSequence()
            .zip(o2List.asSequence())
            .map { (first, second) -> first.compareTo(second) }
            .firstOrNull { it != 0 }
            ?: o1List.size.compareTo(o2List.size)
    }
}

private val packetOrderComparator = Comparator(RecursiveList<Int>::compareTo)

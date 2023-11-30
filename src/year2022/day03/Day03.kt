package year2022.day03

import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("03", "test_input")
    val realInput = readInput("03", "input")

    testInput.asSequence()
        .findDuplicates()
        .mapToPriorities()
        .sum()
        .also(::println) shouldBe 157

    realInput.asSequence()
        .findDuplicates()
        .mapToPriorities()
        .sum()
        .let(::println)

    testInput.asSequence()
        .findBadge()
        .mapToPriorities()
        .sum()
        .also(::println) shouldBe 70

    realInput.asSequence()
        .findBadge()
        .mapToPriorities()
        .sum()
        .let(::println)
}

private fun Sequence<String>.findDuplicates(): Sequence<Char> {
    return map { it to separateCompartmentRegex(it) }
        .map { (input, regex) -> regex.find(input)!! }
        .map { it.groups[1]!!.value.single() }
}

private fun separateCompartmentRegex(input: String): Regex {
    return "\\A.{0,${input.length / 2 - 1}}([a-zA-Z]).*\\1.{0,${input.length / 2 - 1}}\\z".toRegex()
}

private fun Sequence<Char>.mapToPriorities() = map {
    when (it) {
        in 'a'..'z' -> it - 'a' + 1
        else -> it - 'A' + 27
    }
}

private fun Sequence<String>.findBadge() = chunked(3) { (first, second, third) ->
    first.toSet().intersect(second.toSet())
        .intersect(third.toSet())
        .single()
}

package day06

import io.kotest.matchers.shouldBe
import readInput

fun main() {
    val testInput = readInput("06", "test_input").first()
    val realInput = readInput("06", "input").first()

    markerRegex.find(testInput)!!
        .range.last.inc()
        .also(::println) shouldBe 7

    markerRegex.find(realInput)!!
        .range.last.inc()
        .let(::println)

    testInput.windowedSequence(14)
        .indexOfFirst { !(it matches messageRegex) }
        .plus(14)
        .also(::println) shouldBe 19

    realInput.windowedSequence(14)
        .indexOfFirst { !(it matches messageRegex) }
        .plus(14)
        .let(::println)
}

private val markerRegex = "(?!(.).{0,2}\\1)(?!.(.).?\\2)(?!.{2}(.)\\3).{4}".toRegex()
private val messageRegex = ".*?(.).*?\\1.*?".toRegex()
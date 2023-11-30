package year2022.day10

import io.kotest.matchers.shouldBe
import utils.plusOrMinus
import utils.readInput

fun main() {
    val testInput = readInput("10", "test_input")
    val realInput = readInput("10", "input")

    val cycleEnds = 20..220 step 40
    val testRuntime = testInput.asSequence()
        .map(::toInstruction)
        .fold(
            mutableListOf(Register(0, 1, 1)),
            ::computeRegisterHistory
        )
    testRuntime.filter { it.cycle in cycleEnds }
        .sumOf { it.cycle * it.valueDuringCycle }
        .also(::println) shouldBe 13140

    val realRuntime = realInput.asSequence()
        .map(::toInstruction)
        .fold(
            mutableListOf(Register(0, 1, 1)),
            ::computeRegisterHistory
        )
    realRuntime.filter { it.cycle in cycleEnds }
        .sumOf { it.cycle * it.valueDuringCycle }
        .let(::println)

    testRuntime.asSequence()
        .drop(1)
        .chunked(40, ::cycleConsoleLine)
        .forEach(::println)

    println()

    realRuntime.asSequence()
        .drop(1)
        .chunked(40, ::cycleConsoleLine)
        .forEach(::println)
}

private sealed interface Instruction {
    object Noop : Instruction {
        override fun toString() = "Noop"
    }

    data class Add(val quantity: Int) : Instruction {
        override fun toString() = quantity.toString()
    }
}

private fun toInstruction(value: String): Instruction = when (value) {
    "noop" -> Instruction.Noop
    else -> value.takeLastWhile { it != ' ' }.toInt().let(Instruction::Add)
}

data class Register(
    val cycle: Int,
    val valueDuringCycle: Int,
    val valueAfterCycle: Int,
)

private fun computeRegisterHistory(registerHistory: MutableList<Register>, instruction: Instruction) = registerHistory.apply {
    val lastState = last()
    add(
        lastState.copy(
            cycle = lastState.cycle + 1,
            valueDuringCycle = lastState.valueAfterCycle,
        )
    )
    if (instruction is Instruction.Add) add(
        lastState.copy(
            cycle = lastState.cycle + 2,
            valueDuringCycle = lastState.valueAfterCycle,
            valueAfterCycle = lastState.valueAfterCycle + instruction.quantity,
        )
    )
}

private fun cycleConsoleLine(cycle: List<Register>): String {
    return cycle.joinToString(separator = "") {
        if ((it.cycle - 1) % 40 in it.valueDuringCycle.plusOrMinus(1)) "█" else " "
    }
}

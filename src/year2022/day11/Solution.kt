package year2022.day11

import io.kotest.matchers.shouldBe
import utils.readInput

fun main() {
    val testInput = readInput("11", "test_input")
    val realInput = readInput("11", "input")

    val testStartMonkeyList = testInput.buildMonkeyList()
    val realStartMonkeyList = realInput.buildMonkeyList()

    runRounds(20, testStartMonkeyList) { it / 3 }
        .sortedByDescending { it.inspectedItems }
        .let { (first, second) -> first.inspectedItems * second.inspectedItems }
        .also(::println) shouldBe 10605

    runRounds(20, realStartMonkeyList) { it / 3 }
        .sortedByDescending { it.inspectedItems }
        .let { (first, second) -> first.inspectedItems * second.inspectedItems }
        .let(::println)

    val testWorryAdjustment = testStartMonkeyList.asSequence()
        .map { it.testModulo }
        .fold(1, Long::times)
    runRounds(10000, testStartMonkeyList) { it % testWorryAdjustment }
        .sortedByDescending { it.inspectedItems }
        .let { (first, second) -> first.inspectedItems * second.inspectedItems }
        .also(::println) shouldBe 2713310158L


    val realWorryAdjustment = realStartMonkeyList.asSequence()
        .map { it.testModulo }
        .fold(1, Long::times)
    runRounds(10000, realStartMonkeyList) { it % realWorryAdjustment }
        .sortedByDescending { it.inspectedItems }
        .let { (first, second) -> first.inspectedItems * second.inspectedItems }
        .let(::println)
}

private fun List<String>.buildMonkeyList(): List<Monkey> {
    return chunked(7).fold(mutableListOf()) { monkeys, data ->
        val operationComputation: Long.(Long) -> Long = if (data[2][23] == '+') Long::plus else Long::times
        val operationSecondOperand = data[2].takeLastWhile { it != ' ' }.toLongOrNull()
        val testModulo = data[3].takeLastWhile { it != ' ' }.toLong()
        val testTrue = data[4].takeLastWhile { it != ' ' }.toInt()
        val testFalse = data[5].takeLastWhile { it != ' ' }.toInt()
        monkeys.apply {
            add(
                Monkey(
                    items = data[1].removePrefix("  Starting items: ").split(", ").map(String::toLong),
                    operation = operationSecondOperand
                        ?.let { secondOperand -> { it.operationComputation(secondOperand) } }
                        ?: { it * it },
                    ifTrue = testTrue,
                    ifFalse = testFalse,
                    testModulo = testModulo,
                )
            )
        }
    }
}

private data class Monkey(
    val items: List<Long>,
    val operation: (Long) -> Long,
    val ifTrue: Int,
    val ifFalse: Int,
    val testModulo: Long,
    val inspectedItems: Long = 0,
)

private fun runRounds(
    roundCount: Long,
    monkeyList: List<Monkey>,
    worryAdjustment: (Long) -> Long,
): List<Monkey> {
    return (0 until roundCount).fold(monkeyList) { roundState, _ ->
        roundState.indices.fold(roundState) { monkeysState, monkeyTurn ->
            val subject = monkeysState[monkeyTurn]

            val (toTrue, toFalse) = subject.items.asSequence()
                .map(subject.operation)
                .map(worryAdjustment)
                .partition { it % subject.testModulo == 0L }

            monkeysState.toMutableList().apply {
                this[monkeyTurn] = subject.copy(
                    items = emptyList(),
                    inspectedItems = subject.inspectedItems + subject.items.size,
                )
                this[subject.ifTrue] = this[subject.ifTrue].run {
                    copy(items = items + toTrue)
                }
                this[subject.ifFalse] = this[subject.ifFalse].run {
                    copy(items = items + toFalse)
                }
            }
        }
    }
}
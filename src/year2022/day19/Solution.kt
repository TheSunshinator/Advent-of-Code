package year2022.day19

import io.kotest.matchers.shouldBe
import java.util.PriorityQueue
import kotlin.math.ceil
import kotlin.math.max
import utils.readInput

fun main() {
    val testInput = readInput("19", "test_input").let(::parseBlueprints)
    val realInput = readInput("19", "input").let(::parseBlueprints)

    testInput.associateWith { it.maxGeodeProduced(24) }
        .asSequence()
        .sumOf { (blueprint, maxGeodeProduced) -> blueprint.identifier * maxGeodeProduced }
        .also(::println) shouldBe 33

    realInput.associateWith { it.maxGeodeProduced(24) }
        .asSequence()
        .sumOf { (blueprint, maxGeodeProduced) -> blueprint.identifier * maxGeodeProduced }
        .let(::println)

    testInput.asSequence()
        .take(3)
        .map { it.maxGeodeProduced(32) }
        .reduce(Int::times)
        .also(::println) shouldBe (56 * 62)

    realInput.asSequence()
        .take(3)
        .map { it.maxGeodeProduced(32) }
        .reduce(Int::times)
        .let(::println)
}

private fun parseBlueprints(input: List<String>): List<Blueprint> {
    return input.asSequence()
        .mapNotNull(parsingRegex::matchEntire)
        .map { it.destructured }
        .mapTo(mutableListOf()) { (identifier, oreRobotCost, clayRobotCost, obsidianRobotOreCost, obsidianRobotClayCost, geodeRobotOreCost, geodeRobotObsidianCost) ->
            Blueprint(
                identifier.toInt(),
                robotCost = buildMap {
                    this[Rock.ORE] = mapOf(Rock.ORE to oreRobotCost.toInt())
                    this[Rock.CLAY] = mapOf(Rock.ORE to clayRobotCost.toInt())
                    this[Rock.OBSIDIAN] = mapOf(
                        Rock.ORE to obsidianRobotOreCost.toInt(),
                        Rock.CLAY to obsidianRobotClayCost.toInt(),
                    )
                    this[Rock.GEODE] = mapOf(
                        Rock.ORE to geodeRobotOreCost.toInt(),
                        Rock.OBSIDIAN to geodeRobotObsidianCost.toInt(),
                    )
                },
            )
        }
}

private data class Blueprint(
    val identifier: Int,
    val robotCost: Map<MiningRobot, Map<Rock, Int>>,
) {
    val maxCosts = Rock.values().associateWith { rock ->
        robotCost.asSequence()
            .flatMap { it.value.asSequence() }
            .filter { it.key == rock }
            .maxOfOrNull { it.value }
            ?: Int.MAX_VALUE
    }
}

private enum class Rock {
    ORE, CLAY, OBSIDIAN, GEODE
}
private typealias MiningRobot = Rock

private data class State(
    val minutesLeft: Int,
    val robotCount: Map<MiningRobot, Int> = mapOf(Rock.ORE to 1),
    val rockCount: Map<Rock, Int> = emptyMap(),
) {
    val potentialGeodeCount = rockCount.getOrDefault(Rock.GEODE, 0) +
        robotCount.getOrDefault(Rock.GEODE, 0) * minutesLeft +
        minutesLeft.let { it * it - 1 } / 2
}

private val parsingRegex =
    "Blueprint (\\d+): Each ore robot costs (\\d+) ore. Each clay robot costs (\\d+) ore. Each obsidian robot costs (\\d+) ore and (\\d+) clay. Each geode robot costs (\\d+) ore and (\\d+) obsidian.".toRegex()

private fun Blueprint.maxGeodeProduced(minutesAvailable: Int): Int {
    val priorityQueue = PriorityQueue(compareByDescending(State::potentialGeodeCount))
        .apply { add(State(minutesAvailable)) }

    return generateSequence(priorityQueue::poll)
        .filterOutInsufficientPotential()
        .onEach { priorityQueue.addAll(it.potentialNextStates(this)) }
        .maxOf {
            it.rockCount.getOrDefault(Rock.GEODE, 0) +
                it.robotCount.getOrDefault(Rock.GEODE, 0) * it.minutesLeft
        }
}

private fun State.potentialNextStates(blueprint: Blueprint): Set<State> {
    return blueprint.robotCost
        .asSequence()
        .filter { (miningRobot, cost) ->
            cost.all { (rock, _) -> robotCount.getOrDefault(rock, 0) > 0 }
                && robotCount.getOrDefault(miningRobot, 0) < blueprint.maxCosts.getValue(miningRobot)
        }
        .mapNotNullTo(mutableSetOf()) { (robot, rockCost) ->
            val timeToSaveToBuild = rockCost.maxOf { (rock, cost) ->
                val currentStash = rockCount.getOrDefault(rock, 0)
                if (currentStash >= cost) 0
                else ceil((cost - currentStash) / robotCount.getValue(rock).toDouble()).toInt()
            } + 1
            if (timeToSaveToBuild < minutesLeft) State(
                minutesLeft = minutesLeft - timeToSaveToBuild,
                robotCount = robotCount.toMutableMap().apply {
                    compute(robot) { _, count -> count?.plus(1) ?: 1 }
                },
                rockCount = Rock.values().associateWith { rock ->
                    rockCount.getOrDefault(rock, 0) +
                        timeToSaveToBuild * robotCount.getOrDefault(rock, 0) -
                        rockCost.getOrDefault(rock, 0)
                },
            ) else null
        }
}

private fun Sequence<State>.filterOutInsufficientPotential() = object : Sequence<State> {
    private var maxGeode = -1

    override fun iterator(): Iterator<State> {
        return this@filterOutInsufficientPotential.takeWhile { it.potentialGeodeCount > maxGeode }
            .onEach { maxGeode = max(maxGeode, it.rockCount.getOrDefault(Rock.GEODE, 0)) }
            .iterator()
    }
}

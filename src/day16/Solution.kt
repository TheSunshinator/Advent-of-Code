package day16

import io.kotest.matchers.shouldBe
import java.util.LinkedList
import kotlin.math.ceil
import utils.Graph
import utils.Identifiable
import utils.Identifier
import utils.cartesianProduct
import utils.combinations
import utils.findShortestRoute
import utils.readInput

fun main() {
    val testInput = readInput("16", "test_input").parseGraph().simplify()
    val realInput = readInput("16", "input").parseGraph().simplify()

    findOptimalRoute(testInput, 30)
        .also(::println) shouldBe 1651
    findOptimalRoute(realInput, 30)
        .let(::println)

    findOptimalTeamWork(testInput)
        .also(::println) shouldBe 1707
    findOptimalTeamWork(realInput)
        .let(::println)
}

private fun List<String>.parseGraph(): Graph<Valve> {
    val valveToNeighborIds = asSequence()
        .mapNotNull(parsingRegex::matchEntire)
        .map { it.destructured }
        .associate { (identifier, flowRate, multipleNeighbors, singleNeighbor) ->
            Valve(
                identifier = Identifier(identifier),
                flowRate = flowRate.toInt()
            ) to identifierRegex.findAll(multipleNeighbors.ifEmpty { singleNeighbor })
                .map { it.value }
                .mapTo(mutableSetOf(), ::Identifier)
        }
    return Graph(
        nodes = valveToNeighborIds.keys,
        neighbors = valveToNeighborIds.mapKeys { it.key.identifier },
        costs = valveToNeighborIds.flatMap { (valve, neighbors) ->
            neighbors.map { valve.identifier to it to 1L }
        }.toMap()
    )
}

private fun Graph<Valve>.simplify(): Graph<Valve> {
    val valveWithFlows = nodes.filterTo(mutableSetOf()) { it.flowRate > 0 || it.identifier.value == "AA" }
    return Graph(
        nodes = valveWithFlows,
        neighbors = valveWithFlows.associate { valve ->
            valve.identifier to valveWithFlows.asSequence()
                .filterNot { it.identifier == valve.identifier }
                .mapTo(mutableSetOf()) { it.identifier }
        },
        costs = valveWithFlows.asSequence()
            .map { it.identifier }
            .let { it.cartesianProduct(it) }
            .mapNotNull { (start, end) -> findShortestRoute(this, start, end) }
            .associate { metadata ->
                metadata.route.let { it.first() to it.last() } to metadata.cost.toLong()
            },
    )
}

private val parsingRegex =
    "\\AValve ([A-Z]{2}) has flow rate=(\\d+)(?:; tunnels lead to valves ([A-Z]{2}(?:, [A-Z]{2})+)|; tunnel leads to valve ([A-Z]{2}))\\z".toRegex()
private val identifierRegex = "[A-Z]{2}".toRegex()

private data class Valve(
    override val identifier: Identifier,
    val flowRate: Int,
) : Identifiable

private fun findOptimalRoute(
    input: Graph<Valve>,
    maxCycles: Int,
): Int {
    val (start, toVisit) = input.nodes.partition { it.identifier.value == "AA" }
    val metadataQueue = LinkedList<SearchMetadata>()
        .apply { add(SearchMetadata(start, maxCycles, emptyMap(), toVisit.toSet())) }

    return generateSequence { metadataQueue.poll() }
        .flatMap { currentSearchMetadata ->
            val origin = currentSearchMetadata.path.last()
            currentSearchMetadata.unreleasedValves
                .asSequence()
                .mapNotNull { destination ->
                    val movementCost = input.cost(origin, destination).toInt()
                    val releaseCycle = currentSearchMetadata.currentCycle + movementCost + 1

                    if (releaseCycle >= maxCycles) null
                    else SearchMetadata(
                        path = currentSearchMetadata.path + destination,
                        maxCycles = maxCycles,
                        releaseCycles = currentSearchMetadata.releaseCycles + (destination to releaseCycle),
                        unreleasedValves = currentSearchMetadata.unreleasedValves - destination,
                    )
                }
                .ifEmpty { sequenceOf(currentSearchMetadata.copy(unreleasedValves = emptySet())) }
        }
        .filter { routeMetadata ->
            val isDone = routeMetadata.unreleasedValves.isEmpty()
            if (!isDone) metadataQueue.add(routeMetadata)
            isDone
        }
        .maxOf { it.totalReleased }
}

private data class SearchMetadata(
    val path: List<Valve>,
    val maxCycles: Int,
    val releaseCycles: Map<Valve, Int>,
    val unreleasedValves: Set<Valve>,
) {
    val totalReleased = releaseCycles.asSequence().fold(0) { total, (valve, cycleReleased) ->
        val valveTotalRelease = (maxCycles - cycleReleased) * valve.flowRate
        total + valveTotalRelease
    }
    val currentCycle = releaseCycles.getOrDefault(path.last(), 0)
}

private fun findOptimalTeamWork(input: Graph<Valve>): Int {
    val start = input.nodes.first { it.identifier.value == "AA" }
    val maxNodeCount = ceil(input.nodes.size / 2.0).toInt()
    val minNodeCount = maxNodeCount / 2

    return input.nodes
        .filterNot { it == start }
        .run {
            (minNodeCount..maxNodeCount).asSequence()
                .flatMap { size -> combinations(size) }
        }
        .map { myValves ->
            val associateValves = input.nodes - myValves.toSet() + start
            input.copy(nodes = myValves + start) to input.copy(nodes = associateValves)
        }
        .maxOf { (myValves, assistantValve) ->
            findOptimalRoute(myValves, 26) + findOptimalRoute(assistantValve, 26)
        }
}

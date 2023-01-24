package utils

data class Graph<T : Identifiable>(
    val nodes: Set<T>,
    val neighbors: Map<Identifier, Set<Identifier>>,
    val costs: Map<Pair<Identifier, Identifier>, Long>,
) {
    val edges: Set<Pair<Identifier, Identifier>> by lazy {
        neighbors.flatMapTo(mutableSetOf()) { (start, destinations) ->
            destinations.asSequence().map { start to it }
        }
    }

    fun cost(from: T, to: T) = costs.getOrDefault(from.identifier to to.identifier, 0L)
}

fun <T : Identifiable> Graph<T>.depthFirstSearch(start: T, filterNeighbors: (path: List<T>, neighbors: Set<T>) -> Set<T>): Sequence<List<T>> {
    return depthFirstSearchInternal(listOf(start), filterNeighbors)
}

private fun <T : Identifiable> Graph<T>.depthFirstSearchInternal(path: List<T>, filterNeighbors: (path: List<T>, neighbors: Set<T>) -> Set<T>): Sequence<List<T>> {
    return neighbors[path.last().identifier]
        .orEmpty()
        .mapTo(mutableSetOf()) { identifier -> nodes.first { it.identifier == identifier } }
        .let { filterNeighbors(path, it) }
        .asSequence()
        .map { path + it }
        .flatMap { depthFirstSearchInternal(it, filterNeighbors) }
        .plus(sequenceOf(path))
}

fun <T : Identifiable> Graph<T>.breadthFirstSearch(start: T, filterNeighbors: (path: List<T>, neighbors: Set<T>) -> Set<T>): Sequence<List<T>> {
    return generateSequence(listOf(listOf(start))) { previousPaths ->
        previousPaths.flatMap { path ->
            val neighbors = neighbors[path.last().identifier].orEmpty()
            if (neighbors.isEmpty()) emptyList()
            else filterNeighbors(
                path,
                neighbors.mapTo(mutableSetOf()) { identifier -> nodes.first { it.identifier == identifier } }
            ).map { path + it }
        }
    }.flatten()
}

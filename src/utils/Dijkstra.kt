package utils

import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import java.util.PriorityQueue

fun <T> findShortestRoute(
    map: List<List<T>>,
    start: Point,
    end: Point,
    includeDiagonalRoutes: Boolean = false,
    movementCost: (movementStart: MapDetails<T>, movementEnd: MapDetails<T>) -> Int?,
): ShortestRouteMetadata<Point>? {
    val unvisitedPoints = map.coordinates().toMutableSet()
    val priorityQueue = PriorityQueue<ShortestRouteMetadata<Point>> { a, b -> a.cost - b.cost }
        .apply { add(ShortestRouteMetadata(nonEmptyListOf(start), 0)) }

    return generateSequence { priorityQueue.poll() }
        .onEach { unvisitedPoints.remove(it.route.last()) }
        .flatMap { currentRouteMetadata ->
            currentRouteMetadata.currentPosition
                .neighbors(includeDiagonal = includeDiagonalRoutes)
                .filter { it in unvisitedPoints }
                .associateWith {
                    movementCost(
                        map.detailsAt(currentRouteMetadata.currentPosition),
                        map.detailsAt(it),
                    )
                }
                .mapNotNull { (movementEnd, cost) ->
                    if (cost == null) null
                    else ShortestRouteMetadata(
                        route = currentRouteMetadata.route + movementEnd,
                        cost = currentRouteMetadata.cost + cost,
                    )
                }
                .groupBy { it.cost }
                .minByOrNull { it.key }?.value
                .orEmpty()
        }
        .filter { newPath -> priorityQueue.none { it.currentPosition == newPath.currentPosition } }
        .onEach(priorityQueue::add)
        .firstOrNull { it.currentPosition == end }
}

data class ShortestRouteMetadata<T>(
    val route: NonEmptyList<T>,
    val cost: Int,
) {
    val currentPosition
        get() = route.last()
}

fun findShortestRoute(
    graph: Graph<out Identifiable>,
    start: Identifier,
    end: Identifier,
): ShortestRouteMetadata<Identifier>? {
    val unvisitedNodes: MutableSet<Identifiable> = graph.nodes.toMutableSet()
    val priorityQueue = PriorityQueue<ShortestRouteMetadata<Identifier>> { a, b -> a.cost - b.cost }
        .apply { add(ShortestRouteMetadata(nonEmptyListOf(start), 0)) }

    return generateSequence { priorityQueue.poll() }
        .onEach { subject ->
            unvisitedNodes.first { it.identifier == subject.currentPosition }
                .let(unvisitedNodes::remove)
        }
        .flatMap { currentRouteMetadata ->
            currentRouteMetadata.currentPosition
                .let(graph.neighbors::getValue)
                .filter { neighbor -> unvisitedNodes.any { it.identifier == neighbor } }
                .associateWith { graph.costs.getValue(currentRouteMetadata.currentPosition to it) }
                .map { (movementEnd, cost) ->
                    ShortestRouteMetadata(
                        route = currentRouteMetadata.route + movementEnd,
                        cost = currentRouteMetadata.cost + cost.toInt(),
                    )
                }
                .groupBy { it.cost }
                .minByOrNull { it.key }?.value
                .orEmpty()
        }
        .filter { newPath -> priorityQueue.none { it.currentPosition == newPath.currentPosition } }
        .onEach(priorityQueue::add)
        .firstOrNull { it.currentPosition == end }
}

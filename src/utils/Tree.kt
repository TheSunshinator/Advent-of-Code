package utils

data class Tree<T>(
    val nodeValue: T,
    val children: List<Tree<T>>,
) {
    companion object {
        fun <T> Leaf(nodeValue: T) = Tree(nodeValue, emptyList())
    }
}

fun <T, R> Tree<T>.fold(operation: (nodeValue: T, childrenValues: List<R>) -> R): R {
    return operation(nodeValue, children.map { it.fold(operation) })
}

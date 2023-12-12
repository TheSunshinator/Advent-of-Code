package utils

fun <T, R> withMemoization(function: suspend DeepRecursiveScope<T, R>.(T) -> R): DeepRecursiveFunction<T, R> {
    val memoization = mutableMapOf<T, R>()
    return DeepRecursiveFunction {  parameter ->
        memoization.getOrPut(parameter) { function(parameter) }
    }
}

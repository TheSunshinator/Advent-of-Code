package utils

fun <T> Sequence<T>.findCycle(): Cycle<T> {
    return runningFold(listOf(), List<T>::plus)
        .mapNotNull { path ->
            val loopStart = path.lastOrNull()?.let(path::indexOf)
            if (path.lastIndex == loopStart || loopStart == null) null else Cycle(path, loopStart)
        }
        .first()
}

data class Cycle<T>(
    val steps: List<T>,
    val loopStart: Int,
) {
    val loopEnd = steps.lastIndex - 1
    val loopSize = loopEnd - loopStart + 1
}

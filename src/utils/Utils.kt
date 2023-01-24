package utils

import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(day: String, name: String): List<String> = File("src/day$day", "$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

inline fun <T, U, V> Pair<T, U>.mapFirst(transform: (T) -> V) = transform(first) to second
inline fun <T, U, V> Pair<T, U>.mapSecond(transform: (U) -> V) = first to transform(second)

infix fun Int.iterateTo(other: Int) = if (this <= other) rangeTo(other) else downTo(other)

fun parseLongSequence(input: List<String>) = input.first().splitToSequence(",").map(String::toLong)

fun <T> List<List<T>>.coordinates() = indices.asSequence().flatMap { i -> this[i].indices.map { j -> Point(i, j) } }
infix fun IntRange.cartesianProduct(other: IntRange) = asSequence().flatMap { i -> other.map { j -> Point(i, j) } }
infix fun <T, R> Iterable<T>.cartesianProduct(other: Iterable<R>) = asSequence().flatMap { i -> other.map { j -> i to j } }
infix fun <T, R> Sequence<T>.cartesianProduct(other: Sequence<R>) = flatMap { i -> other.map { j -> i to j } }

infix fun Int.plusOrMinus(n: Int) = minus(n)..plus(n)

operator fun <T> (T.() -> Boolean).not(): T.() -> Boolean = { !this@not() }

val IntRange.size
    get() = (last - first) / step

fun <T> Collection<T>.combinations(size: Int): Sequence<Set<T>> {
    return if (size == 1) asSequence().map(::setOf)
    else asSequence()
        .runningFold(toSet(), Set<T>::minus)
        .drop(1)
        .zip(asSequence()) { toCombine, current -> current to toCombine }
        .flatMap { (current, toCombine) ->
            toCombine.combinations(size - 1)
                .map { it + current }
        }
}

fun <T> Collection<T>.permutations(size: Int): Sequence<List<T>> {
    return if (size == 1) asSequence().map(::listOf)
    else asSequence()
        .map { it to this - it }
        .flatMap { (current, toPermute) ->
            toPermute.permutations(size - 1)
                .map { it + current }
        }
}
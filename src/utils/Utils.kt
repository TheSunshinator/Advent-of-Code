package utils

import arrow.core.NonEmptyList
import arrow.core.identity
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Reads lines from the given input txt file.
 */
fun readInput(day: String, name: String, year: Int = 2022): List<String> {
    return File("src/year$year/day$day", "$name.txt").readLines()
}

fun readInputs(
    year: Int,
    day: Int,
    vararg otherTestFileNames: String,
): Pair<List<String>, NonEmptyList<List<String>>> {
    val formattedDay = String.format("%02d", day)
    return File("src/year$year/day$formattedDay", "input.txt").readLines() to NonEmptyList(
        head = File("src/year$year/day$formattedDay", "test_input.txt").readLines(),
        tail = otherTestFileNames.asSequence()
            .map { File("src/year$year/day$formattedDay", "$it.txt") }
            .map { it.readLines() }
            .toList()
    )
}

fun <T> readInputs(
    year: Int,
    day: Int,
    vararg otherTestFileNames: String,
    transform: (List<String>) -> T,
): Pair<T, NonEmptyList<T>> {
    val (realInput, testInputs) = readInputs(year, day, *otherTestFileNames)
    val mappedTestInputs = testInputs.map(transform)
    return Pair(transform(realInput), mappedTestInputs)
}

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
infix fun <T, R> Iterable<T>.cartesianProduct(other: Iterable<R>) =
    asSequence().flatMap { i -> other.map { j -> i to j } }

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

// (a, b) == (b, a)
fun <T> Sequence<T>.combinations(size: Int): Sequence<Set<T>> {
    return if (size == 1) map(::setOf)
    else runningFold(toSet(), Set<T>::minus)
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

fun <T> Sequence<T>.cyclical() = generateSequence(this) { this }.flatten()
fun <T> Sequence<T>.continuing() = object : Sequence<T> {
    private val continuingIterator = this@continuing.iterator()
    override fun iterator() = continuingIterator
}

fun greaterCommonDivisor(a: Int, vararg values: Int): Int = when (values.size) {
    0 -> a
    1 -> values.single().let { b -> greaterCommonDivisor(b, a % b) }
    else -> greaterCommonDivisor(
        greaterCommonDivisor(a, values.first()),
        *IntArray(values.size - 1) { values[it + 1] }
    )
}

fun leastCommonMultiple(a: Int, vararg values: Int): Int = when (values.size) {
    0 -> a
    1 -> values.single().let { b -> a * b / greaterCommonDivisor(a, b) }
    else -> leastCommonMultiple(
        leastCommonMultiple(a, values.first()),
        *IntArray(values.size - 1) { values[it + 1] }
    )
}

fun <T : Comparable<T>> Iterable<T>.minMaxOrNull(): Pair<T, T>? {
    val iterator = iterator()
    return if (iterator.hasNext()) iterator.asSequence().fold(iterator.next().let { it to it }) { (min, max), value ->
        if (value < min) value else {
            min
        } to if (max < value) value else max
    } else null
}

fun <T : Comparable<T>> Iterable<T>.minMax(): Pair<T, T> = minMaxOrNull()!!

fun <T, R : Comparable<R>> Iterable<T>.minMaxOfOrNull(selector: (T) -> R): Pair<R, R>? {
    val iterator = iterator()
    return if (iterator.hasNext()) iterator.asSequence()
        .map(selector)
        .fold(iterator.next().let(selector).let { it to it }) { (min, max), value ->
            if (value < min) value else { min } to if (max < value) value else max
        }
    else null
}

fun <T, R : Comparable<R>> Iterable<T>.minMaxOf(selector: (T) -> R): Pair<R, R> = minMaxOfOrNull(selector)!!

infix fun LongRange.intersect(other: LongRange): LongRange? = when {
    other.first in this && other.last in this -> other
    first in other && last in other -> this
    first in other -> first..other.last
    last in other -> other.first..last
    else -> null
}

fun Sequence<Int>.product() = fold(1L) { product, value -> value * product }
fun Sequence<*>.countLong() = fold(0L) { count, _ -> count + 1 }
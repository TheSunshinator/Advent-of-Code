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

fun Int.plusOrMinus1() = minus(1)..plus(1)
package utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement

fun <T> parseRecursiveList(
    input: String,
    elementParsing: (JsonElement) -> T,
): RecursiveList<T> {
    return Gson().fromJson(input, JsonArray::class.java).toRecursiveList(elementParsing)
}

private fun <T> JsonElement.toRecursiveList(transform: (JsonElement) -> T): RecursiveList<T> {
    return if (this is JsonArray) RecursiveList.NestedList(map { it.toRecursiveList(transform) })
    else RecursiveList.Element(transform(this))
}

sealed interface RecursiveList<T> {
    data class Element<T>(val value: T) : RecursiveList<T>
    class NestedList<T>(content: List<RecursiveList<T>>) : RecursiveList<T>, List<RecursiveList<T>> by content
}

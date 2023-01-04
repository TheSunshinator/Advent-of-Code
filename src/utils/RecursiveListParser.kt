package utils

fun <T> parseRecursiveList(
    input: String,
    startIndex: Int = 0,
    elementParsing: (String) -> T,
): RecursiveList<T> = parseRecursiveListInternal(IndexedValue(startIndex, input), elementParsing).value

private fun <T> parseRecursiveListInternal(
    input: IndexedValue<String>,
    elementParsing: (String) -> T,
): IndexedValue<RecursiveList<T>> {
    return when {
        input.currentCharacter != '[' -> input.value.asSequence()
            .drop(input.index)
            .takeWhile { it != ',' && it != ']' }
            .joinToString(separator = "")
            .let {
                IndexedValue(
                    index = input.index + it.length,
                    value = RecursiveList.Element(elementParsing(it)),
                )
            }

        input.nextCharacter == ']' -> IndexedValue(
            index = input.index + 2,
            value = RecursiveList.NestedList(emptyList()),
        )

        else -> generateSequence(
            IndexedValue(
                input.index,
                emptyList<RecursiveList<T>>(),
            )
        ) { (currentParsingIndex, currentList) ->
            if (input.value[currentParsingIndex] == ']') null
            else {
                val (nextIndex, nextElement) = parseRecursiveListInternal(
                    IndexedValue(
                        currentParsingIndex + 1,
                        input.value
                    ),
                    elementParsing
                )
                IndexedValue(nextIndex, currentList + nextElement)
            }
        }
            .last()
            .let { (lastIndex, content) ->
                IndexedValue(
                    index = lastIndex + 1,
                    value = RecursiveList.NestedList(content),
                )
            }
    }
}

private val IndexedValue<String>.currentCharacter
    get() = value[index]
private val IndexedValue<String>.nextCharacter
    get() = value[index + 1]

sealed interface RecursiveList<T> {
    data class Element<T>(val value: T) : RecursiveList<T>
    class NestedList<T>(content: List<RecursiveList<T>>) : RecursiveList<T>, List<RecursiveList<T>> by content
}

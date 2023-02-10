package day21

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.identity
import arrow.core.left
import arrow.core.right
import io.kotest.matchers.shouldBe
import utils.Identifier
import utils.Tree
import utils.asIdentifier
import utils.fold
import utils.readInput

fun main() {
    val testInput = readInput("21", "test_input").parse().toTree()
    val realInput = readInput("21", "input").parse().toTree()

    testInput.fold { node, childrenValues: List<Long> ->
        node.value.fold(
            ifRight = ::identity,
            ifLeft = childrenValues::reduce
        )
    }
        .also(::println) shouldBe 152

    realInput.fold { node, childrenValues: List<Long> ->
        node.value.fold(
            ifRight = ::identity,
            ifLeft = childrenValues::reduce
        )
    }
        .let(::println)

    testInput.reduce()
        .computeHumanValue()
        .also(::println) shouldBe 301

    realInput.reduce()
        .computeHumanValue()
        .let(::println)
}

private fun List<String>.parse(): Map<Identifier, Either<OperationDetails, Long>> {
    return asSequence()
        .map(parsingRegex::matchEntire)
        .map { it!!.destructured }
        .associate { (identifier, value, firstOperand, operation, secondOperand) ->
            identifier.asIdentifier() to if (value.isEmpty()) OperationDetails(
                firstOperand.asIdentifier(),
                value = when (operation) {
                    "+" -> Operation.Addition
                    "-" -> Operation.Subtraction
                    "*" -> Operation.Multiplication
                    else -> Operation.Division
                },
                secondOperand.asIdentifier(),
            ).left() else value.toLong().right()
        }
}

private data class OperationDetails(
    val firstOperandIdentifier: Identifier,
    val value: Operation,
    val secondOperandIdentifier: Identifier,
)

private val parsingRegex = "([a-z]+): (?:(\\d+)|([a-z]+) ([-+*/]) ([a-z]+))".toRegex()

private fun Map<Identifier, Either<OperationDetails, Long>>.toTree(nodeIdentifier: Identifier = Identifier("root")): Tree<Node> {
    return getValue(nodeIdentifier).fold(
        ifRight = {
            Tree(
                nodeValue = Node(nodeIdentifier, it.right()),
                children = emptyList()
            )
        },
        ifLeft = {
            Tree(
                nodeValue = Node(nodeIdentifier, it.value.left()),
                children = listOf(
                    toTree(it.firstOperandIdentifier),
                    toTree(it.secondOperandIdentifier),
                )
            )
        }
    )
}

private data class Node(
    val identifier: Identifier,
    val value: Either<Operation, Long>,
)

private fun Tree<Node>.reduce() = fold { node, childrenValues: List<Tree<Node>> ->
    node.value.fold(
        ifRight = { Tree(node, childrenValues) },
        ifLeft = { operation ->
            childrenValues.takeIf { children -> children.none { it.nodeValue.identifier == Identifier("humn") } }
                ?.asSequence()
                ?.map { it.nodeValue.value }
                ?.reduce { first, second ->
                    first.flatMap { firstOperand ->
                        second.map { secondOperand -> operation(firstOperand, secondOperand) }
                    }
                }
                ?.fold(
                    ifRight = { Tree(Node(node.identifier, it.right()), emptyList()) },
                    ifLeft = { Tree(node, childrenValues) }
                )
                ?: Tree(node, childrenValues)
        }
    )
}

private fun Tree<Node>.computeHumanValue(): Long {
    return generateSequence(
        seedFunction = {
            val result = children.firstNotNullOf { it.nodeValue.value as? Either.Right }.value
            val formula = children.first { it.nodeValue.value is Either.Left }
            result to formula
        },
        nextFunction = { (result, formula) ->
            if (formula.nodeValue.identifier == Identifier("humn")) null
            else {
                val (firstOperand, secondOperand) = formula.children
                val operation = formula.nodeValue.value.let { it as Either.Left<Operation> }.value

                if (firstOperand.nodeValue.identifier == Identifier("humn")
                    || firstOperand.nodeValue.value is Either.Left<Operation>
                ) with(operation) {
                    check(secondOperand.nodeValue.value is Either.Right<Long>)
                    result.resultsOfOperatingWith(secondOperand.nodeValue.value.value) to firstOperand
                } else with(operation) {
                    check(firstOperand.nodeValue.value is Either.Right<Long>)
                    result.resultsOfOperatingOn(firstOperand.nodeValue.value.value) to secondOperand
                }
            }
        }
    )
        .last()
        .first
}

private sealed interface Operation : (Long, Long) -> Long {
    fun Long.resultsOfOperatingOn(a: Long): Long // R = a + x
    fun Long.resultsOfOperatingWith(b: Long): Long // R = x + b

    object Addition : Operation {
        override fun invoke(a: Long, b: Long) = a + b
        override fun Long.resultsOfOperatingOn(a: Long) = minus(a)
        override fun Long.resultsOfOperatingWith(b: Long) = minus(b)
    }

    object Subtraction : Operation {
        override fun invoke(a: Long, b: Long) = a - b
        override fun Long.resultsOfOperatingOn(a: Long) = a - this
        override fun Long.resultsOfOperatingWith(b: Long) = plus(b)
    }

    object Multiplication : Operation {
        override fun invoke(a: Long, b: Long) = a * b
        override fun Long.resultsOfOperatingOn(a: Long) = div(a)
        override fun Long.resultsOfOperatingWith(b: Long) = div(b)
    }

    object Division : Operation {
        override fun invoke(a: Long, b: Long) = a / b
        override fun Long.resultsOfOperatingOn(a: Long) = a / this
        override fun Long.resultsOfOperatingWith(b: Long) = times(b)
    }
}

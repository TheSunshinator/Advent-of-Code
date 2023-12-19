package year2023.day19

import arrow.core.nonEmptyListOf
import utils.ProblemPart
import utils.readInputs
import utils.runAlgorithm
import utils.size

fun main() {
    val (realInput, testInputs) = readInputs(2023, 19, transform = ::parse)

    runAlgorithm(
        realInput = realInput,
        testInputs = testInputs,
        part1 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(19114),
            algorithm = { (parts, workflow) -> part1(parts, workflow) },
        ),
        part2 = ProblemPart(
            expectedResultsForTests = nonEmptyListOf(167409079868000),
            algorithm = { (_, workflow) -> part2(workflow) },
        ),
    )
}

private fun parse(input: List<String>): Pair<List<Map<String, Long>>, Map<String, List<Rule>>> {
    val workflow = input.asSequence()
        .takeWhile { it.isNotBlank() }
        .mapNotNull(parseWorkflowRegex::matchEntire)
        .map { it.groupValues }
        .associate { (_, nodeIdentifier, rules) ->
            nodeIdentifier to parseConditionRegex.findAll(rules)
                .map { it.groupValues }
                .map { (_, attribute, operation, threshold, destinationIdentifier) ->
                    Rule(
                        attribute = attribute,
                        isGreaterThan = operation == ">",
                        threshold = threshold.toLongOrNull(),
                        destinationIdentifier = destinationIdentifier,
                    )
                }
                .toList()
        }
    val parts = input.takeLastWhile { it.isNotBlank() }
        .map { line ->
            parsePartRegex.findAll(line).associate { match ->
                match.groupValues[1] to match.groupValues[2].toLong()
            }
        }

    return parts to workflow
}

private val parseWorkflowRegex = "(.+?)\\{(.+)\\}".toRegex()
private val parseConditionRegex = "(?:([xmas])([<>])(\\d+):)?([^,]+)".toRegex()
private val parsePartRegex = "([xmas])=(\\d+)".toRegex()

private fun part1(parts: List<Map<String, Long>>, workflow: Map<String, List<Rule>>): Long {
    val rules = workflow.mapValues { (_, rules) ->
        rules.asSequence()
            .map { rule ->
                if (rule.threshold == null) {
                    { _: Map<String, Long> -> rule.destinationIdentifier }
                } else {
                    val matchesCondition: (Long) -> Boolean = if (rule.isGreaterThan) {
                        { it > rule.threshold }
                    } else {
                        { it < rule.threshold }
                    }
                    { part: Map<String, Long> ->
                        if (matchesCondition(part.getValue(rule.attribute))) rule.destinationIdentifier
                        else null
                    }
                }
            }
            .reduce { ruleA, ruleB ->
                { part ->
                    ruleA(part) ?: ruleB(part)
                }
            }
            .let { findDestination ->
                { part: Map<String, Long> -> findDestination(part)!! }
            }
    }

    return parts.asSequence()
        .filter { part ->
            generateSequence("in") { ruleKey -> rules.getValue(ruleKey)(part) }
                .first { it in setOf("R", "A") } == "A"
        }
        .sumOf { acceptedPart ->
            acceptedPart.asSequence().sumOf { it.value }
        }
}

private fun part2(rules: Map<String, List<Rule>>): Long {
    return generateSequence(
        listOf(
            "in" to mapOf("x" to 1L..4000L, "m" to 1L..4000L, "a" to 1L..4000L, "s" to 1L..4000L)
        )
    ) { states ->
        states.asSequence()
            .filterNot { (nodeKey) -> nodeKey in setOf("R", "A") }
            .flatMap { initialState ->
                rules.getValue(initialState.first).fold(mutableListOf(initialState)) { states, rule ->
                    states.apply {
                        addAll(removeLast().second.apply(rule))
                    }
                }
            }
            .toList()
            .takeUnless { it.isEmpty() }
    }
        .flatten()
        .filter { it.first == "A" }
        .map { it.second }
        .sumOf { acceptedPart ->
            acceptedPart.asSequence()
                .map { it.value.size }
                .reduce(Long::times)
        }
}

private fun Map<String, LongRange>.apply(rule: Rule): Sequence<Pair<String, Map<String, LongRange>>> {
    return if (rule.threshold == null) sequenceOf(rule.destinationIdentifier to this)
    else getValue(rule.attribute)
        .let { initialRange ->
            if (rule.isGreaterThan) sequenceOf(
                (rule.threshold + 1)..initialRange.last,
                initialRange.first..rule.threshold,
            ) else sequenceOf(
                initialRange.first..<rule.threshold,
                rule.threshold..initialRange.last,
            )
        }
        .map { toMutableMap().apply { this[rule.attribute] = it } }
        .map { rule.destinationIdentifier to it }
}

private data class Rule(
    val attribute: String,
    val isGreaterThan: Boolean,
    val threshold: Long?,
    val destinationIdentifier: String,
)

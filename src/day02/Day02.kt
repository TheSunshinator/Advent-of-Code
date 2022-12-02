package day02

import io.kotest.matchers.shouldBe
import readInput

fun main() {
    val testInput = readInput("02", "test_input")
    val realInput = readInput("02", "input")

    testInput.part1().also(::println) shouldBe 15
    realInput.part1().let(::println)

    testInput.part2().also(::println) shouldBe 12
    realInput.part2().let(::println)
}

private fun <T> List<String>.totalScore(
    parseSecondInput: (Char) -> T,
    computeScore: (opponent: GameChoice, T) -> Int,
): Int {
    return asSequence()
        .map { GameChoice.parse(it[0]) to parseSecondInput(it[2]) }
        .map { (opponent, t) -> computeScore(opponent, t) }
        .sum()
}

private fun List<String>.part1() = totalScore(
    parseSecondInput = GameChoice::parse,
    computeScore = { opponent, self -> self.score + (self * opponent).score }
)

private fun List<String>.part2() = totalScore(
    parseSecondInput = Outcome::parse,
    computeScore = { opponent, outcome -> (outcome / opponent).score + outcome.score }
)

private enum class GameChoice(
    val score: Int,
) {
    ROCK(1) {
        override fun times(opponent: GameChoice) = when (opponent) {
            ROCK -> Outcome.DRAW
            PAPER -> Outcome.DEFEAT
            SCISSORS -> Outcome.WIN
        }
    },
    PAPER(2) {
        override fun times(opponent: GameChoice) = when (opponent) {
            ROCK -> Outcome.WIN
            PAPER -> Outcome.DRAW
            SCISSORS -> Outcome.DEFEAT
        }
    },
    SCISSORS(3) {
        override fun times(opponent: GameChoice) = when (opponent) {
            ROCK -> Outcome.DEFEAT
            PAPER -> Outcome.WIN
            SCISSORS -> Outcome.DRAW
        }
    };

    abstract operator fun times(opponent: GameChoice): Outcome

    companion object {
        internal fun parse(choice: Char): GameChoice = when (choice) {
            'A', 'X' -> ROCK
            'B', 'Y' -> PAPER
            else -> SCISSORS
        }
    }
}

private enum class Outcome(val score: Int) {
    WIN(6) {
        override fun div(opponent: GameChoice) = when (opponent) {
            GameChoice.ROCK -> GameChoice.PAPER
            GameChoice.PAPER -> GameChoice.SCISSORS
            GameChoice.SCISSORS -> GameChoice.ROCK
        }
    },
    DRAW(3) {
        override fun div(opponent: GameChoice) = opponent
    },
    DEFEAT(0) {
        override fun div(opponent: GameChoice) = when (opponent) {
            GameChoice.ROCK -> GameChoice.SCISSORS
            GameChoice.PAPER -> GameChoice.ROCK
            GameChoice.SCISSORS -> GameChoice.PAPER
        }
    };

    abstract operator fun div(opponent: GameChoice): GameChoice

    companion object {
        internal fun parse(choice: Char): Outcome = when (choice) {
            'X' -> DEFEAT
            'Y' -> DRAW
            else -> WIN
        }
    }
}

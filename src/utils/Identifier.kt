package utils

@JvmInline
value class Identifier(val value: String)

interface Identifiable {
    val identifier: Identifier
}

fun String.asIdentifier() = Identifier(this)

package json

fun JsonValue.stringify(
    pretty: Boolean = false,
    indent: Int = 4,
    depth: Int = 0,
): String = buildString {
    when (this@stringify) {
        is JsonValue.Null -> append("null")
        is JsonValue.Boolean -> append(value)
        is JsonValue.Number -> append(value)
        is JsonValue.String -> append(value.escape())

        is JsonValue.Array -> {
            if (this@stringify.isEmpty()) return "[]"
            append('[')
            if (pretty) append('\n')
            this@stringify.forEachIndexed { i, v ->
                if (pretty) appendIndent(indent, depth + 1)
                append(v.stringify(pretty, indent, depth + 1))
                if (i != this@stringify.size - 1) append(',')
                if (pretty) append('\n')
            }
            if (pretty) appendIndent(indent, depth)
            append(']')
        }

        is JsonValue.Object -> {
            if (this@stringify.isEmpty()) return "{}"
            append('{')
            if (pretty) append('\n')
            this@stringify.onEachIndexed { i, (k, v) ->
                if (pretty) appendIndent(indent, depth + 1)
                append(k.escape())
                append(':')
                if (pretty) append(' ')
                append(v.stringify(pretty, indent, depth + 1))
                if (i != this@stringify.size - 1) append(',')
                if (pretty) append('\n')
            }
            if (pretty) appendIndent(indent, depth)
            append('}')
        }
    }
}

private fun StringBuilder.appendIndent(indent: Int, depth: Int) =
    append(" ".repeat(indent * depth))

private fun String.escape() = buildString {
    append("\"")
    for (c in this@escape) when (c) {
        '\\' -> append("\\\\")
        '"' -> append("\\\"")
        '\n' -> append("\\n")
        '\t' -> append("\\t")
        '\r' -> append("\\r")
        else -> append(c)
    }
    append("\"")
}

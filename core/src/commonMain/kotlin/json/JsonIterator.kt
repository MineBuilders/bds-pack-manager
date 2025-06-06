package json

open class JsonIterator(private val base: Iterator<Char>) : Iterator<Char> {
    private var peeked: Char? = null
    private var hasPeeked = false

    private var line = 1
    private var column = 0

    override fun hasNext() =
        hasPeeked || base.hasNext()

    override fun next() =
        (if (hasPeeked) peeked!!.also { hasPeeked = false }
        else base.next()).also {
            if (it == '\n') (line++).also { column = 0 }
            else column++
        }

    fun peek() =
        if (hasPeeked) peeked!!
        else base.next().also { peeked = it; hasPeeked = true }

    fun peekOrNull() = if (hasNext()) peek() else null

    fun skipWhitespace() =
        dropRemain { it.isWhitespace() }

    inline fun dropRemain(condition: (Char) -> Boolean = { true }) {
        while (hasNext() && condition(peek())) next()
    }

    inline fun consumeRemain(condition: (Char) -> Boolean = { true }) =
        buildString { while (hasNext() && condition(peek())) append(next()) }

    internal fun consumeRawString() = sequence {
        if (!hasNext() || peek() != '"')
            error("String must start with a quote")
        yield(next())
        var escaping = false
        while (hasNext()) {
            val c = next()
            yield(c)
            if (escaping) escaping = false
            else when (c) {
                '\\' -> escaping = true
                '"' -> return@sequence
            }
        }
        error("Unterminated string literal")
    }

    fun consumeString() = buildString {
        val iterator = consumeRawString().iterator()
        var escaping = false
        iterator.next()
        for (c in iterator)
            if (escaping) when (c) {
                '\\' -> append('\\')
                '"' -> append('"')
                'n' -> append('\n')
                't' -> append('\t')
                'r' -> append('\r')
                else -> error("Unknown escape \\$c")
            }.also { escaping = false }
            else if (c == '\\') escaping = true
            else append(c)
        setLength(length - 1)
    }

    fun subScope(begin: Char, end: Char) =
        if (!hasNext() || peek() != begin)
            error("Scope must start with '$begin'")
        else JsonIterator(sequence {
            yield(next())
            var depth = 1
            while (hasNext() && depth > 0)
                when (val c = peek()) {
                    begin -> yield(next()).also { depth++ }
                    end -> yield(next()).also { depth-- }
                    else -> if (c == '"') yieldAll(consumeRawString())
                    else yield(next())
                }
            if (depth != 0)
                error("Unmatched '$begin' ... '$end'")
        }.iterator())

    inline fun subObject() = subScope('{', '}')

    inline fun subArray() = subScope('[', ']')

    inner class Exception(message: String) : IllegalStateException("$message (at $line:$column)")

    inline fun error(message: String): Nothing = throw Exception(message)
}

package json

/** String | Int */
typealias JsonKey = Comparable<*>

class JsonLazy(private val json: Iterable<Char>) {
    constructor(json: CharSequence) : this(json.asIterable())

    operator fun get(vararg keys: JsonKey) =
        JsonIterator(json.iterator()).navigate(keys, 0)

    private tailrec fun JsonIterator.navigate(keys: Array<out JsonKey>, index: Int): JsonValue {
        if (index == keys.size) return JsonParser(this).parseValue()
        skipWhitespace()
        return when (val key = keys[index]) {
            is String -> subObject()
                .findInObject(key)
                .navigate(keys, index + 1)

            is Int -> subArray()
                .findInArray(key)
                .navigate(keys, index + 1)

            else -> error("Key must be String (object) or Int (array), but got $key (${key::class.simpleName})")
        }
    }

    private fun JsonIterator.findInObject(key: String): JsonIterator {
        next()
        while (hasNext()) {
            skipWhitespace()
            if (peek() != '"')
                error("Expected string key in object, but got '${peek()}'")
            val currentKey = consumeString()

            skipWhitespace()
            if (next() != ':') error("Expected ':' after object key '$currentKey'")

            skipWhitespace()
            if (currentKey == key) return subValue()
            else skipValue()

            skipWhitespace()
            when (next().also { skipWhitespace() }) {
                ',' -> continue
                '}' -> break
                else -> error("Expected ',' or '}' in object after skipping a field, but got '${peek()}'")
            }
        }
        error("Key '$key' not found in object")
    }

    private fun JsonIterator.findInArray(index: Int): JsonIterator {
        if (index < 0)
            error("Array index must be non-negative, but got $index")

        next()
        var currentIndex = 0
        while (hasNext()) {
            skipWhitespace()
            if (currentIndex == index) return subValue()
            else skipValue()

            currentIndex++
            skipWhitespace()
            when (next().also { skipWhitespace() }) {
                ',' -> continue
                ']' -> break
                else -> error("Expected ',' or ']' in array after skipping an element, but got '${peekOrNull()}'")
            }
        }
        error("Array index $index out of bounds")
    }

    private fun JsonIterator.subValue() = when (peek()) {
        '{' -> subObject()
        '[' -> subArray()
        '"' -> JsonIterator(sequence { yieldAll(consumeRawString()) }.iterator())
        else -> JsonIterator(consumeRemain { c ->
            c.isLetter() || c.isDigit() || c == '-' || c == '+' ||
                    c == '.' || c == 'e' || c == 'E'
        }.iterator())
    }

    private fun JsonIterator.skipValue(): Any = when (peekOrNull()) {
        '{' -> subObject().dropRemain()
        '[' -> subArray().dropRemain()
        '"' -> consumeRawString().forEach { _ -> }
        in '0'..'9', '-' -> dropRemain { c ->
            c.isDigit() || c == '-' || c == '+' || c == '.' ||
                    c == 'e' || c == 'E'
        }

        't', 'f', 'n' -> dropRemain { it.isLetter() }
        null -> error("Unexpected end of input when trying to skip a value")
        else -> error("Unexpected character '${peek()}' when trying to skip a value")
    }
}

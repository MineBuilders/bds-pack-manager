package json

class JsonParser(private val iterator: JsonIterator) {
    constructor(json: CharSequence) : this(JsonIterator(json.iterator()))

    init {
        iterator.skipWhitespace()
    }

    fun parseValue(): JsonValue {
        if (!iterator.hasNext())
            iterator.error("Unexpected end of input when parsing value")
        return when (val c = iterator.peek()) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> JsonValue.String(parseString())
            in '0'..'9', '-' -> JsonValue.Number(parseNumber())
            't', 'f', 'n' -> parseLiteral()
            else -> iterator.error("Unexpected character '$c' when parsing value")
        }
    }

    fun parseObject(): JsonValue.Object {
        if (iterator.next() != '{')
            iterator.error("Expected '{' at beginning of object")

        iterator.skipWhitespace()
        if (iterator.peek() == '}')
            return JsonValue.Object(emptyMap()).also { iterator.next() }

        val map = mutableMapOf<String, JsonValue>()
        while (true) {
            iterator.skipWhitespace()
            if (iterator.peek() != '"')
                iterator.error("Expected string key in object, but got '${iterator.peek()}'")
            val key = parseString()

            iterator.skipWhitespace()
            if (iterator.next() != ':')
                iterator.error("Expected ':' after object key")

            iterator.skipWhitespace()
            val value = parseValue()
            map[key] = value

            iterator.skipWhitespace()
            when (iterator.next()) {
                ',' -> continue
                '}' -> break
                else -> iterator.error("Expected ',' or '}' after object entry, but got '${iterator.peek()}'")
            }
        }
        return JsonValue.Object(map)
    }

    fun parseArray(): JsonValue.Array {
        if (iterator.next() != '[')
            iterator.error("Expected '[' at beginning of array")

        iterator.skipWhitespace()
        if (iterator.peek() == ']')
            return JsonValue.Array(emptyList()).also { iterator.next() }

        val list = mutableListOf<JsonValue>()
        while (true) {
            iterator.skipWhitespace()
            val element = parseValue()
            list.add(element)

            iterator.skipWhitespace()
            when (iterator.next()) {
                ',' -> continue
                ']' -> break
                else -> iterator.error("Expected ',' or ']' after array element, but got '${iterator.peek()}'")
            }
        }
        return JsonValue.Array(list)
    }

    fun parseString() = iterator.consumeString()

    fun parseNumber(): Number {
        val number =
            iterator.consumeRemain { c -> c.isDigit() || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' }
        return when {
            '.' in number || 'e' in number || 'E' in number -> {
                val asDouble = number.toDouble()
                if (asDouble.toFloat().toDouble() == asDouble) asDouble.toFloat()
                else asDouble
            }

            else -> {
                val asLong = number.toLong()
                if (asLong in Int.MIN_VALUE..Int.MAX_VALUE) asLong.toInt()
                else asLong
            }
        }
    }

    fun parseLiteral() = when (iterator.peek()) {
        't' -> {
            val lit = iterator.consumeRemain { it.isLetter() }
            if (lit == "true") JsonValue.Boolean(true)
            else iterator.error("Invalid literal: '$lit', expected 'true'")
        }

        'f' -> {
            val lit = iterator.consumeRemain { it.isLetter() }
            if (lit == "false") JsonValue.Boolean(false)
            else iterator.error("Invalid literal: '$lit', expected 'false'")
        }

        'n' -> {
            val lit = iterator.consumeRemain { it.isLetter() }
            if (lit == "null") JsonValue.Null
            else iterator.error("Invalid literal: '$lit', expected 'null'")
        }

        else -> iterator.error("Unknown literal starting with '${iterator.peek()}'")
    }
}

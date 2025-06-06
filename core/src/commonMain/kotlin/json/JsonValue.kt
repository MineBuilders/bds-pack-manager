package json

typealias JsonMap = Map<String, JsonValue>
typealias JsonList = List<JsonValue>

sealed interface JsonValue {
    value class Object(private val value: JsonMap) : JsonValue, JsonMap by value
    value class Array(private val value: JsonList) : JsonValue, JsonList by value
    value class String(val value: kotlin.String) : JsonValue
    value class Number(val value: kotlin.Number) : JsonValue
    value class Boolean(val value: kotlin.Boolean) : JsonValue
    data object Null : JsonValue

    companion object {
        inline fun JsonValue?.isNull() = this == null || this is Null

        inline val JsonValue?.asObject get() = this as? Object
        inline val JsonValue?.asArray get() = this as? Array
        inline val JsonValue?.asString get() = (this as? String)?.value
        inline val JsonValue?.asNumber get() = this as? Number
        inline val JsonValue?.asBoolean get() = (this as? Boolean)?.value

        inline val Number?.double get() = this?.value?.toDouble()
        inline val Number?.float get() = this?.value?.toFloat()
        inline val Number?.long get() = this?.value?.toLong()
        inline val Number?.int get() = this?.value?.toInt()

        inline operator fun JsonValue?.get(key: kotlin.String) = asObject?.get(key)
        inline operator fun JsonValue?.get(index: Int) = asArray?.get(index)

        inline fun JsonMap.toJson() = Object(this)
        inline fun JsonList.toJson() = Array(this)
        inline fun kotlin.String.toJson() = String(this)
        inline fun kotlin.Number.toJson() = Number(this)
        inline fun kotlin.Boolean.toJson() = Boolean(this)
    }
}

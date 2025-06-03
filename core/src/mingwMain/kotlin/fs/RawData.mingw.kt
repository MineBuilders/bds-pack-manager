package fs

actual typealias PlatformRawData = ByteArray

actual value class RawData
actual constructor(actual val data: PlatformRawData) {
    @Deprecated("Use .data directly on mingw", ReplaceWith("data"))
    actual suspend inline fun toByteArray() = data

    actual companion object {
        actual inline fun from(data: ByteArray) =
            RawData(data)
    }
}

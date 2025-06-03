package fs

expect class PlatformRawData

expect value class RawData(val data: PlatformRawData) {
    suspend inline fun toByteArray(): ByteArray

    companion object {
        fun from(data: ByteArray): RawData
    }
}

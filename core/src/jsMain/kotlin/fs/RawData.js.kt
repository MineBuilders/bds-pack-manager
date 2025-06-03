package fs

import js.typedarrays.toByteArray
import org.khronos.webgl.Uint8Array
import web.blob.Blob
import web.blob.BlobPropertyBag

actual typealias PlatformRawData = Blob

actual value class RawData
actual constructor(actual val data: PlatformRawData) {
    actual suspend inline fun toByteArray() =
        data.bytes().toByteArray()

    actual companion object {
        actual fun from(data: ByteArray): RawData {
            val uint8Array = Uint8Array(data.toTypedArray())
            val options = BlobPropertyBag(type = "application/octet-stream")
            val blob = Blob(arrayOf(uint8Array), options)
            return RawData(blob)
        }
    }
}

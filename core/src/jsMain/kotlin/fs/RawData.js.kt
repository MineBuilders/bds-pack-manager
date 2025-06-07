package fs

import js.typedarrays.toByteArray
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
//import org.khronos.webgl.get
import web.blob.Blob
import web.blob.BlobPropertyBag

actual typealias PlatformRawData = Blob

actual value class RawData
actual constructor(actual val data: PlatformRawData) {
    @Suppress("CAST_NEVER_SUCCEEDS")
    actual suspend inline fun toByteArray() =
        (toUint8Array() as js.typedarrays.Uint8Array<*>).toByteArray()

    @Suppress("CAST_NEVER_SUCCEEDS")
    suspend inline fun toUint8Array() =
        Uint8Array(data.arrayBuffer() as ArrayBuffer)

//    fun Uint8Array.toByteArray() =
//        ByteArray(length) { i -> this[i].toInt().toByte() }

    actual companion object {
        actual fun from(data: ByteArray) =
            from(Uint8Array(data.toTypedArray()))

        fun from(data: Uint8Array) =
            RawData(Blob(arrayOf(data), BlobPropertyBag(type = "application/octet-stream")))
    }
}

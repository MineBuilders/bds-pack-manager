package fs.zip

import fs.RawData
import js.objects.Object
import js.objects.Record
import org.khronos.webgl.Uint8Array

actual suspend fun RawData.unzip() = buildMap {
    for ((k, v) in Object.entries(unzipSync(toUint8Array())))
        put(k, RawData.from(v))
}

actual suspend fun Zip.zip() =
    RawData.from(zipSync(Record<String, Uint8Array>().apply {
        for ((path, raw) in this@zip)
            set(path, raw.toUint8Array())
    }))

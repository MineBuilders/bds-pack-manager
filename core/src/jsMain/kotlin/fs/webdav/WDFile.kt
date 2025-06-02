package fs.webdav

import fs.IFile
import js.typedarrays.toByteArray
import org.khronos.webgl.Uint8Array
import web.blob.Blob
import web.blob.BlobPropertyBag

class WDFile(
    client: WebDavClient, path: String
) : WDPath(client, path), IFile<WDPath, WDFile, WDDirectory> {
    override suspend fun readRaw() =
        client.download(path).bytes().toByteArray()

    override suspend fun writeRaw(content: ByteArray) {
        val uint8Array = Uint8Array(content.toTypedArray())
        val data = Blob(arrayOf(uint8Array), BlobPropertyBag(type = "application/octet-stream"))
        client.upload(path, data)
    }

    override suspend fun readText() =
        client.download(path).text()

    override suspend fun writeText(content: String) {
        val data = Blob(arrayOf(content), BlobPropertyBag(type = "text/plain"))
        client.upload(path, data)
    }
}

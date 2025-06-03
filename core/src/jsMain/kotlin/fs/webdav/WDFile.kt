package fs.webdav

import fs.IFile
import fs.RawData
import web.blob.Blob
import web.blob.BlobPropertyBag

class WDFile(
    client: WebDavClient, path: String
) : WDPath(client, path), IFile<WDPath, WDFile, WDDirectory> {
    override suspend fun readRaw() =
        RawData(client.download(path))

    override suspend fun writeRaw(content: RawData) {
        client.upload(path, content.data)
    }

    override suspend fun readText() =
        client.download(path).text()

    override suspend fun writeText(content: String) {
        val options = BlobPropertyBag(type = "text/plain")
        val data = Blob(arrayOf(content), options)
        client.upload(path, data)
    }
}

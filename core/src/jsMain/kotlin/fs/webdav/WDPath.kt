package fs.webdav

import fs.IPath

abstract class WDPath internal constructor(
    val client: WebDavClient,
    path: String,
) : IPath<WDPath, WDFile, WDDirectory> {
    val path = WebDavClient.ensureSubPath(path)

    override val name
        get() = path.substringAfterLast('/')
    override val parent: WDDirectory?
        get() {
            val lastSlashIndex = path.lastIndexOf('/')
            if (lastSlashIndex <= 0) return null
            return WDDirectory(client, path.substring(0, lastSlashIndex))
        }

    suspend fun isExist() = try {
        client.list(path)
        true
    } catch (e: Throwable) {
        false
    }

    override suspend fun delete() {
        client.remove(path)
    }
}

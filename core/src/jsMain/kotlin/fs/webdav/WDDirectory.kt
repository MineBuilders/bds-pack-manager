package fs.webdav

import fs.IDirectory
import fs.RawData

class WDDirectory(
    client: WebDavClient, path: String = ""
) : WDPath(client, path), IDirectory<WDPath, WDFile, WDDirectory> {
    override suspend fun getItems() = client.list(path).map {
        if (it.isDirectory) WDDirectory(client, resolvePath(it.name))
        else WDFile(client, resolvePath(it.name))
    }

    override suspend fun resolveFile(name: String, create: Boolean) =
        WDFile(client, resolvePath(name)).also {
            if (create) runCatching { client.list(it.path) }
                .onFailure { _ -> it.writeRaw(RawData.from(byteArrayOf())) }
        }

    override suspend fun resolveDirectory(name: String, create: Boolean) =
        WDDirectory(client, resolvePath(name))
            .also { if (create) runCatching { client.mkdir(it.path) } }

    private fun resolvePath(name: String) = "$path/$name"
}

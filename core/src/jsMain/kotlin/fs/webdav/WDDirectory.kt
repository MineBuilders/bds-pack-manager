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

    override suspend fun resolveFileName(name: String, create: Boolean): WDFile? {
        val file = WDFile(client, resolvePath(name))
        val exist = file.isExist()
        if (!exist && !create) return null
        if (!exist && create) file.writeRaw(RawData.from(byteArrayOf()))
        return file
    }

    override suspend fun resolveDirectoryName(name: String, create: Boolean): WDDirectory? {
        val directory = WDDirectory(client, resolvePath(name))
        val exist = directory.isExist()
        if (!exist && !create) return null
        if (!exist && create) client.mkdir(directory.path)
        return directory
    }

    private fun resolvePath(name: String) = "$path/$name"
}

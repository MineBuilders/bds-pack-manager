package fs.webdav

import fs.IDirectory

class WDDirectory : IDirectory<WDPath, WDDirectory, WDFile> {
    override val name: String
        get() = TODO("Not yet implemented")
    override val parent: WDDirectory?
        get() = TODO("Not yet implemented")

    override suspend fun delete() {
        TODO("Not yet implemented")
    }

    override suspend fun getItems(): List<WDPath> {
        TODO("Not yet implemented")
    }

    override suspend fun resolveFile(name: String, create: Boolean): WDFile {
        TODO("Not yet implemented")
    }

    override suspend fun resolveDirectory(name: String, create: Boolean): WDDirectory {
        TODO("Not yet implemented")
    }
}

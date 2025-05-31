package fs.webdav

import fs.IPath

class WDPath : IPath<WDPath, WDDirectory, WDFile> {
    override val name: String
        get() = TODO("Not yet implemented")
    override val parent: WDDirectory?
        get() = TODO("Not yet implemented")

    override suspend fun delete() {
        TODO("Not yet implemented")
    }
}

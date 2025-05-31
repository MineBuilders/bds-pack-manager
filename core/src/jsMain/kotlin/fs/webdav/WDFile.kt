package fs.webdav

import fs.IFile

class WDFile : IFile<WDPath, WDDirectory, WDFile> {
    override val name: String
        get() = TODO("Not yet implemented")
    override val parent: WDDirectory?
        get() = TODO("Not yet implemented")

    override suspend fun delete() {
        TODO("Not yet implemented")
    }

    override suspend fun readRaw(): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun writeRaw(content: ByteArray) {
        TODO("Not yet implemented")
    }

    override suspend fun readText(): String {
        TODO("Not yet implemented")
    }

    override suspend fun writeText(content: String) {
        TODO("Not yet implemented")
    }
}

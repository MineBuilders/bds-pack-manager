package fs

expect open class File : Path, IFile<Path, File, Directory> {
    override suspend fun readRaw(): ByteArray
    override suspend fun writeRaw(content: ByteArray)

    override suspend fun readText(): String
    override suspend fun writeText(content: String)
}

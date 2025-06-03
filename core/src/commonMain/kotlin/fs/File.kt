package fs

expect open class File : Path, IFile<Path, File, Directory> {
    override suspend fun readRaw(): RawData
    override suspend fun writeRaw(content: RawData)

    override suspend fun readText(): String
    override suspend fun writeText(content: String)
}

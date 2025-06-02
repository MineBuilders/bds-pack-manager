package fs

interface IFile<
        Path : IPath<Path, File, Directory>,
        File : IFile<Path, File, Directory>,
        Directory : IDirectory<Path, File, Directory>,
        > : IPath<Path, File, Directory> {
    suspend fun readRaw(): ByteArray
    suspend fun writeRaw(content: ByteArray)

    suspend fun readText(): String
    suspend fun writeText(content: String)
}

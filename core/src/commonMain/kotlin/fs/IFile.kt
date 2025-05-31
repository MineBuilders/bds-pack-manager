package fs

interface IFile<
        Path : IPath<Path, Directory, File>,
        Directory : IDirectory<Path, Directory, File>,
        File : IFile<Path, Directory, File>
        > : IPath<Path, Directory, File> {
    suspend fun readRaw(): ByteArray
    suspend fun writeRaw(content: ByteArray)

    suspend fun readText(): String
    suspend fun writeText(content: String)
}

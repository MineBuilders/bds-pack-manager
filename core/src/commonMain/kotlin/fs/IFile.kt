package fs

typealias AFile = IFile<*, *, *>

interface IFile<
        Path : IPath<Path, File, Directory>,
        File : IFile<Path, File, Directory>,
        Directory : IDirectory<Path, File, Directory>,
        > : IPath<Path, File, Directory> {
    suspend fun readRaw(): RawData
    suspend fun writeRaw(content: RawData)

    suspend fun readText(): String
    suspend fun writeText(content: String)
}

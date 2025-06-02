package fs

interface IDirectory<
        Path : IPath<Path, File, Directory>,
        File : IFile<Path, File, Directory>,
        Directory : IDirectory<Path, File, Directory>,
        > : IPath<Path, File, Directory> {
    suspend fun getItems(): List<Path>

    suspend fun resolveFile(name: String, create: Boolean = false): File
    suspend fun resolveDirectory(name: String, create: Boolean = false): Directory
}

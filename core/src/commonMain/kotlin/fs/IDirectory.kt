package fs

interface IDirectory<
        Path : IPath<Path, Directory, File>,
        Directory : IDirectory<Path, Directory, File>,
        File : IFile<Path, Directory, File>
        > : IPath<Path, Directory, File> {
    suspend fun getItems(): List<Path>

    suspend fun resolveFile(name: String, create: Boolean = false): File
    suspend fun resolveDirectory(name: String, create: Boolean = false): Directory
}

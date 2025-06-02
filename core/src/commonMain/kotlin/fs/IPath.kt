package fs

interface IPath<
        Path : IPath<Path, File, Directory>,
        File : IFile<Path, File, Directory>,
        Directory : IDirectory<Path, File, Directory>,
        > {
    val name: String
    val parent: Directory?

    suspend fun delete()
}

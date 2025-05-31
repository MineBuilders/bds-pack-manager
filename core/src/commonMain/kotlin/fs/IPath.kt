package fs

interface IPath<
        Path : IPath<Path, Directory, File>,
        Directory : IDirectory<Path, Directory, File>,
        File : IFile<Path, Directory, File>
        > {
    val name: String
    val parent: Directory?

    suspend fun delete()
}

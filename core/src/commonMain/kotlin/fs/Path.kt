package fs

expect abstract class Path : IPath<Path, Directory, File> {
    override val name: String
    override val parent: Directory?

    override suspend fun delete()
}

package fs

expect abstract class Path : IPath<Path, File, Directory> {
    override val name: String
    override val parent: Directory?

    override suspend fun delete()
}

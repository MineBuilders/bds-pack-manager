package fs

expect class Directory : Path, IDirectory<Path, Directory, File> {
    override suspend fun getItems(): List<Path>

    override suspend fun resolveFile(name: String, create: Boolean): File
    override suspend fun resolveDirectory(name: String, create: Boolean): Directory
}

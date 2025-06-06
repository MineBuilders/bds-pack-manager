package fs

expect class Directory : Path, IDirectory<Path, File, Directory> {
    override suspend fun getItems(): List<Path>

    override suspend fun resolveFileName(name: String, create: Boolean): File?
    override suspend fun resolveDirectoryName(name: String, create: Boolean): Directory?
}

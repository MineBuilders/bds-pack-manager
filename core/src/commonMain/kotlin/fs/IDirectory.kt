package fs

typealias ADirectory = IDirectory<*, *, *>

interface IDirectory<
        Path : IPath<Path, File, Directory>,
        File : IFile<Path, File, Directory>,
        Directory : IDirectory<Path, File, Directory>,
        > : IPath<Path, File, Directory> {
    suspend fun getItems(): List<Path>

    suspend fun resolveFileName(name: String, create: Boolean = false): File?
    suspend fun resolveDirectoryName(name: String, create: Boolean = false): Directory?

    suspend fun resolveFile(path: String, create: Boolean = false): File? {
        val parts = path.split("/").filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null

        var current = this
        for (i in 0 until parts.lastIndex)
            current = current.resolveDirectoryName(parts[i], create) ?: return null
        return current.resolveFileName(parts.last(), create)
    }

    suspend fun resolveDirectory(path: String, create: Boolean = false): Directory? {
        val parts = path.split("/").filter { it.isNotEmpty() }
        if (parts.isEmpty()) return null

        @Suppress("UNCHECKED_CAST") var current = this as Directory
        for (part in parts) current = current.resolveDirectoryName(part, create) ?: return null
        return current
    }
}

package fs

import web.fs.FileSystemHandle
import web.fs.FileSystemRemoveOptions

actual abstract class Path internal constructor(
    actual override val parent: Directory?,
    open val handle: FileSystemHandle
) : IPath<Path, File, Directory> {
    actual override val name get() = handle.name

    actual override suspend fun delete() {
        val options = object : FileSystemRemoveOptions {
            override val recursive = true
        }
        parent?.handle?.removeEntry(name, options)
    }
}

package fs

import kotlinx.coroutines.await
import web.fs.*
import kotlin.js.Promise

actual class Directory(
    override val parent: Directory?,
    override val handle: FileSystemDirectoryHandle,
) : Path(parent, handle), IDirectory<Path, Directory, File> {

    actual override suspend fun getItems(): List<Path> {
        val list = mutableListOf<Path>()
        val asyncIterator = handle.entries().iterator()
        while (asyncIterator.hasNext()) {
            val (_, subHandle) = asyncIterator.next()
            if (subHandle.kind == FileSystemHandleKind.file) list +=
                File(this, subHandle.unsafeCast<FileSystemFileHandle>())
            if (subHandle.kind == FileSystemHandleKind.directory) list +=
                Directory(this, subHandle.unsafeCast<FileSystemDirectoryHandle>())
        }
        return list
    }

    actual override suspend fun resolveFile(name: String, create: Boolean): File {
        val options = object : FileSystemGetFileOptions {
            override val create = create
        }
        return File(this, handle.getFileHandle(name, options))
    }

    actual override suspend fun resolveDirectory(name: String, create: Boolean): Directory {
        val options = object : FileSystemGetDirectoryOptions {
            override val create = create
        }
        return Directory(this, handle.getDirectoryHandle(name, options))
    }

    companion object {
        suspend fun requestUser(): Directory {
            val handle = js("window.showDirectoryPicker()") as Promise<FileSystemDirectoryHandle>
            return Directory(null, handle.await())
        }
    }
}

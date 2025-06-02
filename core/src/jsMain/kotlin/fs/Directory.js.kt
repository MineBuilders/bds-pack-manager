package fs

import kotlinx.coroutines.await
import web.fs.*
import kotlin.js.Promise

actual class Directory(
    override val parent: Directory?,
    override val handle: FileSystemDirectoryHandle,
) : Path(parent, handle), IDirectory<Path, File, Directory> {
    actual override suspend fun getItems() = buildList {
        val asyncIterator = handle.entries().iterator()
        while (asyncIterator.hasNext()) {
            val (_, subHandle) = asyncIterator.next()
            if (subHandle.kind == FileSystemHandleKind.file) this +=
                File(this@Directory, subHandle.unsafeCast<FileSystemFileHandle>())
            if (subHandle.kind == FileSystemHandleKind.directory) this +=
                Directory(this@Directory, subHandle.unsafeCast<FileSystemDirectoryHandle>())
        }
    }

    actual override suspend fun resolveFile(name: String, create: Boolean) =
        File(this, handle.getFileHandle(name, FileSystemGetFileOptions(create = create)))

    actual override suspend fun resolveDirectory(name: String, create: Boolean) =
        Directory(this, handle.getDirectoryHandle(name, FileSystemGetDirectoryOptions(create = create)))

    companion object {
        suspend fun requestUser() = js("window.showDirectoryPicker()")
            .unsafeCast<Promise<FileSystemDirectoryHandle>>()
            .await().let { Directory(null, it) }
    }
}

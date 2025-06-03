package fs

import web.fs.FileSystemFileHandle
import web.fs.FileSystemWriteChunkType

actual open class File(
    override val parent: Directory?,
    override val handle: FileSystemFileHandle,
) : Path(parent, handle), IFile<Path, File, Directory> {
    actual override suspend fun readRaw() =
        RawData(handle.getFile())

    actual override suspend fun writeRaw(content: RawData) =
        writePlatform(content.data)

    actual override suspend fun readText() =
        handle.getFile().text()

    actual override suspend fun writeText(content: String) =
        writePlatform(content)

    private suspend fun writePlatform(data: FileSystemWriteChunkType) {
        val writable = handle.createWritable()
        writable.write(data)
        writable.close()
    }
}

package fs

import org.khronos.webgl.Uint8Array
import web.fs.FileSystemFileHandle

actual open class File(
    override val parent: Directory?,
    override val handle: FileSystemFileHandle,
) : Path(parent, handle), IFile<Path, Directory, File> {
    actual override suspend fun readRaw(): ByteArray {
        val file = handle.getFile()
        val uint8Array = file.bytes()
        return ByteArray(uint8Array.length) { i -> uint8Array[i].toByte() }
    }

    actual override suspend fun writeRaw(content: ByteArray) {
        val writable = handle.createWritable()
        val uint8Array = Uint8Array(content.toTypedArray())
        writable.write(uint8Array)
        writable.close()
    }

    actual override suspend fun readText() =
        handle.getFile().text()

    actual override suspend fun writeText(content: String) {
        val writable = handle.createWritable()
        writable.write(content)
        writable.close()
    }
}

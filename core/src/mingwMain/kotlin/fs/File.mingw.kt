package fs

import kotlinx.cinterop.*
import platform.windows.*

actual open class File(path: String) : Path(path), IFile<Path, Directory, File> {
    actual override suspend fun readRaw() = memScoped {
        val handle = CreateFileW(
            path,
            GENERIC_READ,
            FILE_SHARE_READ.convert(),
            null,
            OPEN_EXISTING.convert(),
            FILE_ATTRIBUTE_NORMAL.convert(),
            null
        )
        if (handle == INVALID_HANDLE_VALUE)
            error("Failed to open file: ${GetLastError()}")

        val buffer = ByteArray(4096)
        val bytesRead = alloc<DWORDVar>()
        val success = buffer.usePinned {
            ReadFile(
                handle,
                it.addressOf(0),
                buffer.size.toUInt(),
                bytesRead.ptr,
                null
            )
        }
        CloseHandle(handle)
        if (success == 0) error("Failed to read file: ${GetLastError()}")
        buffer.copyOf(bytesRead.value.toInt())
    }

    actual override suspend fun writeRaw(content: ByteArray) = memScoped {
        val handle = CreateFileW(
            path,
            GENERIC_WRITE.convert(),
            0u,
            null,
            CREATE_ALWAYS.convert(),
            FILE_ATTRIBUTE_NORMAL.convert(),
            null
        )
        if (handle == INVALID_HANDLE_VALUE)
            error("Failed to open file for write: ${GetLastError()}")

        val written = alloc<DWORDVar>()
        val success = content.usePinned {
            WriteFile(
                handle,
                it.addressOf(0),
                content.size.convert(),
                written.ptr,
                null
            )
        }
        CloseHandle(handle)
        if (success == 0) error("Failed to write file: ${GetLastError()}")
    }

    actual override suspend fun readText() = readRaw().decodeToString()

    actual override suspend fun writeText(content: String) = writeRaw(content.encodeToByteArray())
}

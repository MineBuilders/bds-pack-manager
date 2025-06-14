package fs

import kotlinx.cinterop.*
import platform.windows.*

actual open class File(path: String) : Path(path), IFile<Path, File, Directory> {
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

        val size = alloc<LARGE_INTEGER>()
        if (GetFileSizeEx(handle, size.ptr) == 0)
            error("Failed to get file size: ${GetLastError()}")
        val fileSize = size.QuadPart
        if (fileSize <= 0 || fileSize > Int.MAX_VALUE)
            error("Invalid file size: $fileSize")

        val buffer = ByteArray(fileSize.toInt())
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
        RawData(buffer.copyOf(bytesRead.value.toInt()))
    }

    actual override suspend fun writeRaw(content: RawData) = memScoped {
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
        val success = if (content.data.isEmpty())
            WriteFile(
                handle,
                null,
                0u,
                written.ptr,
                null
            )
        else content.data.usePinned {
            WriteFile(
                handle,
                it.addressOf(0),
                content.data.size.convert(),
                written.ptr,
                null
            )
        }
        CloseHandle(handle)
        if (success == 0) error("Failed to write file: ${GetLastError()}")
    }

    actual override suspend fun readText() =
        readRaw().data.decodeToString()

    actual override suspend fun writeText(content: String) =
        writeRaw(RawData(content.encodeToByteArray()))
}

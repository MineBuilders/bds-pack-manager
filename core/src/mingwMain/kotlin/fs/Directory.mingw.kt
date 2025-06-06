package fs

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import platform.windows.*

actual class Directory(path: String) : Path(path), IDirectory<Path, File, Directory> {
    actual override suspend fun getItems(): List<Path> = memScoped {
        val result = mutableListOf<Path>()
        val findData = alloc<WIN32_FIND_DATAW>()
        val handle = FindFirstFileW("$path\\*", findData.ptr)
        if (handle == INVALID_HANDLE_VALUE) return emptyList()

        try {
            do {
                val name = findData.cFileName.toKString()
                if (name != "." && name != "..") {
                    val path = "$path\\$name"
                    val isDir = (findData.dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY.toUInt()) != 0u
                    result += if (isDir) Directory(path) else File(path)
                }
            } while (FindNextFileW(handle, findData.ptr) != 0)
        } finally {
            FindClose(handle)
        }
        result
    }

    actual override suspend fun resolveFileName(name: String, create: Boolean): File? {
        val file = File("$path\\$name")
        val exist = file.isFile()
        if (!exist && !create) return null
        if (!exist && create) file.writeText("")
        return file
    }

    actual override suspend fun resolveDirectoryName(name: String, create: Boolean): Directory? {
        val directory = Directory("$path\\$name")
        val exist = directory.isDirectory()
        if (!exist && !create) return null
        if (!exist && create) CreateDirectoryW(path, null)
        return directory
    }
}

package fs

import platform.windows.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

actual abstract class Path internal constructor(val path: String) : IPath<Path, File, Directory> {
    actual override val name: String
        get() = path.substringAfterLast('\\')

    actual override val parent: Directory?
        get() = path.substringBeforeLast('\\', "")
            .takeIf { it.isNotEmpty() }?.let { Directory(it) }

    @OptIn(ExperimentalContracts::class)
    fun isFile(): Boolean {
        contract {
            returns(true) implies (this@Path is File)
        }
        val attr = GetFileAttributesW(path)
        return attr != INVALID_FILE_ATTRIBUTES && (attr and FILE_ATTRIBUTE_DIRECTORY.toUInt() == 0u)
    }

    actual override suspend fun delete() {
        if (isFile()) DeleteFileW(path)
        else RemoveDirectoryW(path)
    }
}

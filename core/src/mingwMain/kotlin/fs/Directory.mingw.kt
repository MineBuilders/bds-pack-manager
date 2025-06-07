package fs

import kotlinx.cinterop.*
import platform.windows.*

actual class Directory(path: String) : Path(path), IDirectory<Path, File, Directory> {
    actual override suspend fun getItems(): List<Path> = memScoped {
        val result = mutableListOf<Path>()
        val findData = alloc<WIN32_FIND_DATAW>()
        val handle = FindFirstFileW("$path\\*", findData.ptr)
        if (handle == INVALID_HANDLE_VALUE) return emptyList()

        do {
            val name = findData.cFileName.toKString()
            if (name == "." || name == "..") continue
            val path = "$path\\$name"
            val isDir = (findData.dwFileAttributes and FILE_ATTRIBUTE_DIRECTORY.toUInt()) != 0u
            result += if (isDir) Directory(path) else File(path)
        } while (FindNextFileW(handle, findData.ptr) != 0)

        FindClose(handle)
        result
    }

    actual override suspend fun resolveFileName(name: String, create: Boolean): File? {
        val file = File("$path\\$name")
        val exist = file.isFile()
        if (!exist && !create) return null
        if (!exist && create) file.writeRaw(RawData(byteArrayOf()))
        return file
    }

    actual override suspend fun resolveDirectoryName(name: String, create: Boolean): Directory? {
        val directory = Directory("$path\\$name")
        val exist = directory.isDirectory()
        if (!exist && !create) return null
        if (!exist && create) CreateDirectoryW(directory.path, null)
        return directory
    }

    companion object
}

fun Directory.Companion.showPicker() = memScoped {
    if (CoInitializeEx(null, COINIT_APARTMENTTHREADED) != S_OK)
        error("Failed to initialize COM")

    val fileDialogPtr = alloc<LPVOIDVar>()
    var hResult = CoCreateInstance(
        CLSID_FileOpenDialog.ptr,
        null,
        CLSCTX_INPROC_SERVER.convert(),
        IID_IFileDialog.ptr,
        fileDialogPtr.ptr.reinterpret()
    )
    if (hResult < 0) CoUninitialize().also {
        error("Failed to create FileDialog: HRESULT = $hResult")
    }

    val fileDialog = fileDialogPtr.value!!.reinterpret<IFileDialog>().pointed
    val optionsVar = alloc<DWORDVar>()
    fileDialog.lpVtbl!!.pointed.GetOptions!!(fileDialog.ptr, optionsVar.ptr)
    fileDialog.lpVtbl!!.pointed.SetOptions!!(fileDialog.ptr, optionsVar.value or FOS_PICKFOLDERS)

    hResult = fileDialog.lpVtbl!!.pointed.Show!!(fileDialog.ptr, null)
    if (hResult < 0) {
        fileDialog.lpVtbl!!.pointed.Release!!(fileDialog.ptr)
        CoUninitialize()
        error("User cancelled or an error occurred (HRESULT = $hResult)")
    }

    var directoryPath: String? = null
    val itemPtr = alloc<LPVOIDVar>()
    if (fileDialog.lpVtbl!!.pointed.GetResult!!(fileDialog.ptr, itemPtr.ptr.reinterpret()) >= 0) {
        val item = itemPtr.value!!.reinterpret<IShellItem>().pointed
        val pathPtr = alloc<LPWSTRVar>()
        item.lpVtbl!!.pointed.GetDisplayName!!(item.ptr, SIGDN_FILESYSPATH, pathPtr.ptr)
        directoryPath = pathPtr.value?.toKStringFromUtf16()
        CoTaskMemFree(pathPtr.value)
    }

    fileDialog.lpVtbl!!.pointed.Release!!(fileDialog.ptr)
    CoUninitialize()
    Directory(directoryPath!!)
}

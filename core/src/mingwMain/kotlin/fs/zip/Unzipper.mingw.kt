package fs.zip

import fs.RawData
import kotlinx.cinterop.*
import kotlinx.cinterop.ByteVar
import libzip.*
import platform.posix.memset
import platform.posix.size_tVar

actual suspend fun RawData.unzip(): Zip = memScoped {
    val archive = alloc<mz_zip_archive>()
    memset(archive.ptr, 0, sizeOf<mz_zip_archive>().convert())

    data.usePinned { pinned ->
        if (mz_zip_reader_init_mem(
                archive.ptr,
                pinned.addressOf(0),
                data.size.toULong(),
                0u
            ) == 0
        ) error("Failed to init zip archive from memory")
    }

    val result = mutableMapOf<String, RawData>()
    for (i in 0 until mz_zip_reader_get_num_files(archive.ptr).toInt()) {
        val stat = alloc<mz_zip_archive_file_stat>()
        if (mz_zip_reader_file_stat(archive.ptr, i.convert(), stat.ptr) == 0) continue

        val name = stat.m_filename.toKString()
        if (name.endsWith("/")) continue

        val sizePtr = alloc<size_tVar>()
        val dataPtr = mz_zip_reader_extract_to_heap(archive.ptr, i.convert(), sizePtr.ptr, 0u)
            ?: continue
        val size = sizePtr.value.toInt()
        val data = dataPtr.reinterpret<ByteVar>()
        val byteArray = ByteArray(size) { data[it] }

        mz_free(dataPtr)
        result[name] = RawData(byteArray)
    }

    mz_zip_reader_end(archive.ptr)
    result
}

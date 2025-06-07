package fs.zip

import fs.RawData
import kotlinx.cinterop.*
import kotlinx.cinterop.ByteVar
import libzip.*
import platform.posix.memcpy
import platform.posix.memset

actual suspend fun Zip.zip() = memScoped {
    val archive = alloc<mz_zip_archive>()
    memset(archive.ptr, 0, sizeOf<mz_zip_archive>().convert())

    if (mz_zip_writer_init_heap(archive.ptr, 0u, (64 * 1024).convert()) == 0)
        error("Failed to initialize ZIP writer")

    for ((path, data) in entries)
        if (data.data.usePinned {
                mz_zip_writer_add_mem(
                    archive.ptr,
                    path,
                    it.addressOf(0),
                    data.data.size.convert(),
                    MZ_BEST_COMPRESSION.convert()
                )
            } == 0) {
            mz_zip_writer_end(archive.ptr)
            error("Failed to add file $path")
        }

    val dataPtr = alloc<CPointerVar<ByteVar>>()
    val sizePtr = alloc<ULongVar>()
    if (mz_zip_writer_finalize_heap_archive(archive.ptr, dataPtr.ptr.reinterpret(), sizePtr.ptr) == 0) {
        mz_zip_writer_end(archive.ptr)
        error("Failed to finalize archive")
    }

    val size = sizePtr.value.toInt()
    val data = dataPtr.value ?: error("Finalized buffer is null")
    val byteArray = ByteArray(size)
    byteArray.usePinned {
        memcpy(it.addressOf(0), data, size.convert())
    }

    mz_zip_writer_end(archive.ptr)
    RawData(byteArray)
}

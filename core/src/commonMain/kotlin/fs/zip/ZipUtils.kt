package fs.zip

import fs.ADirectory
import fs.AFile
import fs.RawData

@Suppress("RemoveExplicitTypeArguments")
suspend fun ADirectory.zip() = buildMap<String, RawData> {
    suspend fun ADirectory.collectFiles(parent: String?): Unit =
        getItems().forEach {
            val path = if (parent == null) it.name else "$parent/${it.name}"
            if (it is AFile) put(path, it.readRaw())
            else if (it is ADirectory) it.collectFiles(path)
        }
    collectFiles(null)
}.zip()

suspend fun Zip.saveTo(directory: ADirectory) =
    forEach { (path, data) ->
        directory.resolveFile(path, true)!!.writeRaw(data)
    }

package fs.zip

import fs.RawData

typealias Zip = Map<String, RawData>

expect suspend fun RawData.unzip() : Zip

expect suspend fun Zip.zip() : RawData

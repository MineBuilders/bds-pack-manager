@file:Suppress("SpellCheckingInspection")
@file:JsModule("fflate")
@file:JsNonModule

package fs.zip

import js.objects.Record
import org.khronos.webgl.Uint8Array

external interface ZipOptions : DeflateOptions, ZipAttributes

external interface DeflateOptions {
    var level: Int?
    var mem: Int?
    var dictionary: Uint8Array?
}

external interface ZipAttributes {
    var os: Double?
    var attrs: Double?
    var extra: Record<Double, Uint8Array>?
    var comment: String?

    /** string | number | Date | undefined */
    var mtime: Any?
}

external fun zipSync(
    data: Record<String, Uint8Array>,
    opts: ZipOptions = definedExternally
): Uint8Array

external interface UnzipOptions {
    var filter: ((file: UnzipFileInfo) -> Boolean)?
}

external interface UnzipFileInfo {
    var name: String
    var size: Double
    var originalSize: Double
    var compression: Double
}

external fun unzipSync(
    data: Uint8Array,
    opts: UnzipOptions = definedExternally
): Record<String, Uint8Array>

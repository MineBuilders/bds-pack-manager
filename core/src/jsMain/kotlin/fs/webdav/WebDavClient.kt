package fs.webdav

import js.array.asList
import web.blob.Blob
import web.encoding.btoa
import web.http.*
import web.parsing.DOMParser
import web.parsing.DOMParserSupportedType

class WebDavClient(host: String, username: String, password: String) {
    private val host = if (host.endsWith("/")) host.dropLast(1) else host
    private val headerAuth = "Basic " + btoa("$username:$password")

    private suspend fun request(
        method: RequestMethod,
        path: String,
        headers: Headers? = null,
        body: BodyInit? = undefined
    ) = fetch(
        url = host + ensureSubPath(path),
        RequestInit(
            method = method,
            headers = Headers().apply {
                set("Authorization", headerAuth)
                headers?.forEach { value, key -> set(key, value) }
            },
            body = body
        ),
    ).also {
        if (!it.ok) error("WebDAV error ${it.status}: ${it.statusText}")
    }

    data class ListItem(val name: String, val isDirectory: Boolean)

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE", "SpellCheckingInspection")
    suspend fun list(path: String) = buildList {
        val xml = DOMParser().parseFromString(
            request(
                method = js("\"PROPFIND\"") as RequestMethod,
                path = path,
                headers = Headers().apply {
                    set("Content-Type", "text/xml")
                    set("Depth", "1")
                },
                body = BodyInit(
                    """
                        <?xml version="1.0" encoding="utf-8" ?>
                        <propfind xmlns="DAV:">
                            <prop>
                                <displayname/>
                                <resourcetype/>
                            </prop>
                        </propfind>
                    """.trimIndent()
                )
            ).text(), DOMParserSupportedType.applicationXml
        )
        for (response in xml.getElementsByTagNameNS(DAV_NS, "response")) this += ListItem(
            name = response.getElementsByTagNameNS(DAV_NS, "displayname")
                .asList().firstOrNull()
                ?.textContent?.takeIf { it.isNotBlank() } ?: continue,
            isDirectory = response.getElementsByTagNameNS(DAV_NS, "collection").length > 0
        )
    }

    suspend fun upload(path: String, file: Blob) = request(
        method = RequestMethod.PUT,
        path = path,
        headers = Headers().apply { set("Content-Type", file.type) },
        body = BodyInit(file)
    )

    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    suspend fun mkdir(path: String) =
        request(js("\"MKCOL\"") as RequestMethod, path)

    suspend fun download(path: String) =
        request(RequestMethod.GET, path).blob()

    suspend fun remove(path: String) =
        request(RequestMethod.DELETE, path)

    companion object {
        private const val DAV_NS = "DAV:"

        fun ensureSubPath(subPath: String) =
            (if (subPath.startsWith("/")) subPath else "/$subPath")
                .let { if (it.endsWith("/")) it.dropLast(1) else it }
    }
}

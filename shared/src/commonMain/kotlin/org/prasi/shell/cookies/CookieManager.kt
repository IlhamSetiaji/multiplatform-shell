package org.prasi.shell.cookies

interface CookieManager {
    suspend fun setCookie(
        url: String,
        cookie: Cookie,
    )
    suspend fun getCookies(url: String): List<Cookie>
    suspend fun removeAllCookies()
    suspend fun removeCookies(url: String)
}
@Suppress("FunctionName")
expect fun WebViewCookieManager(): CookieManager
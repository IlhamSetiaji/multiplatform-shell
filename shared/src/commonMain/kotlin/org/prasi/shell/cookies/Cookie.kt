package org.prasi.shell.cookies

data class Cookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expiresDate: Long? = null,
    val isSessionOnly: Boolean = false,
    val sameSite: HTTPCookieSameSitePolicy? = null,
    val isSecure: Boolean? = null,
    val isHttpOnly: Boolean? = null,
    val maxAge: Long? = null,
) {
    enum class HTTPCookieSameSitePolicy {
        NONE,
        LAX,
        STRICT,
    }

    override fun toString(): String {
        var cookieValue = "$name=$value; Path=$path"

        if (domain != null) cookieValue += "; Domain=$domain"

        if (expiresDate != null) cookieValue += "; Expires=" + getCookieExpirationDate(expiresDate)

        if (maxAge != null) cookieValue += "; Max-Age=$maxAge"

        if (isSecure != null && isSecure) cookieValue += "; Secure"

        if (isHttpOnly != null && isHttpOnly) cookieValue += "; HttpOnly"

        if (sameSite != null) cookieValue += "; SameSite=$sameSite"

        cookieValue += ";"

        return cookieValue
    }
}

expect fun getCookieExpirationDate(expiresDate: Long): String
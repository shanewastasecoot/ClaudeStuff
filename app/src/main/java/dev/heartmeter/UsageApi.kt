package dev.heartmeter

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Talks to the (unofficial) endpoint Claude Code's /usage screen uses.
 * It may change without notice; failures surface as a short message on the widget.
 */
object UsageApi {
    private const val USAGE_URL = "https://api.anthropic.com/api/oauth/usage"
    private const val TOKEN_URL = "https://console.anthropic.com/v1/oauth/token"
    // Public OAuth client id of the Claude Code CLI.
    private const val CLIENT_ID = "9d1c250a-e61b-44d9-88ed-5944d1962f5e"

    class ApiException(val shortMessage: String, detail: String) : IOException(detail)

    /** Fetches usage, refreshing the access token once if it has expired. Returns the raw JSON. */
    fun fetch(prefs: Prefs): String {
        if (prefs.accessToken.isBlank()) throw ApiException("NO TOKEN", "No access token set")
        val first = getUsage(prefs.accessToken)
        if (first.code == 401 && prefs.refreshToken.isNotBlank()) {
            refresh(prefs)
            return checked(getUsage(prefs.accessToken))
        }
        return checked(first)
    }

    private data class Resp(val code: Int, val body: String)

    private fun checked(r: Resp): String = when (r.code) {
        200 -> r.body
        401 -> throw ApiException("TOKEN EXPIRED", "401: ${r.body.take(200)}")
        403 -> throw ApiException("NO ACCESS", "403: ${r.body.take(200)}")
        429 -> throw ApiException("SLOW DOWN", "429: rate limited")
        else -> throw ApiException("ERROR ${r.code}", "${r.code}: ${r.body.take(200)}")
    }

    private fun getUsage(token: String): Resp {
        val c = URL(USAGE_URL).openConnection() as HttpURLConnection
        c.requestMethod = "GET"
        c.setRequestProperty("Authorization", "Bearer $token")
        c.setRequestProperty("anthropic-beta", "oauth-2025-04-20")
        c.setRequestProperty("Accept", "application/json")
        c.setRequestProperty("User-Agent", "heartmeter-widget/1.0")
        return execute(c)
    }

    private fun refresh(prefs: Prefs) {
        val c = URL(TOKEN_URL).openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.doOutput = true
        c.setRequestProperty("Content-Type", "application/json")
        c.setRequestProperty("Accept", "application/json")
        val body = JSONObject()
            .put("grant_type", "refresh_token")
            .put("refresh_token", prefs.refreshToken)
            .put("client_id", CLIENT_ID)
            .toString()
        c.outputStream.use { it.write(body.toByteArray()) }
        val r = execute(c)
        if (r.code != 200) throw ApiException("RELOGIN NEEDED", "refresh ${r.code}: ${r.body.take(200)}")
        val o = JSONObject(r.body)
        prefs.accessToken = o.getString("access_token")
        // Refresh tokens rotate: keep the new one or the next refresh fails.
        o.optString("refresh_token").takeIf { it.isNotBlank() }?.let { prefs.refreshToken = it }
    }

    private fun execute(c: HttpURLConnection): Resp {
        c.connectTimeout = 15_000
        c.readTimeout = 15_000
        try {
            val code = c.responseCode
            val stream = if (code in 200..299) c.inputStream else c.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            return Resp(code, body)
        } catch (e: IOException) {
            throw ApiException("OFFLINE", e.message ?: "network error")
        } finally {
            c.disconnect()
        }
    }
}

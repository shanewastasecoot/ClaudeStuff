package dev.heartmeter

import android.content.Context
import android.content.SharedPreferences

/** Everything lives in app-private storage; nothing leaves the phone except the API calls. */
class Prefs(context: Context) {
    private val p: SharedPreferences =
        context.applicationContext.getSharedPreferences("heartmeter", Context.MODE_PRIVATE)

    var accessToken: String
        get() = p.getString("access_token", "") ?: ""
        set(v) = p.edit().putString("access_token", v.trim()).apply()

    var refreshToken: String
        get() = p.getString("refresh_token", "") ?: ""
        set(v) = p.edit().putString("refresh_token", v.trim()).apply()

    var lastJson: String?
        get() = p.getString("last_json", null)
        set(v) = p.edit().putString("last_json", v).apply()

    var lastSuccessAt: Long
        get() = p.getLong("last_success_at", 0L)
        set(v) = p.edit().putLong("last_success_at", v).apply()

    var lastError: String?
        get() = p.getString("last_error", null)
        set(v) = p.edit().putString("last_error", v).apply()

    fun snapshot(): UsageSnapshot? =
        lastJson?.let { runCatching { UsageSnapshot.parse(it) }.getOrNull() }
}

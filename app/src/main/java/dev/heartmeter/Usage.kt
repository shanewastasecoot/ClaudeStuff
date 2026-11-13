package dev.heartmeter

import org.json.JSONObject
import java.time.Instant
import java.time.OffsetDateTime

/** One rate-limit window as reported by the usage endpoint. */
data class UsageWindow(
    /** Percent of the window already used, 0..100. */
    val utilization: Double,
    val resetsAt: Instant?,
) {
    /** Percent still available, clamped to 0..100. */
    val remaining: Double get() = (100.0 - utilization).coerceIn(0.0, 100.0)
}

data class UsageSnapshot(
    val fiveHour: UsageWindow?,
    val sevenDay: UsageWindow?,
) {
    companion object {
        fun parse(json: String): UsageSnapshot {
            val o = JSONObject(json)
            return UsageSnapshot(
                fiveHour = window(o, "five_hour"),
                sevenDay = window(o, "seven_day"),
            )
        }

        private fun window(o: JSONObject, key: String): UsageWindow? {
            val w = o.optJSONObject(key) ?: return null
            if (w.isNull("utilization")) return null
            val resets = w.optString("resets_at", "").takeIf { it.isNotBlank() && it != "null" }
            return UsageWindow(
                utilization = w.getDouble("utilization"),
                resetsAt = resets?.let { runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull() },
            )
        }
    }
}

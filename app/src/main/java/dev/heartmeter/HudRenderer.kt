package dev.heartmeter

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.util.Date
import java.util.Locale
import kotlin.math.floor

/**
 * Draws the 8-bit adventure HUD on a tiny logical pixel grid, then scales each
 * logical pixel up to a solid square so it stays crisp on the home screen.
 *
 *   SESSION 66%        2H14M
 *   <3<3<3<3<3<3<3 . . .       <- 10 hearts, 10% each, quarter-heart steps
 *   WEEKLY 61%          3D4H
 *   [=========-------]         <- magic meter
 *   UPD 14:05
 */
object HudRenderer {
    const val W = 88
    const val H = 44

    private const val BG = 0xFF0B0B14.toInt()
    private const val FRAME = 0xFFC8A040.toInt()
    private const val FRAME_DANGER = 0xFFE03030.toInt()
    private const val TEXT = 0xFFF0F0E0.toInt()
    private const val DIM = 0xFF8A8A9A.toInt()
    private const val HEART = 0xFFE83838.toInt()
    private const val HEART_SHINE = 0xFFFFD0D0.toInt()
    private const val HEART_EMPTY = 0xFF3A1820.toInt()
    private const val MAGIC = 0xFF30C048.toInt()
    private const val MAGIC_SHINE = 0xFF90F0A0.toInt()
    private const val MAGIC_EMPTY = 0xFF10301A.toInt()
    private const val WARN = 0xFFFF6060.toInt()

    private val HEART_SHAPE = arrayOf(
        ".XX.XX.",
        "XXXXXXX",
        "XXXXXXX",
        ".XXXXX.",
        "..XXX..",
        "...X...",
    )

    data class State(
        val snapshot: UsageSnapshot?,
        val lastSuccessAt: Long,
        val error: String?,
        val now: Instant = Instant.now(),
    )

    fun render(state: State, scale: Int = 8): Bitmap {
        val px = Px(scale)
        val s = state.snapshot
        val session = s?.fiveHour
        val week = s?.sevenDay
        val danger = (session != null && session.remaining <= 20.0) ||
            (week != null && week.remaining <= 10.0)

        px.fill(0, 0, W, H, BG)
        frame(px, if (danger) FRAME_DANGER else FRAME)

        // Session (5-hour window) as hearts.
        px.text(4, 4, "SESSION " + pct(session), TEXT)
        px.textRight(W - 4, 4, countdown(session?.resetsAt, state.now), DIM)
        val life = session?.remaining ?: 0.0
        for (i in 0 until 10) {
            // Each heart holds 10%; round the partial heart to quarters.
            val f = ((life - i * 10.0) / 10.0).coerceIn(0.0, 1.0)
            heart(px, 4 + i * 8, 11, floor(f * 4.0) / 4.0, session != null)
        }

        // Weekly window as a magic meter.
        px.text(4, 20, "WEEKLY " + pct(week), TEXT)
        px.textRight(W - 4, 20, countdown(week?.resetsAt, state.now), DIM)
        meter(px, 4, 27, 80, 6, (week?.remaining ?: 0.0) / 100.0)

        // Footer: status line.
        val footer: Pair<String, Int> = when {
            state.error != null -> state.error to WARN
            session != null && session.remaining <= 0.0 ->
                "REST " + countdown(session.resetsAt, state.now) to WARN
            state.lastSuccessAt > 0 -> "UPD " + clock(state.lastSuccessAt) to DIM
            else -> "TAP TO START" to DIM
        }
        px.text(4, 35, footer.first.take(20), footer.second)

        return px.bitmap
    }

    private fun pct(w: UsageWindow?): String =
        if (w == null) "--" else "${w.remaining.toInt()}%"

    private fun countdown(at: Instant?, now: Instant): String {
        if (at == null) return ""
        val d = Duration.between(now, at)
        if (d.isNegative) return "0M"
        val days = d.toDays()
        val hours = d.toHours() % 24
        val mins = d.toMinutes() % 60
        return when {
            days > 0 -> "${days}D${hours}H"
            hours > 0 -> "${hours}H${mins}M"
            else -> "${mins}M"
        }
    }

    private fun clock(ms: Long): String =
        SimpleDateFormat("HH:mm", Locale.US).format(Date(ms))

    private fun frame(px: Px, c: Int) {
        // 1px border inset by 1, with clipped corners for a chunky retro look.
        px.fill(2, 1, W - 4, 1, c)
        px.fill(2, H - 2, W - 4, 1, c)
        px.fill(1, 2, 1, H - 4, c)
        px.fill(W - 2, 2, 1, H - 4, c)
    }

    private fun heart(px: Px, x: Int, y: Int, fill: Double, known: Boolean) {
        val filledCols = (fill * 7.0).let { if (fill >= 1.0) 7 else Math.round(it).toInt() }
        for (row in HEART_SHAPE.indices) {
            for (col in HEART_SHAPE[row].indices) {
                if (HEART_SHAPE[row][col] != 'X') continue
                val color = when {
                    !known -> HEART_EMPTY
                    col < filledCols -> if (row == 1 && col == 1) HEART_SHINE else HEART
                    else -> HEART_EMPTY
                }
                px.dot(x + col, y + row, color)
            }
        }
    }

    private fun meter(px: Px, x: Int, y: Int, w: Int, h: Int, frac: Double) {
        px.fill(x, y, w, h, TEXT) // outline
        px.fill(x + 1, y + 1, w - 2, h - 2, MAGIC_EMPTY)
        val inner = w - 2
        val n = Math.round(inner * frac.coerceIn(0.0, 1.0)).toInt()
        if (n > 0) {
            px.fill(x + 1, y + 1, n, h - 2, MAGIC)
            px.fill(x + 1, y + 1, n, 1, MAGIC_SHINE)
        }
    }

    /** Logical-pixel canvas. */
    private class Px(val scale: Int) {
        val bitmap: Bitmap = Bitmap.createBitmap(W * scale, H * scale, Bitmap.Config.ARGB_8888)
        private val canvas = Canvas(bitmap)
        private val paint = Paint().apply { isAntiAlias = false }

        fun fill(x: Int, y: Int, w: Int, h: Int, c: Int) {
            paint.color = c
            canvas.drawRect(
                (x * scale).toFloat(), (y * scale).toFloat(),
                ((x + w) * scale).toFloat(), ((y + h) * scale).toFloat(), paint,
            )
        }

        fun dot(x: Int, y: Int, c: Int) = fill(x, y, 1, 1, c)

        fun text(x: Int, y: Int, s: String, c: Int) {
            var cx = x
            for (ch in s.uppercase()) {
                val g = PixelFont.glyph(ch)
                for (r in 0 until 5) for (col in 0 until 3) {
                    if (g[r][col] == 'X') dot(cx + col, y + r, c)
                }
                cx += 4
            }
        }

        fun textRight(right: Int, y: Int, s: String, c: Int) =
            text(right - (s.length * 4 - 1), y, s, c)
    }
}

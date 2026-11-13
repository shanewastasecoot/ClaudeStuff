package dev.heartmeter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class UsageWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        redrawAll(context)
        UpdateWorker.schedulePeriodic(context)
    }

    override fun onEnabled(context: Context) {
        UpdateWorker.schedulePeriodic(context)
        UpdateWorker.refreshNow(context)
    }

    override fun onDisabled(context: Context) {
        UpdateWorker.cancelPeriodic(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) UpdateWorker.refreshNow(context)
    }

    companion object {
        const val ACTION_REFRESH = "dev.heartmeter.REFRESH"

        /** Redraws every placed widget from the cached snapshot. Cheap; no network. */
        fun redrawAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, UsageWidget::class.java))
            if (ids.isEmpty()) return
            val prefs = Prefs(context)
            val bitmap = HudRenderer.render(
                HudRenderer.State(prefs.snapshot(), prefs.lastSuccessAt, prefs.lastError),
            )
            val tap = PendingIntent.getBroadcast(
                context, 0,
                Intent(context, UsageWidget::class.java).setAction(ACTION_REFRESH),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val views = RemoteViews(context.packageName, R.layout.widget).apply {
                setImageViewBitmap(R.id.hud, bitmap)
                setOnClickPendingIntent(R.id.hud, tap)
            }
            manager.updateAppWidget(ids, views)
        }
    }
}

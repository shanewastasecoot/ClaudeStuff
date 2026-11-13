package dev.heartmeter

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import java.util.concurrent.Executors

/** Setup screen: paste tokens, test the connection, preview the HUD. */
class MainActivity : Activity() {
    private val io = Executors.newSingleThreadExecutor()
    private lateinit var prefs: Prefs
    private lateinit var preview: ImageView
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)
        preview = findViewById(R.id.preview)
        status = findViewById(R.id.status)
        val access = findViewById<EditText>(R.id.access_token)
        val refresh = findViewById<EditText>(R.id.refresh_token)
        access.setText(prefs.accessToken)
        refresh.setText(prefs.refreshToken)

        findViewById<Button>(R.id.save).setOnClickListener {
            prefs.accessToken = access.text.toString()
            prefs.refreshToken = refresh.text.toString()
            status.text = "Fetching…"
            io.execute {
                val err = UpdateWorker.refresh(applicationContext)
                runOnUiThread {
                    // The refresh may have rotated tokens; show what is stored now.
                    access.setText(prefs.accessToken)
                    refresh.setText(prefs.refreshToken)
                    status.text = if (err == null) "Quest log updated. Add the widget to your home screen!" else "Problem: $err"
                    redraw()
                }
            }
            UpdateWorker.schedulePeriodic(applicationContext)
        }
        redraw()
    }

    private fun redraw() {
        preview.setImageBitmap(
            HudRenderer.render(HudRenderer.State(prefs.snapshot(), prefs.lastSuccessAt, prefs.lastError)),
        )
    }

    override fun onDestroy() {
        io.shutdown()
        super.onDestroy()
    }
}

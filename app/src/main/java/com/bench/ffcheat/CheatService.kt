package com.bench.ffcheat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlinx.coroutines.*
import rikka.shizuku.Shizuku

class CheatService : Service() {

    companion object {
        const val CH = "ff_ch"
        var aimDX = 0f; var aimDY = 0f; var aimOn = false
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var wm: WindowManager
    private lateinit var overlay: OverlayView
    private var svc: IMemoryService? = null
    private var running = false

    private val userSvcArgs = Shizuku.UserServiceArgs(
        ComponentName(packageName, MemoryUserService::class.java.name)
    ).processNameSuffix("memory").debuggable(false).version(1)

    private val userSvcConn: Shizuku.UserServiceConnection = object : Shizuku.UserServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            svc = IMemoryService.Stub.asInterface(binder)
            val dm = DisplayMetrics()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(dm)
            svc?.setConfig(0.25f, 350f, dm.widthPixels.toFloat(), dm.heightPixels.toFloat())
            startLoop()
        }
        override fun onServiceDisconnected(name: ComponentName) { svc = null }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(1, buildNotif())

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlay = OverlayView(this)

        val dm = DisplayMetrics()
        @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(dm)
        wm.addView(overlay, WindowManager.LayoutParams(
            dm.widthPixels, dm.heightPixels,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ))

        Shizuku.bindUserService(userSvcArgs, userSvcConn)
    }

    private fun startLoop() {
        if (running) return
        running = true
        scope.launch {
            var ok = false
            var retries = 0
            while (running) {
                if (!ok) {
                    ok = svc?.init() ?: false
                    if (!ok) { delay(2000); if (++retries > 60) { stopSelf(); return@launch } ; continue }
                }

                val data = svc?.tick()
                if (data != null && data.isNotEmpty()) {
                    val cnt = data.last().toInt()
                    val esp = mutableListOf<EspEntity>()
                    for (i in 0 until cnt) {
                        val b = i * 5
                        if (b + 4 < data.size - 4)
                            esp += EspEntity(data[b], data[b+1], data[b+2], data[b+3], data[b+4].toInt())
                    }
                    val ab = cnt * 5
                    if (ab + 2 < data.size) { aimDX = data[ab]; aimDY = data[ab+1]; aimOn = data[ab+2] > .5f }
                    withContext(Dispatchers.Main) { overlay.updateEntities(esp); overlay.invalidate() }
                    if (aimOn) AimAccessibilityService.instance?.injectAimMove(aimDX, aimDY)
                }
                delay(16)
            }
        }
    }

    override fun onStartCommand(i: Intent?, f: Int, s: Int) = START_STICKY

    override fun onDestroy() {
        running = false; scope.cancel()
        runCatching { Shizuku.unbindUserService(userSvcArgs, userSvcConn, true) }
        runCatching { wm.removeView(overlay) }
        super.onDestroy()
    }

    override fun onBind(i: Intent?): IBinder? = null

    private fun createChannel() {
        val ch = NotificationChannel(CH, "System Service", NotificationManager.IMPORTANCE_MIN)
            .apply { setShowBadge(false); setSound(null, null) }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
    }

    private fun buildNotif() = Notification.Builder(this, CH)
        .setSmallIcon(android.R.drawable.ic_menu_preferences)
        .setContentTitle("System Service")
        .setOngoing(true).build()
}

data class EspEntity(val screenX: Float, val screenY: Float, val health: Float,
                     val maxHealth: Float, val teamId: Int)

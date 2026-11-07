package com.bench.ffcheat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }

        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(1)
                Toast.makeText(this, "Autorize o Shizuku", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            Toast.makeText(this, "Shizuku não está rodando", Toast.LENGTH_LONG).show()
            return
        }

        startForegroundService(Intent(this, CheatService::class.java))
        finish()
    }
}

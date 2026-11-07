package com.bench.ffcheat

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService injeta swipe gestures para mover a mira.
 *
 * Como funciona:
 *  - CheatService.tick() calcula o delta (dx, dy) da mira atual até a cabeça do alvo
 *  - Chama injectAimMove(dx, dy)
 *  - Este serviço envia um gesture de swipe no centro da tela na direção do delta
 *
 * Limitação: AccessibilityService não funciona em apps com FLAG_SECURE.
 * Free Fire às vezes ativa FLAG_SECURE — se for o caso, use root + InputManager:
 *   val im = getSystemService(Context.INPUT_SERVICE) as InputManager
 *   im.injectInputEvent(MotionEvent.obtain(...), InputManager.INJECT_INPUT_EVENT_MODE_ASYNC)
 * (requires android.permission.INJECT_EVENTS, concedida via root/adb)
 */
class AimAccessibilityService : AccessibilityService() {

    companion object {
        var instance: AimAccessibilityService? = null
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    /**
     * Injeta swipe do centro da tela em direção ao alvo.
     * dx, dy: delta já suavizado (pixels).
     */
    fun injectAimMove(dx: Float, dy: Float) {
        // Pega tamanho real da tela
        val dm = resources.displayMetrics
        val cx = dm.widthPixels  * 0.5f
        val cy = dm.heightPixels * 0.5f

        // Swipe de duração curta para ser responsivo
        val path = Path().apply {
            moveTo(cx, cy)
            lineTo(cx + dx, cy + dy)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(
                    path,
                    0L,   // startTime
                    16L   // duration ms — 1 frame
                )
            )
            .build()

        dispatchGesture(gesture, null, handler)
    }
}

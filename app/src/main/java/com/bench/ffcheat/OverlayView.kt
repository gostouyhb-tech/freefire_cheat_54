package com.bench.ffcheat

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.max

class OverlayView(context: Context) : View(context) {

    private val entities = mutableListOf<EspEntity>()

    // Paints
    private val boxPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        isAntiAlias = true
    }
    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        isAntiAlias = true
        setShadowLayer(3f, 1f, 1f, Color.BLACK)
    }
    private val hpBgPaint = Paint().apply {
        color = Color.argb(180, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val snaplinePaint = Paint().apply {
        color = Color.argb(120, 255, 255, 0)
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }

    // Caixa ao redor do inimigo — tamanho proporcional à distância
    // Distância estimada: quanto menor o tamanho na tela, mais longe
    private val BOX_HEAD_RATIO = 0.5f  // metade da altura total = head box
    private val BOX_WIDTH_RATIO = 0.6f // 60% da altura = largura

    fun updateEntities(list: List<EspEntity>) {
        synchronized(entities) {
            entities.clear()
            entities.addAll(list)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width  * 0.5f
        val cy = height * 0.5f

        synchronized(entities) {
            for (e in entities) {
                // Cor baseada em HP: vermelho mortal → verde cheio
                val hpRatio = if (e.maxHealth > 0) (e.health / e.maxHealth).coerceIn(0f, 1f) else 0f
                val boxColor = hpColor(hpRatio)
                boxPaint.color = boxColor

                // Estima tamanho da caixa pela posição Y na tela
                // Mais perto do centro vertical = mais distante
                val distFactor = max(0.3f, 1.0f - (e.screenY / height))
                val boxH  = 180f * distFactor
                val boxW  = boxH * BOX_WIDTH_RATIO
                val headH = boxH * BOX_HEAD_RATIO

                // Caixa principal (corpo)
                val left   = e.screenX - boxW * 0.5f
                val top    = e.screenY - headH
                val right  = e.screenX + boxW * 0.5f
                val bottom = e.screenY + boxH - headH

                // Desenha caixa em 4 cantos (corner box — mais limpo)
                drawCornerBox(canvas, left, top, right, bottom, boxW * 0.25f)

                // Barra de HP
                drawHpBar(canvas, left - 8f, top, bottom, hpRatio)

                // Snapline do centro da tela até os pés do inimigo
                canvas.drawLine(cx, cy, e.screenX, bottom, snaplinePaint)

                // HP em texto
                val hpText = "${e.health.toInt()}/${e.maxHealth.toInt()}"
                canvas.drawText(hpText, left, top - 6f, textPaint)
            }
        }

        // Círculo de FOV do aimbot no centro da tela
        val fovPaint = Paint().apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        canvas.drawCircle(cx, cy, 350f, fovPaint)
    }

    // ── Corner box ──────────────────────────────────────────────────────────
    private fun drawCornerBox(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, len: Float) {
        // Top-left
        canvas.drawLine(l, t, l + len, t, boxPaint)
        canvas.drawLine(l, t, l, t + len, boxPaint)
        // Top-right
        canvas.drawLine(r - len, t, r, t, boxPaint)
        canvas.drawLine(r, t, r, t + len, boxPaint)
        // Bottom-left
        canvas.drawLine(l, b - len, l, b, boxPaint)
        canvas.drawLine(l, b, l + len, b, boxPaint)
        // Bottom-right
        canvas.drawLine(r - len, b, r, b, boxPaint)
        canvas.drawLine(r, b - len, r, b, boxPaint)
    }

    // ── HP bar vertical ──────────────────────────────────────────────────────
    private fun drawHpBar(canvas: Canvas, x: Float, top: Float, bottom: Float, ratio: Float) {
        val barW = 6f
        val totalH = bottom - top
        canvas.drawRect(x - barW, top, x, bottom, hpBgPaint)
        fillPaint.color = hpColor(ratio)
        val fillTop = bottom - totalH * ratio
        canvas.drawRect(x - barW, fillTop, x, bottom, fillPaint)
    }

    // ── Cor HP ──────────────────────────────────────────────────────────────
    private fun hpColor(ratio: Float): Int {
        // Verde (1.0) → Amarelo (0.5) → Vermelho (0.0)
        return when {
            ratio > 0.6f -> Color.rgb(50, 220, 50)
            ratio > 0.3f -> Color.rgb(220, 220, 50)
            else         -> Color.rgb(220, 50, 50)
        }
    }
}

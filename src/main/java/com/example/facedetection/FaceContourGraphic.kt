package com.example.facedetection

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class FaceContourGraphic(overlay: GraphicOverlay, private val rect: RectF) : GraphicOverlay.Graphic(overlay) {

    private val boxPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

    override fun draw(canvas: Canvas?) {
        // Scale and translate the coordinates
        val left = translateX(rect.left)
        val top = translateY(rect.top)
        val right = translateX(rect.right)
        val bottom = translateY(rect.bottom)
        canvas!!.drawRect(left, top, right, bottom, boxPaint)
    }
}

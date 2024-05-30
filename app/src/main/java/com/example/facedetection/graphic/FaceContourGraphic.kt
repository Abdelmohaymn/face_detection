package com.example.facedetection.graphic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/**
 * FaceContourGraphic is responsible for drawing a bounding box around a detected face.
 *
 * @param overlay The GraphicOverlay on which this graphic will be drawn.
 * @param rect The bounding box rectangle for the detected face.
 */
class FaceContourGraphic(overlay: GraphicOverlay, private val rect: RectF) : GraphicOverlay.Graphic(overlay) {

    // Paint object for drawing the bounding box
    private val boxPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = BOX_STROKE_WIDTH
    }

    companion object {
        private const val BOX_STROKE_WIDTH = 5.0f
    }

    /**
     * Draws the bounding box on the supplied canvas.
     *
     * @param canvas The canvas on which the bounding box will be drawn.
     */
    override fun draw(canvas: Canvas?) {
        // Scale and translate the coordinates
        val left = translateX(rect.left)
        val top = translateY(rect.top)
        val right = translateX(rect.right)
        val bottom = translateY(rect.bottom)

        // Draw the bounding box
        canvas?.drawRect(left, top, right, bottom, boxPaint)
    }
}

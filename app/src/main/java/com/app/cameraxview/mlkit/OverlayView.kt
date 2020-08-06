package com.app.cameraxview.mlkit

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

/**
 * Created by Rahul on 29/07/20.
 */
class OverlayView(c: Context?) : View(c) {
    private var mRect: Rect? = null
    private var mText: String? = null
    fun setOverlay(rect: Rect?, text: String?) {
        mRect = rect
        mText = text
    }

    override fun onDraw(canvas: Canvas) {
        if (mRect != null) {
            val p = Paint()
            p.color = Color.RED
            p.style = Paint.Style.STROKE
            p.strokeWidth = 4.5f
            canvas.drawRect(mRect!!, p)
            if (mText != null) {
                p.textSize = 80f
                canvas.drawText(mText!!, mRect!!.left.toFloat(), mRect!!.bottom + 90.toFloat(), p)
            }
        }
    }
}
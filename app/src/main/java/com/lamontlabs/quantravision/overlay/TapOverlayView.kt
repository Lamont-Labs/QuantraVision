package com.lamontlabs.quantravision.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import timber.log.Timber

class TapOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var onTap: (() -> Unit)? = null
    var onLongPress: (() -> Unit)? = null
    
    private val gestureDetector: GestureDetector
    
    init {
        setBackgroundColor(Color.TRANSPARENT)
        isClickable = true
        isFocusable = false
        
        val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                Timber.d("TapOverlayView: Single tap detected at (${e.x}, ${e.y})")
                onTap?.invoke()
                return true
            }
            
            override fun onLongPress(e: MotionEvent) {
                Timber.d("TapOverlayView: Long press detected at (${e.x}, ${e.y})")
                onLongPress?.invoke()
            }
            
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }
        }
        
        gestureDetector = GestureDetector(context, gestureListener)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val handled = gestureDetector.onTouchEvent(event)
        return handled || super.onTouchEvent(event)
    }
    
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
    }
}

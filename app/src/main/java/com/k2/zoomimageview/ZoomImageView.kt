package com.k2.zoomimageview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.absoluteValue

/**
 * @author Kislay [k.two.apps@gmail.com]
 * @since 03/09/20
 */

const val MAX_SCALE = 3F
const val MIN_SCALE = 1F
const val MID_SCALE = 1.75F

class ZoomImageView : androidx.appcompat.widget.AppCompatImageView {

    private var oldScale = MIN_SCALE
    private lateinit var tapDetector: GestureDetector
    private lateinit var scaleDetector: ScaleGestureDetector
    private var touchSlop: Float = 0F
    private val zoomMatrix = Matrix()
    private val baseMatrix = Matrix()
    private val drawMatrix = Matrix()
    private val displayRect = RectF()
    private var zoomAnimator: ValueAnimator? = null
    private var handlingTouch = false
    var onDrawableLoaded: () -> Unit = {}
    private val textPaint = Paint()
    private var logText = ""
    private val matrixValues = FloatArray(9)
    var debugInfoVisible = false

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        initView()
    }

    private fun initView() {
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
        initTextPaint()
        scaleType = ScaleType.MATRIX
        scaleDetector = ScaleGestureDetector(context, scaleListener)
        tapDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                oldScale = currentScale
                val scaleFactor = if (currentScale != MIN_SCALE) MIN_SCALE else MID_SCALE
                setScaleAbsolute(scaleFactor, e.x, e.y)
                return true
            }

            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float
            ): Boolean {
                if (scaleDetector.isInProgress || currentScale <= MIN_SCALE) return false
                panImage(distanceX, distanceY)
                return (distanceX.absoluteValue > touchSlop || distanceY.absoluteValue > touchSlop)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return tapDetector.onTouchEvent(event)
                || return scaleDetector.onTouchEvent(event) || return true
    }

    private fun setZoom(scale: Float, x: Float, y: Float) {
        zoomMatrix.postScale(scale, scale, x, y)
        setBounds()
        updateMatrix()
    }

    private fun updateMatrix() {
        getDrawMatrix()
        logText = "tX: $currentTransX tY: $currentTransY"
        logText += " Scale: $currentScale"
        imageMatrix = drawMatrix
    }

    private fun setScale(scale: Float, x: Float, y: Float) {
        setZoom(scale, x, y)
    }

    private fun setScaleAbsolute(scale: Float, x: Float, y: Float) {
        cancelAnimation()
        animateZoom(oldScale, scale, x, y)
    }

    private inline val drawableWidth: Int
        get() = drawable?.intrinsicWidth ?: 0

    private inline val drawableHeight: Int
        get() = drawable?.intrinsicHeight ?: 0

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null) {
            onDrawableLoaded.invoke()
            resetZoom()
            zoomMatrix.set(imageMatrix)
        }
    }

    private fun resetZoom() {
        val mTempSrc = RectF(0F, 0F, drawableWidth.toFloat(), drawableHeight.toFloat())
        val mTempDst = RectF(0F, 0F, width.toFloat(), height.toFloat())
        baseMatrix.setRectToRect(mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER)
        setScaleAbsolute(MIN_SCALE, width / 2F, height / 2F)
        imageMatrix = baseMatrix
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            handlingTouch = true
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector.scaleFactor.isNaN() || detector.scaleFactor.isInfinite())
                return false
            if (currentScale > MAX_SCALE && detector.scaleFactor > 1F)
                return false
            oldScale = currentScale
            setScale(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            oldScale = currentScale
            var needsReset = false
            var newScale = MIN_SCALE
            if (currentScale < MIN_SCALE) {
                newScale = MIN_SCALE
                needsReset = true
            }
            if (needsReset) setScaleAbsolute(newScale, detector.focusX, detector.focusY)
            handlingTouch = false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (debugInfoVisible) {
            canvas.drawText(logText, 10F, height - 10F, textPaint)
            val drawableBound = getDisplayRect()?.let {
                "Drawable: $it"
            } ?: ""
            canvas.drawText(drawableBound, 10F, 40F, textPaint)
        }
    }

    private fun initTextPaint() {
        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 40F
    }

    private fun animateZoom(startZoom: Float, endZoom: Float, x: Float, y: Float) {
        zoomAnimator = ValueAnimator.ofFloat(startZoom, endZoom).apply {
            duration = 300
            addUpdateListener {
                val scale = (it.animatedValue as Float) / currentScale
                setZoom(scale, x, y)
            }
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun cancelAnimation() {
        zoomAnimator?.removeAllUpdateListeners()
        zoomAnimator?.cancel()
    }

    private fun panImage(distanceX: Float, distanceY: Float) {
        zoomMatrix.postTranslate(-distanceX, -distanceY)
        setBounds()
        updateMatrix()
    }

    private fun getDisplayRect(): RectF? {
        drawable?.let { d ->
            displayRect.set(
                0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat()
            )
            getDrawMatrix().mapRect(displayRect)
            return displayRect
        }
        return null
    }

    private fun getDrawMatrix(): Matrix {
        drawMatrix.set(baseMatrix)
        drawMatrix.postConcat(zoomMatrix)
        return drawMatrix;
    }

    private fun setBounds() {
        val rect = getDisplayRect() ?: return
        val height = rect.height()
        val width = rect.width()
        val viewHeight: Int = this.height
        var deltaX = 0f
        var deltaY = 0f
        when {
            height <= viewHeight -> {
                deltaY = (viewHeight - height) / 2 - rect.top
            }
            rect.top > 0 -> {
                deltaY = -rect.top
            }
            rect.bottom < viewHeight -> {
                deltaY = viewHeight - rect.bottom
            }
        }
        val viewWidth: Int = this.width
        when {
            width <= viewWidth -> {
                deltaX = (viewWidth - width) / 2 - rect.left
            }
            rect.left > 0 -> {
                deltaX = -rect.left
            }
            rect.right < viewWidth -> {
                deltaX = viewWidth - rect.right
            }
        }
        zoomMatrix.postTranslate(deltaX, deltaY)
    }

    private inline val currentScale: Float
        get() {
            zoomMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MSCALE_X]
        }

    private inline val currentTransX: Float
        get() {
            zoomMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MTRANS_X]
        }

    private inline val currentTransY: Float
        get() {
            zoomMatrix.getValues(matrixValues)
            return matrixValues[Matrix.MTRANS_Y]
        }

}

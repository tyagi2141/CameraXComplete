package com.app.cameraxview.mlkit

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.PreviewCallback
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * Created by Rahul on 29/07/20.
 */
public class CameraView(context: Context?, camera: Camera?) :
    SurfaceView(context), SurfaceHolder.Callback {
    // Preview display parameters (by portrait mode)
    private var mPreviewSize: Camera.Size? = null

    // Instances
    private var mCamera: Camera?
    private var mPreviewCallback: PreviewCallback? = null
    fun setPreviewCallback(previewCallback: PreviewCallback?) {
        mPreviewCallback = previewCallback
    }

    fun getPreviewSize(): Camera.Size? {
        return mPreviewSize
    }
    /** Calculate preview size to fit output screen  */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originalWidth = MeasureSpec.getSize(widthMeasureSpec).toDouble()
        val originalHeight = MeasureSpec.getSize(heightMeasureSpec).toDouble()

        // Switch width and height size for portrait preview screen.
        // Because the camera stream size always assume landscape size.
        var DISPLAY_WIDTH = CameraView.PREVIEW_HEIGHT
        var DISPLAY_HEIGHT = CameraView.PREVIEW_WIDTH
        if (mPreviewSize != null) {
            DISPLAY_WIDTH = mPreviewSize!!.height
            DISPLAY_HEIGHT = mPreviewSize!!.width
        }

        // Consider calculated size is overflow
        val calculatedHeight = (originalWidth * DISPLAY_HEIGHT / DISPLAY_WIDTH).toInt()
        val finalWidth: Int
        val finalHeight: Int
        if (calculatedHeight > originalHeight) {
            finalWidth = (originalHeight * DISPLAY_WIDTH / DISPLAY_HEIGHT).toInt()
            finalHeight = originalHeight.toInt()
        } else {
            finalWidth = originalWidth.toInt()
            finalHeight = calculatedHeight
        }

        // Set new measures
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        try {
            mCamera!!.setPreviewDisplay(holder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        try {
            mCamera!!.stopPreview()
            mCamera!!.setPreviewCallback(null)
            mCamera!!.setPreviewDisplay(null)
            mCamera = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder?,
        format: Int,
        w: Int,
        h: Int
    ) {
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            // Start set-up camera configurations
            val parameters = mCamera!!.parameters

            // Set image format
            parameters.previewFormat = ImageFormat.NV21

            // Set preview size (find suitable size with configurations)
            mPreviewSize = findSuitablePreviewSize(parameters.supportedPreviewSizes)
            parameters.setPreviewSize(mPreviewSize!!.width, mPreviewSize!!.height)

            // Set Auto-Focusing if is available.
            if (parameters.supportedFocusModes
                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)
            ) {
                parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }

            // Adapt parameters
            mCamera!!.parameters = parameters

            // Set Screen-Mode portrait
            mCamera!!.setDisplayOrientation(SCREEN_ORIENTATION)

            // Set preview callback
            // When the preview updated, 'onPreviewFrame()' function is called.
            mCamera!!.setPreviewCallback(mPreviewCallback)

            // Show preview images
            mCamera!!.startPreview()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Find suitable preview scren size
     * If the value of PREVIEW_WIDTH and PREVIEW_HEIGHT are not supported on chosen camera,
     * find a size value that the most similar size and ratio.
     */
     open fun findSuitablePreviewSize(supportedPreviewSize: List<Camera.Size>): Camera.Size? {
        var previewSize: Camera.Size? = null
        val originalAspectRatio =
            PREVIEW_WIDTH.toDouble() / PREVIEW_HEIGHT.toDouble()
        var lastFit = Double.MAX_VALUE
        var currentFit: Double
        for (s in supportedPreviewSize) {
            if (s.width == PREVIEW_WIDTH && s.height == PREVIEW_HEIGHT) {
                previewSize = s
                break
            } else if (previewSize == null) {
                lastFit =
                    Math.abs(s.width.toDouble() / s.height.toDouble() - originalAspectRatio)
                previewSize = s
            } else {
                currentFit =
                    Math.abs(s.width.toDouble() / s.height.toDouble() - originalAspectRatio)
                if (currentFit <= lastFit && Math.abs(PREVIEW_WIDTH - s.width) <= Math.abs(
                        PREVIEW_WIDTH - previewSize.width
                    )
                ) {
                    previewSize = s
                    lastFit = currentFit
                }
            }
        }
        return previewSize
    }
    companion object {
        // Camera configuration values
        const val PREVIEW_WIDTH = 1280
        const val PREVIEW_HEIGHT = 720
        const val SCREEN_ORIENTATION = 90
    }

    init {
        mCamera = camera
        val holder = holder
        holder.addCallback(this)
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }
}
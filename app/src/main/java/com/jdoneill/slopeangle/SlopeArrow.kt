package com.jdoneill.slopeangle

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.view.View
import com.jdoneill.slopeangle.R.drawable
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * The implementation of slope angle arrow. Thee mode of operation for rotating
 * the arrow is using device motion sensors.
 */
class SlopeArrow(// context
    private val mContext: Context
) : View(mContext),
    SensorEventListener {
    // sensors
    private val sensorManager: SensorManager
    private val mListener: SensorEventListener? = null
    private var mGravity: FloatArray?
    private var mGeomagnetic: FloatArray?
    private var slopeAngle = 0f
    // bitmap
    private val arrowBitmap: Bitmap?
    private val arrowMatrix: Matrix
    private val arrowPaint: Paint
    /**
     * Draws the slope arrow at the current angle of slope
     */
    override fun onDraw(canvas: Canvas) {
        // reset the matrix to default values

        arrowMatrix.reset()
        // pass the current slope angle to the matrix


        arrowMatrix.postRotate(slopeAngle)
        // use the matrix to draw the bitmap


        canvas.drawBitmap(arrowBitmap, arrowMatrix, arrowPaint)
        super.onDraw(canvas)
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        // Try for a width based on our minimum

        val minw = paddingLeft + paddingRight + suggestedMinimumWidth
        val w =
            Math.max(minw, MeasureSpec.getSize(widthMeasureSpec))

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can


        val minh = paddingTop + paddingBottom
        val h =
            Math.min(MeasureSpec.getSize(heightMeasureSpec), minh)
        setMeasuredDimension(w, h)
    }

    /**
     * Updates the slope angle, in degrees, such that
     * the slope arrow is drawn within view
     */
    private fun setRotationAngle(angle: Double) {
        // save the angle
        slopeAngle = angle.toFloat()
// force the arrow to re-paint itself
        postInvalidate()
    }

    /**
     * Unregisters the sensor listener if it is registered.
     */
    fun unregisterListeners() {
        sensorManager.unregisterListener(mListener)
        Log.i(TAG, "Sensor listener unregistered.")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) mGravity =
            event.values
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) mGeomagnetic =
            event.values
        if (mGravity != null && mGeomagnetic != null) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            val success =
                SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
            if (success) {
                val outR = FloatArray(9)
                SensorManager.remapCoordinateSystem(
                    R,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Y,
                    outR
                )
                val orientation = FloatArray(3)
                SensorManager.getOrientation(outR, orientation)
                val smoothOrientation = FloatArray(3)
                // pitch angle


                val pitch =
                    Math.toDegrees(smoothOrientation[1].toDouble()).toFloat()
                // pitch is positive no matter which way device is tilted


                var pitchAngle = Math.abs(pitch).toDouble()
                // round angle to 2 decimal places

                pitchAngle = round(pitchAngle, 2)
                val pitchAngleString = Double.toString(pitchAngle)
                setRotationAngle(pitchAngle)
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor,
        accuracy: Int
    ) {
    }

    companion object {
        private const val TAG = "SlopeArrowSensor"
        /**
         * Rounds a double to specified number of decimal places.
         * Note the rounding mode is UP.
         *
         * @param value double to be rounded
         * @param places number of decimal places to round to.
         * @return
         */
        private fun round(value: Double, places: Int): Double {
            if (places < 0) throw IllegalArgumentException()
            var bd = BigDecimal(value)
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.doubleValue()
        }
    }

    init {
        setWillNotDraw(false)

        // create the bitmap drawable

        arrowBitmap = BitmapFactory.decodeResource(
            resources,
            drawable.ic_arrow_downward_black_24dp
        )
        arrowMatrix = Matrix()
        arrowPaint = Paint()
        sensorManager =
            mContext.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        val accSensor: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor: Sensor? =
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
}
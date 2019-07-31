package com.jdoneill.slopeangle;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * The implementation of slope angle arrow. Thee mode of operation for rotating
 * the arrow is using device motion sensors.
 */
public class SlopeArrow extends View implements SensorEventListener{
    private static final String TAG = "SlopeArrowSensor";

    // context
    private Context mContext;

    // sensors
    private SensorManager sensorManager;
    private Sensor aSensor;
    private Sensor gSensor;
    private SensorEventListener mListener;

    private float[] mGravity;
    private float[] mGeomagnetic;
    private float slopeAngle;

    // bitmap
    private Bitmap arrowBitmap;
    private Matrix arrowMatrix;
    private Paint arrowPaint;

    public SlopeArrow(Context context){
        super(context);
        this.mContext = context;
        this.setWillNotDraw(false);

        // create the bitmap drawable
        arrowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_downward_black_24dp);
        arrowMatrix = new Matrix();
        arrowPaint = new Paint();

        // Get the default sensor for the sensor type from the SensorManager
        sensorManager = (SensorManager)mContext.getSystemService(Activity.SENSOR_SERVICE);
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

    }

    /**
     * Draws the slope arrow at the current angle of slope
     */
    protected void onDraw(Canvas canvas){
        // reset the matrix to default values
        arrowMatrix.reset();
        // pass the current slope angle to the matrix
        arrowMatrix.postRotate(slopeAngle);
        // use the matrix to draw the bitmap
        canvas.drawBitmap(arrowBitmap, arrowMatrix, arrowPaint);

        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try for a width based on our minimum
        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();

        int w = Math.max(minw, MeasureSpec.getSize(widthMeasureSpec));

        // Whatever the width ends up being, ask for a height that would let the pie
        // get as big as it can
        int minh = getPaddingTop() + getPaddingBottom();
        int h = Math.min(MeasureSpec.getSize(heightMeasureSpec), minh);

        setMeasuredDimension(w, h);
    }

    /**
     * Updates the slope angle, in degrees, such that
     * the slope arrow is drawn within view
     */
    private void setRotationAngle(double angle){
        // save the angle
        slopeAngle = (float)angle;
        // force the arrow to re-paint itself
        postInvalidate();
    }

    /**
     * Rounds a double to specified number of decimal places.
     * Note the rounding mode is UP.
     *
     * @param value double to be rounded
     * @param places number of decimal places to round to.
     * @return
     */
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Unregisters the sensor listener if it is registered.
     */
    public void unregisterListeners() {
        sensorManager.unregisterListener(mListener);
        Log.i(TAG, "Sensor listener unregistered.");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;

        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if(mGravity != null && mGeomagnetic != null){
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if(success){
                float[] outR = new float[9];
                SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);

                float orientation[] = new float[3];
                SensorManager.getOrientation(outR, orientation);
                float smoothOrientation[] = new float[3];
                // pitch angle
                float pitch = (float)Math.toDegrees(smoothOrientation[1]);
                // pitch is positive no matter which way device is tilted
                double pitchAngle = (double) Math.abs(pitch);
                // round angle to 2 decimal places
                pitchAngle = round(pitchAngle, 2);
                String pitchAngleString = Double.toString(pitchAngle);

                setRotationAngle(pitchAngle);

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

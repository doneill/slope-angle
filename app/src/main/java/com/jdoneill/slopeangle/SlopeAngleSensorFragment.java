package com.jdoneill.slopeangle;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A placeholder fragment containing a simple view.
 */
public class SlopeAngleSensorFragment extends Fragment {

    private static final String TAG = "SlopeAngleSensor";

    // Views
    private View fragView;
    // Layout container
    private TextView slopeAngleTV;
    private ImageView slopeArrow;
    // drawables
    private Bitmap arrowBitmap;

    /**
     * Default constructor
     */
    public SlopeAngleSensorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // register sensor event listeners
        int mMaxDelay = 0;
        registerEventListener(mMaxDelay, Sensor.TYPE_ACCELEROMETER);
        registerEventListener(mMaxDelay, Sensor.TYPE_MAGNETIC_FIELD);

        // inflate fragment layout to access views
        fragView = inflater.inflate(R.layout.fragment_main, container, false);

        slopeAngleTV = (TextView)fragView.findViewById(R.id.slopeText);
        slopeArrow = (ImageView)fragView.findViewById(R.id.slopeArrow);
        arrowBitmap = BitmapFactory.decodeResource(fragView.getResources(), R.drawable.ic_arrow_downward_black_24dp);

        return fragView;
    }


    /**
     * Register a {@link android.hardware.SensorEventListener} for the sensor and max batch delay.
     * The maximum batch delay specifies the maximum duration in microseconds for which subsequent
     * sensor events can be temporarily stored by the sensor before they are delivered to the
     * registered SensorEventListener. A larger delay allows the system to handle sensor events more
     * efficiently, allowing the system to switch to a lower power state while the sensor is
     * capturing events. Once the max delay is reached, all stored events are delivered to the
     * registered listener. Note that this value only specifies the maximum delay, the listener may
     * receive events quicker. A delay of 0 disables batch mode and registers the listener in
     * continuous mode.
     * The optimum batch delay depends on the application. For example, a delay of 5 seconds or
     * higher may be appropriate for an  application that does not update the UI in real time.
     *
     * @param maxdelay
     * @param sensorType
     */
    private void registerEventListener(int maxdelay, int sensorType) {

        // Get the default sensor for the sensor type from the SenorManager
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        // sensorType is either Sensor.TYPE_ACCELEROMETER or Sensor.TYPE_MAGNETIC_FIELD
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);

        // Register the listener for this sensor
        sensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Listener that handles sensor events.
     */
    private final SensorEventListener mListener = new SensorEventListener() {

        float[] mGravity;
        float[] mGeomagnetic;

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

                    // pitch angle
                    float pitch = (float)Math.toDegrees(orientation[1]);
                    // pitch is positive no matter which way device is tilted
                    double pitchAngle = (double) Math.abs(pitch);
                    // round angle to 2 decimal places
                    pitchAngle = round(pitchAngle, 2);
                    // update string
                    String pitchAngleString = Double.toString(pitchAngle);
                    slopeAngleTV.setText(pitchAngleString + (char)0x00B0);
                    // point arrow in direction of slope angle
                    slopeArrow.setRotation(pitch);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // not implemented
        }
    };


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
        SensorManager sensorManager =
                (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(mListener);
        Log.i(TAG, "Sensor listener unregistered.");
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterListeners();
    }
}

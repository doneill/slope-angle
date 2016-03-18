package com.jdoneill.slopeangle;

import android.app.Activity;
import android.app.Fragment;
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

/**
 * A placeholder fragment containing a simple view.
 */
public class SlopeAngleSensorFragment extends Fragment {

    private static final String TAG = "SlopeAngleSensor";

    // Views
    private TextView slopeAngleTV;
    private ImageView slopeAngleImage;
    // images
    private int greenCircle;
    private int blueSquare;
    private int blackDiamond;
    private int dblBlackDiamond;


    public SlopeAngleSensorFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // register sensor event listeners
        int mMaxDelay = 0;
        registerEventListener(mMaxDelay, Sensor.TYPE_ACCELEROMETER);
        registerEventListener(mMaxDelay, Sensor.TYPE_MAGNETIC_FIELD);

        // inflate fragment layout to access views
        View fragView = inflater.inflate(R.layout.fragment_main, container, false);

        slopeAngleTV = (TextView)fragView.findViewById(R.id.text1);
        slopeAngleImage = (ImageView)fragView.findViewById(R.id.slopeAngleImage);

        // get resource id values to dynamically update
        greenCircle = fragView.getResources().getIdentifier("com.jdoneill.slopeangle:mipmap/ic_green_circle", null, null);
        blueSquare = fragView.getResources().getIdentifier("com.jdoneill.slopeangle:mipmap/ic_blue_square", null, null);
        blackDiamond = fragView.getResources().getIdentifier("com.jdoneill.slopeangle:mipmap/ic_black_diamond", null, null);
        dblBlackDiamond = fragView.getResources().getIdentifier("com.jdoneill.slopeangle:mipmap/ic_double_black_diamond", null, null);

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
     * Listener that handles step sensor events for step detector and step counter sensors.
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
                    double pitchAngle = (double) Math.abs(pitch);

                    String pitchAngleString = Double.toString(pitchAngle);

                    // set slope angle and color ratings
                    if(pitchAngle >= 56.3){
                        slopeAngleTV.setText(pitchAngleString);
                        slopeAngleImage.setImageResource(dblBlackDiamond);
                    }else if(pitchAngle < 56.3 && pitchAngle > 36.9){
                        slopeAngleTV.setText(pitchAngleString);
                        slopeAngleImage.setImageResource(blackDiamond);
                    }else if(pitchAngle < 36.9 && pitchAngle > 18.7){
                        slopeAngleTV.setText(pitchAngleString);
                        slopeAngleImage.setImageResource(blueSquare);
                    }else if(pitchAngle < 18.7){
                        slopeAngleTV.setText(pitchAngleString);
                        slopeAngleImage.setImageResource(greenCircle);
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

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

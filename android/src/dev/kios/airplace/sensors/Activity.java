package dev.kios.airplace.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.AndroidPreferences;

import static android.content.Context.SENSOR_SERVICE;

public class Activity implements SensorEventListener {
    // Constants
    private static final int STANDING_DELAY = 50;

    // Context
    private final Context mContext;
    private final Preferences preferences;

    // Sensors
    private final Sensor mAccelerometerSensor;
    private final SensorManager mSensorManager;
    // Listener
    private final ActivityListener mActivityListener;

    // Status
    private final float mYOffset;
    private final float[] mScale;
    private final float[] mLastDiff;
    private final float[] mLastValues;
    private final float[] mLastDirections;
    private final float[][] mLastExtremes;

    private int noStep;
    private int mLastMatch;
    private float sensitivity;
    private MovementState preState = MovementState.WALK;
    private MovementState currentState = MovementState.STILL;

    private Activity(Context context, ActivityListener activityListener) {
        mContext = context;
        // Sensors
        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Listener
        mActivityListener = activityListener;

        // Status
        preferences = Gdx.app.getPreferences("AirPlacePreferences");
        sensitivity = 30 - preferences.getInteger("WalkingSensitivity", 26);

        noStep = 0;
        mLastMatch = -1;

        int h = 480;
        mYOffset = h * 0.5f;

        mScale = new float[2];
        mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));

        mLastDiff = new float[6];
        mLastValues = new float[6];
        mLastDirections = new float[6];
        mLastExtremes = new float[6][6];
    }

    public static Activity newInstance(Context context, ActivityListener activityListener) {
        Activity activity = new Activity(context, activityListener);
        if (activity.hasRequiredSensors()) {
            return activity;
        } else {
            return null;
        }
    }

    // Check that the device has the required sensors
    private boolean hasRequiredSensors() {
        if (mAccelerometerSensor != null) {
            return true;
        } else {
            Toast.makeText(mContext, "The device does not have the required sensors", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void start() {
        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float vSum = 0;
                /* Sum x, y, z axis values */
                for (int i = 0; i < 3; i++) {
                    final float v = mYOffset + event.values[i] * mScale[1];
                    vSum += v;
                }
                int k = 0;
                float v = vSum / 3;

                float direction = (Float.compare(v, mLastValues[k]));
                if (direction == -mLastDirections[k]) {
                    /* Direction changed minimum or maximum? */
                    int extType = (direction > 0 ? 0 : 1);
                    mLastExtremes[extType][k] = mLastValues[k];
                    float diff = Math.abs(mLastExtremes[extType][k] - mLastExtremes[1 - extType][k]);

                    /* Passed the threshold sensitivity? */
                    sensitivity = 30 - preferences.getInteger("WalkingSensitivity", 26);
                    if (diff > sensitivity) {
                        boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
                        boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
                        boolean isNotContra = (mLastMatch != 1 - extType);

                        if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
                            noStep = 0;
                            if (currentState != MovementState.WALK) {
                                currentState = MovementState.WALK;
                            }
                            mLastMatch = extType;
                        } else {
                            mLastMatch = -1;
                        }
                    } else {
                        noStep++;
                        if (currentState != MovementState.STILL && noStep > STANDING_DELAY) {
                            currentState = MovementState.STILL;
                        }
                    }
                    mLastDiff[k] = diff;
                }
                mLastDirections[k] = direction;
                mLastValues[k] = v;

                if (preState != currentState) {
                    mActivityListener.onActivityChanged(currentState);
                    preState = currentState;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public enum MovementState {
        WALK("Walk"), STILL("Still");

        private final String state;

        MovementState(String name) {
            this.state = name;
        }

        @Override
        public String toString() {
            return state;
        }
    }

    public interface ActivityListener {
        void onActivityChanged(MovementState movementState);
    }
}

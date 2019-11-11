package com.kios.airplace;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.text.DecimalFormat;
import java.util.Locale;

public class AndroidFragment extends FragmentActivity implements AndroidFragmentApplication.Callbacks, SensorEventListener {
	private static final int STANDING_DELAY = 50;
	private static final float SENSITIVITY = 4.5f;

	// Magnetic values that are passed to Logger and to Navigator
	public static float[] magnetometerReading;

	// Method used: movementDetector
	// Variables to calculate if the user is walking or not
	// Source: https://github.com/dmsl/anyplace/blob/master/android/Anyplace/src/com/dmsl/anyplace/sensors/MovementDetector.java
	private int noStep;
	private int mLastMatch;
	private float mYOffset;
	private float[] mScale;
	private float[] mLastDiff;
	private float[] mLastValues;
	private float[] mLastDirections;
	private float[][] mLastExtremes;

	// Method used: onSensorChanged
	// Variables to calculate the current orientation
	// Source: https://developer.android.com/guide/topics/sensors/sensors_position
	private float[] rotationMatrix;
	private float[] orientationAngles;
	private float[] accelerometerReading;

	// Variables to change the view of the menu
	private Menu menu;
	private TextView loggerTextView;
	private LinearLayout loggerLayout;
	private SensorManager sensorManager;

	public AndroidFragment() {
		magnetometerReading = new float[3];

		int h = 480;
		noStep = 0;
		mLastMatch = -1;
		mYOffset = h * 0.5f;

		mScale = new float[2];
		mLastDiff = new float[6];
		mLastValues = new float[6];
		mLastDirections = new float[6];
		mLastExtremes = new float[][]{new float[6], new float[6]};

		mScale[0] = -(h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
		mScale[1] = -(h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));

		rotationMatrix = new float[9];
		orientationAngles = new float[3];
		accelerometerReading = new float[3];
	}

	void changeNavigatorState() {
		MenuItem navigatorItem = menu.findItem(R.id.navigatorItem);
		navigatorItem.setTitle((Globals.NAVIGATOR) ? getString(R.string.disable_navigator) : getString(R.string.enable_navigator));
	}

	float mappingAngles(float x) {
		return (float) (x - (2 * Math.PI) * Math.floor(x / (2 * Math.PI)));
	}

	void changeVisibilityLogger() {
		MenuItem loggerItem = menu.findItem(R.id.loggerItem);

		if (Globals.LOGGER) {
			loggerItem.setTitle(getString(R.string.hide_logger));
			loggerLayout.setVisibility(View.VISIBLE);
		} else {
			loggerItem.setTitle(getString(R.string.show_logger));
			loggerLayout.setVisibility(View.GONE);
		}
	}

	void movementDetector(SensorEvent event) {
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
			if (diff > SENSITIVITY) {
				boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff[k] * 2 / 3);
				boolean isPreviousLargeEnough = mLastDiff[k] > (diff / 3);
				boolean isNotContra = (mLastMatch != 1 - extType);

				if (isAlmostAsLargeAsPrevious && isPreviousLargeEnough && isNotContra) {
					noStep = 0;
					if (Globals.STATUS != Globals.STATE.WALKING) {
						Globals.STATUS = Globals.STATE.WALKING;
					}
					mLastMatch = extType;
				} else {
					mLastMatch = -1;
				}
			} else {
				noStep++;
				if (Globals.STATUS != Globals.STATE.STANDING && noStep > STANDING_DELAY) {
					Globals.STATUS = Globals.STATE.STANDING;
				}
			}
			mLastDiff[k] = diff;
		}
		mLastDirections[k] = direction;
		mLastValues[k] = v;
	}

	@Override
	public void exit() {

	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (accelerometer != null) {
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
		}
		Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		if (magneticField != null) {
			sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (!Globals.LOGGER && !Globals.NAVIGATOR)
			return;

		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			movementDetector(event);
			System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.length);
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.length);
		}

		SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
		SensorManager.getOrientation(rotationMatrix, orientationAngles);

		DecimalFormat format = new DecimalFormat("000.00");
		Globals.ORIENTATION = (float) Math.toDegrees(mappingAngles(orientationAngles[0]));
		loggerTextView.setText(String.format(Locale.getDefault(), "Orientation [%s] Status [%s] Samples [%d]", format.format(Globals.ORIENTATION), Globals.STATUS.getStatus(), Globals.RECORDED_SAMPLES));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_android_fragment);
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.menu_android_fragment);

		menu = toolbar.getMenu();
		loggerLayout = findViewById(R.id.loggerLayout);
		loggerTextView = findViewById(R.id.loggerTextView);
		final Button button = findViewById(R.id.loggerButton);

		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				int id = item.getItemId();

				switch (id) {
					case R.id.preferencesItem:
						startActivity(new Intent(AndroidFragment.this, PreferencesActivity.class));
						return true;
					case R.id.navigatorItem:
						Globals.NAVIGATOR = !Globals.NAVIGATOR;
						changeNavigatorState();
						return true;
					case R.id.loggerItem:
						if (button.getText().equals("Stop")) {
							Toast.makeText(getApplicationContext(), "Stop Logger", Toast.LENGTH_SHORT).show();
							return true;
						}
						Globals.LOGGER = !Globals.LOGGER;
						changeVisibilityLogger();
						return true;
					case R.id.aboutItem:
						Toast.makeText(getApplicationContext(), "About", Toast.LENGTH_SHORT).show();
						return true;
					case R.id.debugItem:
						Globals.DEBUG = !Globals.DEBUG;
						return true;
					default:
						return false;
				}
			}
		});

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (button.getText().equals("Start")) {
					if (!Globals.CAN_DISTRIBUTE) {
						Toast.makeText(getApplicationContext(), "Click at a position...", Toast.LENGTH_SHORT).show();
						return;
					}
					button.setText(getString(R.string.stop));
					Globals.ENABLE_LOGGING = true;
				} else {
					button.setText(R.string.start);
					Globals.WIFI_LIST.clear();
					Globals.RECORDED_SAMPLES = 0;
					Globals.MAGNETIC_FIELD.clear();
					Globals.ENABLE_LOGGING = false;
				}
			}
		});

		changeNavigatorState();
		changeVisibilityLogger();
		LibGdxFragment libGdxFragment = new LibGdxFragment();
		getSupportFragmentManager().beginTransaction().add(R.id.libGdxFragment, libGdxFragment).commit();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
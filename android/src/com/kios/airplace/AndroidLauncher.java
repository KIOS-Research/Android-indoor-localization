package com.kios.airplace;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

public class AndroidLauncher extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_android_launcher);

		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 0) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

				SharedPreferences pref = getApplicationContext().getSharedPreferences("Preferences", MODE_PRIVATE);
				Globals.SELECTED_ALGORITHM = pref.getInt("SELECTED_ALGORITHM", 2);
				Globals.SHOW_MARKER_LOCATION[0] = pref.getBoolean("SHOW_MARKER_LOCATION_0", true);
				Globals.SHOW_MARKER_LOCATION[1] = pref.getBoolean("SHOW_MARKER_LOCATION_1", true);
				Globals.SHOW_MARKER_LOCATION[2] = pref.getBoolean("SHOW_MARKER_LOCATION_2", false);
				Globals.SHOW_MARKER_LOCATION[3] = pref.getBoolean("SHOW_MARKER_LOCATION_3", false);

				Globals.PARTICLES_NUMBER = pref.getInt("PARTICLES_NUMBER", Integer.parseInt(getString(R.string.particles_number)));
				Globals.PARTICLES_NUMBER_TO_SHOW = pref.getInt("PARTICLES_NUMBER_TO_SHOW", Integer.parseInt(getString(R.string.particles_number_to_show)));

				Globals.STEP_LENGTH = pref.getFloat("STEP_LENGTH", Float.parseFloat(getString(R.string.step_length_number)));
				Globals.INITIAL_VARIANCE = pref.getFloat("INITIAL_VARIANCE", Float.parseFloat(getString(R.string.initial_variance_number)));
				Globals.SYSTEM_NOISE.setEntry(0, 0, pref.getFloat("SYSTEM_NOISE_00", Float.parseFloat(getString(R.string.system_noise_array_00))));
				Globals.SYSTEM_NOISE.setEntry(0, 1, pref.getFloat("SYSTEM_NOISE_01", Float.parseFloat(getString(R.string.system_noise_array_01))));
				Globals.SYSTEM_NOISE.setEntry(1, 0, pref.getFloat("SYSTEM_NOISE_10", Float.parseFloat(getString(R.string.system_noise_array_10))));
				Globals.SYSTEM_NOISE.setEntry(1, 1, pref.getFloat("SYSTEM_NOISE_11", Float.parseFloat(getString(R.string.system_noise_array_11))));
				Globals.MEASUREMENTS_NOISE.setEntry(0, 0, pref.getFloat("MEASUREMENTS_NOISE_00", Float.parseFloat(getString(R.string.measurement_noise_array_00))));
				Globals.MEASUREMENTS_NOISE.setEntry(0, 1, pref.getFloat("MEASUREMENTS_NOISE_01", Float.parseFloat(getString(R.string.measurement_noise_array_01))));
				Globals.MEASUREMENTS_NOISE.setEntry(1, 0, pref.getFloat("MEASUREMENTS_NOISE_10", Float.parseFloat(getString(R.string.measurement_noise_array_10))));
				Globals.MEASUREMENTS_NOISE.setEntry(1, 1, pref.getFloat("MEASUREMENTS_NOISE_11", Float.parseFloat(getString(R.string.measurement_noise_array_11))));

				Globals.RSS_NAV_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + Globals.RSS_NAV_FILE_PATH;
				Globals.MAGNETIC_NAV_FILE_PATH = Environment.getExternalStorageDirectory().getPath() + Globals.MAGNETIC_NAV_FILE_PATH;

				startActivity(new Intent(this, AndroidFragment.class));
			} else {
				finish();
			}
		}
	}
}
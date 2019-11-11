package com.kios.airplace;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;

import java.text.MessageFormat;

public class PreferencesActivity extends AppCompatActivity {
	private Dialog dialog;

	public void savePreferences() {
		SharedPreferences pref = getApplicationContext().getSharedPreferences("Preferences", MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putInt("SELECTED_ALGORITHM", Globals.SELECTED_ALGORITHM);
		editor.putBoolean("SHOW_MARKER_LOCATION_0", Globals.SHOW_MARKER_LOCATION[0]);
		editor.putBoolean("SHOW_MARKER_LOCATION_1", Globals.SHOW_MARKER_LOCATION[1]);
		editor.putBoolean("SHOW_MARKER_LOCATION_2", Globals.SHOW_MARKER_LOCATION[2]);
		editor.putBoolean("SHOW_MARKER_LOCATION_3", Globals.SHOW_MARKER_LOCATION[3]);

		editor.putInt("PARTICLES_NUMBER", Globals.PARTICLES_NUMBER);
		editor.putInt("PARTICLES_NUMBER_TO_SHOW", Globals.PARTICLES_NUMBER_TO_SHOW);

		editor.putFloat("STEP_LENGTH", Globals.STEP_LENGTH);
		editor.putFloat("INITIAL_VARIANCE", Globals.INITIAL_VARIANCE);
		editor.putFloat("SYSTEM_NOISE_00", (float) Globals.SYSTEM_NOISE.getEntry(0, 0));
		editor.putFloat("SYSTEM_NOISE_01", (float) Globals.SYSTEM_NOISE.getEntry(0, 1));
		editor.putFloat("SYSTEM_NOISE_10", (float) Globals.SYSTEM_NOISE.getEntry(1, 0));
		editor.putFloat("SYSTEM_NOISE_11", (float) Globals.SYSTEM_NOISE.getEntry(1, 1));
		editor.putFloat("MEASUREMENTS_NOISE_00", (float) Globals.MEASUREMENTS_NOISE.getEntry(0, 0));
		editor.putFloat("MEASUREMENTS_NOISE_01", (float) Globals.MEASUREMENTS_NOISE.getEntry(0, 1));
		editor.putFloat("MEASUREMENTS_NOISE_10", (float) Globals.MEASUREMENTS_NOISE.getEntry(1, 0));
		editor.putFloat("MEASUREMENTS_NOISE_11", (float) Globals.MEASUREMENTS_NOISE.getEntry(1, 1));

		editor.apply();
	}

	public void okButtonClicked(View view) {
		//Positioning Algorithms
		RadioGroup radioGroup = findViewById(R.id.rgPositioningAlgorithms);
		int selectedRadioButton = radioGroup.getCheckedRadioButtonId();

		switch (selectedRadioButton) {
			case R.id.rbKNN:
				Globals.SELECTED_ALGORITHM = 1;
				break;
			case R.id.rbWKNN:
				Globals.SELECTED_ALGORITHM = 2;
				break;
			case R.id.rbMAP:
				Globals.SELECTED_ALGORITHM = 3;
				break;
			case R.id.rbMMSE:
				Globals.SELECTED_ALGORITHM = 4;
				break;
		}

		//MarkerLocation
		CheckBox checkBox = findViewById(R.id.cbPoints);
		Globals.SHOW_RECORDED_POINTS = checkBox.isChecked();

		checkBox = findViewById(R.id.cbRss);
		Globals.SHOW_MARKER_LOCATION[0] = checkBox.isChecked();

		checkBox = findViewById(R.id.cbMagnetic);
		Globals.SHOW_MARKER_LOCATION[1] = checkBox.isChecked();

		checkBox = findViewById(R.id.cbFused);
		Globals.SHOW_MARKER_LOCATION[2] = checkBox.isChecked();

		checkBox = findViewById(R.id.cbPF);
		Globals.SHOW_MARKER_LOCATION[3] = checkBox.isChecked();

		//Particle Filter
		if (Globals.SHOW_MARKER_LOCATION[3]) {
			Button dialogButton = dialog.findViewById(R.id.btnDone);

			dialogButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText editText = dialog.findViewById(R.id.etParticlesNumber);
					if (!editText.getText().toString().equals("")) {
						Globals.PARTICLES_NUMBER = Integer.parseInt(editText.getText().toString());
					}

					editText = dialog.findViewById(R.id.etParticlesToShow);
					if (!editText.getText().toString().equals("")) {
						Globals.PARTICLES_NUMBER_TO_SHOW = Integer.parseInt(editText.getText().toString());
					}

					editText = dialog.findViewById(R.id.etStepLength);
					if (!editText.getText().toString().equals("")) {
						Globals.STEP_LENGTH = Float.parseFloat(editText.getText().toString());
					}

					editText = dialog.findViewById(R.id.etInitialVariance);
					if (!editText.getText().toString().equals("")) {
						Globals.INITIAL_VARIANCE = Float.parseFloat(editText.getText().toString());
					}

					editText = dialog.findViewById(R.id.etSystemNoise00);
					if (!editText.getText().toString().equals("")) {
						Globals.SYSTEM_NOISE.setEntry(0, 0, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etSystemNoise01);
					if (!editText.getText().toString().equals("")) {
						Globals.SYSTEM_NOISE.setEntry(0, 1, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etSystemNoise10);
					if (!editText.getText().toString().equals("")) {
						Globals.SYSTEM_NOISE.setEntry(1, 0, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etSystemNoise11);
					if (!editText.getText().toString().equals("")) {
						Globals.SYSTEM_NOISE.setEntry(1, 1, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etMeasurementNoise00);
					if (!editText.getText().toString().equals("")) {
						Globals.MEASUREMENTS_NOISE.setEntry(0, 0, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etMeasurementNoise01);
					if (!editText.getText().toString().equals("")) {
						Globals.MEASUREMENTS_NOISE.setEntry(0, 1, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etMeasurementNoise10);
					if (!editText.getText().toString().equals("")) {
						Globals.MEASUREMENTS_NOISE.setEntry(1, 0, Float.parseFloat(editText.getText().toString()));
					}

					editText = dialog.findViewById(R.id.etMeasurementNoise11);
					if (!editText.getText().toString().equals("")) {
						Globals.MEASUREMENTS_NOISE.setEntry(1, 1, Float.parseFloat(editText.getText().toString()));
					}

					dialog.dismiss();
					savePreferences();
					startActivity(new Intent(getApplicationContext(), AndroidFragment.class));
				}
			});
			dialog.show();
		} else {
			savePreferences();
			startActivity(new Intent(this, AndroidFragment.class));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);

		dialog = new Dialog(this);
		dialog.setContentView(R.layout.dialog_particle_filter);

		//Positioning Algorithms
		RadioButton radioButton;
		switch (Globals.SELECTED_ALGORITHM) {
			case 1:
				radioButton = findViewById(R.id.rbKNN);
				radioButton.setChecked(true);
				break;
			case 2:
				radioButton = findViewById(R.id.rbWKNN);
				radioButton.setChecked(true);
				break;
			case 3:
				radioButton = findViewById(R.id.rbMAP);
				radioButton.setChecked(true);
				break;
			case 4:
				radioButton = findViewById(R.id.rbMMSE);
				radioButton.setChecked(true);
				break;
		}

		//MarkerLocation
		CheckBox checkBox = findViewById(R.id.cbPoints);
		checkBox.setChecked(Globals.SHOW_RECORDED_POINTS);

		checkBox = findViewById(R.id.cbRss);
		checkBox.setChecked(Globals.SHOW_MARKER_LOCATION[0]);

		checkBox = findViewById(R.id.cbMagnetic);
		checkBox.setChecked(Globals.SHOW_MARKER_LOCATION[1]);

		checkBox = findViewById(R.id.cbFused);
		checkBox.setChecked(Globals.SHOW_MARKER_LOCATION[2]);

		checkBox = findViewById(R.id.cbPF);
		checkBox.setChecked(Globals.SHOW_MARKER_LOCATION[3]);

		//Particle Filter
		EditText editText = dialog.findViewById(R.id.etParticlesNumber);
		editText.setText(String.valueOf(Globals.PARTICLES_NUMBER));

		editText = dialog.findViewById(R.id.etParticlesToShow);
		editText.setText(String.valueOf(Globals.PARTICLES_NUMBER_TO_SHOW));

		editText = dialog.findViewById(R.id.etStepLength);
		editText.setText(String.valueOf(Globals.STEP_LENGTH));

		editText = dialog.findViewById(R.id.etInitialVariance);
		editText.setText(String.valueOf(Globals.INITIAL_VARIANCE));

		editText = dialog.findViewById(R.id.etSystemNoise00);
		editText.setText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(0, 0)));

		editText = dialog.findViewById(R.id.etSystemNoise01);
		editText.setText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(0, 1)));

		editText = dialog.findViewById(R.id.etSystemNoise10);
		editText.setText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(1, 0)));

		editText = dialog.findViewById(R.id.etSystemNoise11);
		editText.setText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(1, 1)));

		editText = dialog.findViewById(R.id.etMeasurementNoise00);
		editText.setText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(0, 0)));

		editText = dialog.findViewById(R.id.etMeasurementNoise01);
		editText.setText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(0, 1)));

		editText = dialog.findViewById(R.id.etMeasurementNoise10);
		editText.setText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(1, 0)));

		editText = dialog.findViewById(R.id.etMeasurementNoise11);
		editText.setText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(1, 1)));

		//Total Points
		TextView textView = findViewById(R.id.tvTotalPoints);
		textView.setText(MessageFormat.format("{0}: {1}", getString(R.string.total_points), Globals.TOTAL_POINTS));
	}
}
package com.kios.airplace;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.util.Objects;

public class LibGdxFragment extends AndroidFragmentApplication {

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();

		WiFiMagnetic wiFiMagnetic = new WiFiMagnetic(Objects.requireNonNull(getContext()));
		return initializeForView(new AirPlace(wiFiMagnetic), configuration);
	}
}
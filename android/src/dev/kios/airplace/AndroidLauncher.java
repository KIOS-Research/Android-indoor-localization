package dev.kios.airplace;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import dev.kios.airplace.sensors.Activity;
import dev.kios.airplace.sensors.Compass;
import dev.kios.airplace.sensors.Wifi;

import java.util.ArrayList;

public class AndroidLauncher extends AndroidApplication implements Compass.CompassListener, Activity.ActivityListener, Wifi.WifiListener {
    private Wifi mWifi;
    private Compass mCompass;
    private Activity mActivity;
    private AndroidSpecificCode androidSpecificCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        androidSpecificCode = new AndroidSpecificCode();

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useImmersiveMode = true;
        config.useAccelerometer = false;
        initialize(new CoreLauncher(androidSpecificCode), config);

        mWifi = Wifi.newInstance(getApplicationContext(), this);
        mCompass = Compass.newInstance(getApplicationContext(), this);
        mActivity = Activity.newInstance(getApplicationContext(), this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWifi != null) mWifi.stop();
        if (mCompass != null) mCompass.stop();
        if (mActivity != null) mActivity.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWifi != null) mWifi.start();
        if (mCompass != null) mCompass.start();
        if (mActivity != null) mActivity.start();
    }

    @Override
    public void onActivityChanged(Activity.MovementState movementState) {
        androidSpecificCode.setActivity(movementState.toString());
    }

    @Override
    public void onOrientationChanged(float azimuth, float[] magneticField) {
        androidSpecificCode.setOrientation(azimuth);
        androidSpecificCode.setMagneticField(magneticField);
    }

    @Override
    public void onWifiChanged(ArrayList<LogRecord> scanResults) {
        androidSpecificCode.setScanResults(scanResults);
    }
}
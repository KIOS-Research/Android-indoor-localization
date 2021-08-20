package dev.kios.airplace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class DesktopSpecificCode implements CoreSpecificCode {
    private LogFileReader logFileReader;
    Vector2 point;
    String activity;
    float orientation;
    float[] magneticField;
    ArrayList<LogRecord> scanResults;

    public DesktopSpecificCode() {
        point = new Vector2();
        magneticField = new float[3];
        scanResults = new ArrayList<>();
    }

    @Override
    public Vector2 getPoint() {
        return point;
    }

    @Override
    public String getActivity() {
        return activity;
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    @Override
    public float[] getMagneticField() {
        return magneticField;
    }

    @Override
    public ArrayList<LogRecord> getScanResults() {
        return scanResults;
    }

    @Override
    public boolean getLogSample() {
        if (logFileReader == null) {
            Preferences preferences = Gdx.app.getPreferences("AirPlacePreferences");
            logFileReader = new LogFileReader(preferences.getString("RouteFile"));
        }

        LogSample logSample = logFileReader.scanNewSample();

        if (logSample == null) {
            return false;
        }

        point.x = logSample.x;
        point.y = logSample.y;
        activity = logSample.status;
        orientation = logSample.heading;
        magneticField = logSample.magneticField.clone();

        scanResults.clear();
        scanResults.addAll(logSample.wiFi);

        return true;
    }
}
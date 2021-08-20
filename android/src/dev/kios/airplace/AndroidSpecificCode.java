package dev.kios.airplace;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class AndroidSpecificCode implements CoreSpecificCode {
    private final ArrayList<LogRecord> scanResults;

    private String activity;
    private float orientation;
    private float[] magneticField;

    public AndroidSpecificCode() {
        magneticField = new float[3];
        scanResults = new ArrayList<>();
    }

    void setActivity(String activity) {
        this.activity = activity;
    }

    @Override
    public Vector2 getPoint() {
        return null; // TODO
    }

    @Override
    public String getActivity() {
        return activity;
    }

    void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    @Override
    public float getOrientation() {
        return orientation;
    }

    void setMagneticField(float[] magneticField) {
        this.magneticField = magneticField.clone();
    }

    @Override
    public float[] getMagneticField() {
        return magneticField;
    }

    public void setScanResults(ArrayList<LogRecord> scanResults) {
        this.scanResults.clear();
        this.scanResults.addAll(scanResults);
    }

    @Override
    public ArrayList<LogRecord> getScanResults() {
        return scanResults;
    }

    @Override
    public boolean getLogSample() {
        return false; // TODO
    }
}
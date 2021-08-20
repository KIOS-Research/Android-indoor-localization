package dev.kios.airplace;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public interface CoreSpecificCode {

    Vector2 getPoint();

    String getActivity();

    float getOrientation();

    float[] getMagneticField();

    ArrayList<LogRecord> getScanResults();

    boolean getLogSample();
}
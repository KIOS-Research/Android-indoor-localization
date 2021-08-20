package dev.kios.airplace;

import java.util.ArrayList;

public class LogSample implements Cloneable {
    long timeStamp;
    float x;
    float y;
    float heading;
    String status;
    float[] magneticField;
    ArrayList<LogRecord> wiFi;

    public LogSample() {
        wiFi = new ArrayList<>();
        magneticField = new float[3];
    }

    @Override
    public LogSample clone() throws CloneNotSupportedException {
        return (LogSample) super.clone();
    }
}

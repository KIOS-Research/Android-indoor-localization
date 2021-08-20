package dev.kios.airplace;

public class LogRecord {
    private final String BSSID;
    private final int level;

    public LogRecord(String BSSID, int level) {
        this.BSSID = BSSID;
        this.level = level;
    }

    public String getBSSID() {
        return BSSID;
    }

    public int getLevel() {
        return level;
    }

    public String toString() {
        return BSSID + " " + level;
    }
}
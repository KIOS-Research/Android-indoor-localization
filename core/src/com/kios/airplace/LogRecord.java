package com.kios.airplace;

public class LogRecord {
	private int level;
	private String BSSID;

	LogRecord(String BSSID, int level) {
		super();
		this.BSSID = BSSID;
		this.level = level;
	}

	int getLevel() {
		return level;
	}

	String getBSSID() {
		return BSSID;
	}

	public String toString() {
		return BSSID + " " + level + "\n";
	}
}
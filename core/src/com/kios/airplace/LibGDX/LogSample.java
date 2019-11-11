package com.kios.airplace.LibGDX;

import java.util.ArrayList;

public class LogSample implements Cloneable {
	public float x, y;
	public String status;
	public long timeStamp;
	public float orientation;
	public ArrayList<String> BSSID;
	public ArrayList<Integer> level;
	public ArrayList<Float> magnetic;

	public LogSample() {
		BSSID = new ArrayList<>();
		level = new ArrayList<>();
		magnetic = new ArrayList<>();
	}

	public LogSample clone() throws CloneNotSupportedException {
		return (LogSample) super.clone();
	}
}
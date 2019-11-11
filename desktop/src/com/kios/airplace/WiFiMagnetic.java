package com.kios.airplace;

import com.badlogic.gdx.math.Vector2;

import java.io.BufferedReader;
import java.io.FileReader;

public class WiFiMagnetic implements WiFiMagneticListener {
	private boolean EOF;
	private BufferedReader rssReader;
	private BufferedReader magneticReader;

	WiFiMagnetic() {
		try {
			rssReader = new BufferedReader(new FileReader(Globals.RSS_LOG_FILE_PATH));
			magneticReader = new BufferedReader(new FileReader(Globals.MAGNETIC_LOG_FILE_PATH));

			rssReader.readLine();
			magneticReader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scan() {
		try {
			Globals.WIFI_LIST.clear();
			Globals.MAGNETIC_FIELD.clear();

			if (Globals.SHOW_NEXT && !EOF) {
				String rssLine = rssReader.readLine();
				String magneticLine = magneticReader.readLine();

				while (!rssLine.startsWith("#")) {
					Globals.WIFI_LIST.add(new LogRecord(rssLine.split(" ")[4], Integer.parseInt(rssLine.split(" ")[5])));
					rssLine = rssReader.readLine();

					if (rssLine == null)
						break;
				}

				if (magneticLine.startsWith("#"))
					magneticLine = magneticReader.readLine();

				Globals.REAL_LOCATION[0][0] = Double.parseDouble(magneticLine.split(" ")[1]);
				Globals.REAL_LOCATION[1][0] = Double.parseDouble(magneticLine.split(" ")[2]);

				Globals.ORIENTATION = Float.parseFloat(magneticLine.split(" ")[3]);
				Globals.MAGNETIC_FIELD.add(Float.parseFloat(magneticLine.split(" ")[4]));
				Globals.MAGNETIC_FIELD.add(Float.parseFloat(magneticLine.split(" ")[5]));
				Globals.MAGNETIC_FIELD.add(Float.parseFloat(magneticLine.split(" ")[6]));

				Globals.SHOW_NEXT = false;
				Globals.ONE_TIME_SCAN = true;
				Globals.NEW_SCAN_AVAILABLE = true;

				Globals.rlPoints.add(new Vector2(Float.parseFloat(magneticLine.split(" ")[1]), Float.parseFloat(magneticLine.split(" ")[2])));
			}
		} catch (Exception e) {
			e.printStackTrace();
			EOF = true;
		}
	}
}
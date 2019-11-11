package com.kios.airplace.Positioning;

import com.kios.airplace.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class MagneticRadioMap {
	private HashMap<String, MagneticValues> magneticLocationHashMap;

	public MagneticRadioMap() throws Exception {
		magneticLocationHashMap = new HashMap<>();

		if (!ConstructRadioMap(new File(Globals.MAGNETIC_NAV_FILE_PATH))) {
			throw new Exception("Invalid magneticRadioMapFile");
		}
	}

	private boolean ConstructRadioMap(File inFile) {
		if (!inFile.exists() || !inFile.canRead()) {
			return false;
		}

		String[] temp;
		String line, key;
		MagneticValues magneticValues;
		BufferedReader reader = null;
		magneticLocationHashMap.clear();

		try {
			reader = new BufferedReader(new FileReader(inFile));

			// Read the first line
			reader.readLine();

			while ((line = reader.readLine()) != null) {
				temp = line.split(", ");

				if (temp.length != 6)
					return false;

				key = temp[0] + " " + temp[1];
				magneticValues = new MagneticValues(Float.parseFloat(temp[3]), Float.parseFloat(temp[4]), Float.parseFloat(temp[5]));
				magneticLocationHashMap.put(key, magneticValues);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return true;
	}

	public HashMap<String, MagneticValues> getMagneticLocationHashMap() {
		return magneticLocationHashMap;
	}
}
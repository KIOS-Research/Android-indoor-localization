package com.kios.airplace.Positioning;

import com.kios.airplace.Globals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RssRadioMap {
	private String NaN;
	private ArrayList<String> macAddressList;
	private HashMap<String, ArrayList<String>> rssLocationHashMap;

	public RssRadioMap() throws Exception {
		NaN = "-110";
		macAddressList = new ArrayList<>();
		rssLocationHashMap = new HashMap<>();

		if (!ConstructRadioMap(new File(Globals.RSS_NAV_FILE_PATH))) {
			throw new Exception("Invalid rssRadioMapFile");
		}
	}

	public String getNaN() {
		return NaN;
	}

	private boolean ConstructRadioMap(File inFile) {
		if (!inFile.exists() || !inFile.canRead()) {
			return false;
		}

		macAddressList.clear();
		rssLocationHashMap.clear();

		String[] temp;
		String key, line;
		ArrayList<String> RSS_Values;
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(inFile));

			// Read the first line # NaN -110
			line = reader.readLine();
			temp = line.split(" ");
			if (!temp[1].equals("NaN"))
				return false;
			NaN = temp[2];
			line = reader.readLine();

			// Must exists
			if (line == null)
				return false;

			line = line.replace(", ", " ");
			temp = line.split(" ");

			final int startOfRSS = 4;

			// Must have more than 4 fields
			if (temp.length < startOfRSS)
				return false;

			// Store all Mac Addresses Heading Added
			macAddressList.addAll(Arrays.asList(temp).subList(startOfRSS, temp.length));

			while ((line = reader.readLine()) != null) {
				if (line.trim().equals(""))
					continue;

				line = line.replace(", ", " ");
				temp = line.split(" ");

				if (temp.length < startOfRSS)
					return false;

				key = temp[0] + " " + temp[1];
				RSS_Values = new ArrayList<>(Arrays.asList(temp).subList(startOfRSS - 1, temp.length));

				// Equal number of MAC address and RSS Values
				if (macAddressList.size() != RSS_Values.size())
					return false;

				rssLocationHashMap.put(key, RSS_Values);
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

	public ArrayList<String> getMacAddressList() {
		return macAddressList;
	}

	public HashMap<String, ArrayList<String>> getRssLocationHashMap() {
		return rssLocationHashMap;
	}
}
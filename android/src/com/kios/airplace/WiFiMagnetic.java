package com.kios.airplace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.util.List;

// Source: https://developer.android.com/guide/topics/connectivity/wifi-scan
public class WiFiMagnetic extends FragmentActivity implements AndroidFragmentApplication.Callbacks, WiFiMagneticListener {
	private Context context;
	private WifiManager wifiManager;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!Globals.NEW_SCAN_AVAILABLE) {
				List<ScanResult> scanResultList = wifiManager.getScanResults();
				context.unregisterReceiver(this);

				for (ScanResult scanResult : scanResultList) {
					Globals.WIFI_LIST.add(new LogRecord(scanResult.BSSID, scanResult.level));
				}

				Globals.MAGNETIC_FIELD.add(AndroidFragment.magnetometerReading[0]);
				Globals.MAGNETIC_FIELD.add(AndroidFragment.magnetometerReading[1]);
				Globals.MAGNETIC_FIELD.add(AndroidFragment.magnetometerReading[2]);

				Globals.NEW_SCAN_AVAILABLE = true;
			}
		}
	};

	WiFiMagnetic(Context context) {
		this.context = context;
		wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

		if (!wifiManager.isWifiEnabled()) {
			Toast.makeText(context.getApplicationContext(), "Enabling WiFi", Toast.LENGTH_LONG).show();
			wifiManager.setWifiEnabled(true);
		}
	}

	@Override
	public void exit() {

	}

	@Override
	public void scan() {
		if (!Globals.ONE_TIME_SCAN) {
			Globals.WIFI_LIST.clear();
			Globals.MAGNETIC_FIELD.clear();
			context.registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			wifiManager.startScan();
			Globals.ONE_TIME_SCAN = true;
		}
	}
}
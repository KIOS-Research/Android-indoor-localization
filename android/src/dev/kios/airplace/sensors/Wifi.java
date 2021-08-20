package dev.kios.airplace.sensors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import dev.kios.airplace.LogRecord;

import java.util.ArrayList;
import java.util.List;

public class Wifi extends BroadcastReceiver {
    private final Context mContext;
    private final WifiManager wifiManager;
    private final IntentFilter intentFilter;
    private final WifiListener mWifiListener;

    private ArrayList<LogRecord> lastScanResults;

    private Wifi(Context context, WifiListener wifiListener) {
        mContext = context;
        // Sensors
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Listener
        mWifiListener = wifiListener;

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(this, intentFilter);

        wifiManager.startScan();
    }

    public static Wifi newInstance(Context context, WifiListener wifiListener) {
        return new Wifi(context, wifiListener);
    }

    public void start() {
        mContext.registerReceiver(this, intentFilter);
        wifiManager.startScan();
    }

    public void stop() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
        if (success) {
            scanSuccess();
        } else {
            scanFailure();
        }

        wifiManager.startScan();
    }

    private void scanSuccess() {
        List<ScanResult> scanResults = wifiManager.getScanResults();

        if (lastScanResults == null) {
            lastScanResults = new ArrayList<>();
            for (ScanResult scanResult : scanResults) {
                lastScanResults.add(new LogRecord(scanResult.BSSID, scanResult.level));
            }
        } else {
            ArrayList<LogRecord> newScanResults = new ArrayList<>();
            for (ScanResult scanResult : scanResults) {
                newScanResults.add(new LogRecord(scanResult.BSSID, scanResult.level));
            }

            if (lastScanResults.equals(newScanResults))
                return;
            lastScanResults = newScanResults;
        }

        mWifiListener.onWifiChanged(lastScanResults);
    }

    private void scanFailure() {
        Toast.makeText(mContext, "Scan Failure. Scanning again…", Toast.LENGTH_SHORT).show();
    }

    public interface WifiListener {
        void onWifiChanged(ArrayList<LogRecord> scanResults);
    }
}

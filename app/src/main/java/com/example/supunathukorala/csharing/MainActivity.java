package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends Activity {

    TextView mainText;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    String[] values;
    StringBuilder sb = new StringBuilder();
    ListView listView;
    ArrayAdapter<String> adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainText = (TextView) findViewById(R.id.mainText);
        listView = (ListView) findViewById(R.id.list);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Check for wifi is disabled
        if (!wifiManager.isWifiEnabled()) {

            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            wifiManager.setWifiEnabled(true);
        }

        receiverWifi = new WifiReceiver();

        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        mainText.setText("Starting Scan...");

        wifiList = wifiManager.getScanResults();
        values = new String[wifiList.size()];
        sb.append("\n        Number Of Wifi connections :").append(wifiList.size()).append("\n\n");

        for (int i = 0; i < wifiList.size(); i++) {
            values[i] = wifiList.get(i).SSID;
        }

        mainText.setText(sb);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                String itemValue = (String) listView.getItemAtPosition(position);

                Toast.makeText(getApplicationContext(),
                          (position +1) + " Network Name : " + itemValue, Toast.LENGTH_LONG)
                        .show();

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = wifiList.get(position).SSID;


                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();


            }
        });
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    // Broadcast receiver class called its receive method
    // when number of wifi connections changed

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            sb = new StringBuilder();
            wifiList = wifiManager.getScanResults();
            values = new String[wifiList.size()];
            sb.append("\n        Number Of Wifi connections :").append(wifiList.size()).append("\n\n");

            for (int i = 0; i < wifiList.size(); i++) {
                values[i] = wifiList.get(i).SSID;
            }
            mainText.setText(sb);
            listView.setAdapter(adapter);
        }

    }

}
package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


public class MainActivity2 extends Activity {

    Button buttonScan;
    Button buttonConnect;

    ///// ------------------------------------ NETWORK CREDENTIALS

    String networkSSID = "SP-5521 1529";
    String networkPass = "12345678";
    WifiManager wifiManager ;


    ///// ------------------------------------ NETWORK CREDENTIALS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity22);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        buttonScan = (Button) findViewById(R.id.button_Scan);
        buttonConnect = (Button)findViewById(R.id.button_Connect);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(),
                    "wifi is disabled..making it enabled", Toast.LENGTH_LONG)
                    .show();
            wifiManager.setWifiEnabled(true);
        }

        ///// ---------------------------- Connecting Code

        buttonScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                wifiManager.startScan();
                for (ScanResult sx : wifiManager.getScanResults()) {
                    System.out.println("Point - " + sx);
                }
            }
        });
        buttonConnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                // setup a wifiManager configuration
                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = "\"SP-5521 1529\"";
                wc.preSharedKey = "\"12345678\"";
                wc.status = WifiConfiguration.Status.ENABLED;
                wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                // connect to and enable the connection

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                for( WifiConfiguration i : list ) {
                    int netId = wifiManager.addNetwork(wc);
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.setWifiEnabled(true);
                }
            }
        });
    }
}

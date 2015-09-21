package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView mainText;
    private String networkSSID = "c-Sharing";
    private String networkPass = "pass";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainText);
        Button chatButton = (Button) findViewById(R.id.buttonchat);
        Button createNetworkButton = (Button) findViewById(R.id.buttonCreate);
        Button joinNetworkButton = (Button) findViewById(R.id.buttonJoin);
        Button privateChatButton = (Button) findViewById(R.id.buttonPrivate);
        chatButton.setOnClickListener(this);
        createNetworkButton.setOnClickListener(this);
        joinNetworkButton.setOnClickListener(this);
        privateChatButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonchat:
                Intent i = new Intent(MainActivity.this, MessageActivity.class);
                EditText user = (EditText) findViewById(R.id.userName);
                i.putExtra("name", user.getText().toString());
                startActivity(i);
                break;
            case R.id.buttonCreate:
                CreateWifiAccessPoint createOne = new CreateWifiAccessPoint();
                createOne.execute((Void) null);
                break;
            case R.id.buttonJoin:
                JoinWifiNetwork joinOne = new JoinWifiNetwork();
                joinOne.execute((Void) null);
                break;
            case R.id.buttonPrivate:
                 i = new Intent(MainActivity.this, ClientSelectActivity.class);
                 user = (EditText) findViewById(R.id.userName);
                i.putExtra("name", user.getText().toString());
                startActivity(i);



        }

    }

    private class CreateWifiAccessPoint extends AsyncTask<Void, Void, Boolean> {
        {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
            boolean methodFound = false;
            for (Method method : wmMethods) {
                if (method.getName().equals("setWifiApEnabled")) {
                    methodFound = true;
                    WifiConfiguration netConfig = new WifiConfiguration();
                    netConfig.SSID=networkSSID;
                    netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                    netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                    netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                    netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    try {
                        final boolean apStatus = (Boolean) method.invoke(wifiManager, netConfig, true);
                        for (Method isWifiApEnabledMethod : wmMethods)
                            if (isWifiApEnabledMethod.getName().equals("isWifiApEnabled")) {
                                while (!(Boolean) isWifiApEnabledMethod.invoke(wifiManager)) {
                                }
                                for (Method method1 : wmMethods) {
                                    if (method1.getName().equals("getWifiApState")) {
                                    }
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (apStatus) {
                                        System.out.println("SUCCESS ");
                                    Toast.makeText(getApplicationContext(),"Wifi Hotspot Created",Toast.LENGTH_SHORT).show();
                                    } else {
                                        System.out.println("FAILED");
                                        Toast.makeText(getApplicationContext(),"Wifi Hotspot Creation Failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            return methodFound;

        }
    }

    private class JoinWifiNetwork extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

            WifiManager wifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.addNetwork(conf);
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
                wifiManager.startScan();
            }

            int netId = wifiManager.addNetwork(conf);
                wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"Joined to "+networkSSID,Toast.LENGTH_SHORT).show();
                    System.out.println("SUCCESS ");
                }
            });
            return null;
        }
    }
}
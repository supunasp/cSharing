package com.example.supunathukorala.csharing;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
<<<<<<< HEAD
import android.widget.Button;
import android.widget.EditText;
=======
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
>>>>>>> origin/master
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

<<<<<<< HEAD
    private TextView mainText;
=======
public class MainActivity extends Activity implements View.OnClickListener {

    TextView mainText;
    WifiManager wifiManager;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    String[] values;
    StringBuilder sb = new StringBuilder();
    ListView listView;
    ArrayAdapter<String> adapter;
>>>>>>> origin/master

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainText = (TextView) findViewById(R.id.mainText);
<<<<<<< HEAD
        Button chatButton = (Button) findViewById(R.id.buttonchat);
        chatButton.setOnClickListener(this);
=======
        listView = (ListView) findViewById(R.id.list);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Check for wifiManager is disabled
        if (!wifiManager.isWifiEnabled()) {

            Toast.makeText(getApplicationContext(), "wifiManager is disabled..making it enabled",
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
                        (position + 1) + " Network Name : " + itemValue, Toast.LENGTH_LONG)
                        .show();

                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = wifiList.get(position).SSID;
                wifiConfig.wepKeys[0] = "\"" + 12345678 + "\"";
                wifiConfig.wepTxKeyIndex = 0;
                wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);


                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();

                //WifiConfiguration wifiConfig = new WifiConfiguration();
                //wifiConfig.SSID = String.format("\"%s\"", ssid);
                //wifiConfig.preSharedKey = String.format("\"%s\"", key);

                WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
//remember id
                netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();

                if (wifiManager != null) {
                    WifiManager.MulticastLock lock = wifiManager.createMulticastLock("mylock");
                    lock.acquire();
                }


            }
        });
        Button chatButton = (Button) findViewById(R.id.buttonchat);
        chatButton.setOnClickListener(this);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }


    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
>>>>>>> origin/master
    }

    @Override
    public void onClick(View v) {
<<<<<<< HEAD
        Intent i = new Intent(MainActivity.this,MessageActivity.class);
        EditText user = (EditText) findViewById(R.id.userName);
        i.putExtra("name",user.getText().toString());
        startActivity(i);
=======
        Intent i = new Intent(MainActivity.this,MsgActivity.class);
        EditText user = (EditText) findViewById(R.id.userName);
        i.putExtra("name",user.getText().toString());
        startActivity(i);
    }

    // Broadcast receiver class called its receive method
    // when number of wifiManager connections changed

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifiManager connections changed
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

>>>>>>> origin/master
    }
}
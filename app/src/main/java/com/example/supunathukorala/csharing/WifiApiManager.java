package com.example.supunathukorala.csharing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
/**
 * Created by Supun Athukorala on 9/9/2015.
 */

public class WifiApiManager {
    private final WifiManager mWifiManager;

    public WifiApiManager(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }



    /**
     * Gets a list of the clients connected to the Hotspot, reachable timeout is 300
     */
    public ArrayList<ClientScanResult> getClientList(boolean onlyReachables) {
        return getClientList(onlyReachables, 300);
    }

    /**
     * Gets a list of the clients connected to the Hotspot
     */
    public ArrayList<ClientScanResult> getClientList(boolean onlyReachables, int reachableTimeout) {
        BufferedReader br = null;
        ArrayList<ClientScanResult> result = null;

        try {
            result = new ArrayList<>();
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");

                if ((splitted != null) && (splitted.length >= 4)) {
                    // Basic sanity check
                    String mac = splitted[3];

                    if (mac.matches("..:..:..:..:..:..")) {
                        boolean isReachable = InetAddress.getByName(splitted[0]).isReachable(reachableTimeout);

                        if (!onlyReachables || isReachable) {
                            result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5], isReachable));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(this.getClass().toString(), e.getMessage());
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Log.e(this.getClass().toString(), e.getMessage());
            }
        }

        return result;
    }
}
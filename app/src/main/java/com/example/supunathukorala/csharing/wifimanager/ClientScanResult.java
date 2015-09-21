package com.example.supunathukorala.csharing.wifimanager;

/**
 * Created by Supun Athukorala on 9/9/2015.
 */
public class ClientScanResult {

    private String IpAddr;

    private String HWAddr;

    private String Device;

    private boolean isReachable;

    public ClientScanResult(String ipAddress, String hWAddress, String device, boolean isReachable) {
        super();
        IpAddr = ipAddress;
        HWAddr = hWAddress;
        Device = device;
        this.setReachable(isReachable);
    }

    public String getIpAddress() {
        return IpAddr;
    }


    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    public boolean isReachable() {
        return isReachable;
    }
}
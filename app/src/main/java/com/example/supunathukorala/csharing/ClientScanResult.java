package com.example.supunathukorala.csharing;

/**
 * Created by Supun Athukorala on 9/9/2015.
 */
public class ClientScanResult {

    private String IpAddr;

    private String HWAddr;

    private String Device;

    private boolean isReachable;

    public ClientScanResult(String ipAddr, String hWAddr, String device, boolean isReachable) {
        super();
        IpAddr = ipAddr;
        HWAddr = hWAddr;
        Device = device;
        this.setReachable(isReachable);
    }

    public String getIpAddr() {
        return IpAddr;
    }

    public void setIpAddr(String ipAddr) {
        IpAddr = ipAddr;
    }

    public String getHWAddr() {
        return HWAddr;
    }

    public void setHWAddr(String hWAddr) {
        HWAddr = hWAddr;
    }

    public String getDevice() {
        return Device;
    }

    public void setDevice(String device) {
        Device = device;
    }

    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    public boolean isReachable() {
        return isReachable;
    }
}
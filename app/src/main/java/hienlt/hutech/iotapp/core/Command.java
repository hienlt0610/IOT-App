package hienlt.hutech.iotapp.core;

/**
 * Created by hienl on 12/29/2016.
 */

public class Command {
    private String action;
    private int device;
    private String deviceName;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Command() {
    }

    public Command(String action, int device) {
        this.action = action;
        this.device = device;
    }

    public String getAction() {
        return action;
    }

    public String getFullAction(){
        return action+device;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getDevice() {
        return device;
    }

    public void setDevice(int device) {
        this.device = device;
    }

    public boolean isDeviceValid(){
        return ((action != null && !action.isEmpty()) && device!=-1);
    }

    public boolean isDeviceCustom(String device){
        return deviceName.toLowerCase().contains(device.toLowerCase());
    }

    @Override
    public String toString() {
        return this.action+":"+this.device+" - "+this.deviceName;
    }
}

package hienlt.hutech.iotapp.model;

/**
 * Created by hienl on 12/15/2016.
 */

public class Device {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTurnOn() {
        return turnOn;
    }

    public void setTurnOn(boolean turnOn) {
        this.turnOn = turnOn;
    }

    private boolean turnOn;

    public Device(){}
    public Device(String deviceName){
        setName(deviceName);
    }
}

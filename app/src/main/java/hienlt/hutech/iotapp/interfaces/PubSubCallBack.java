package hienlt.hutech.iotapp.interfaces;

/**
 * Created by hienl on 12/15/2016.
 */

public interface PubSubCallBack {
    public void subscribe(String topic,int qos);
    public void publish(byte[] data, String topic, int qos);
}

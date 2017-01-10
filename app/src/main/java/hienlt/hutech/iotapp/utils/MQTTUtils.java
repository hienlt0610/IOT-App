package hienlt.hutech.iotapp.utils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Created by hienl on 1/9/2017.
 */

public class MQTTUtils {
    public static void publish(String topic, String data, MqttAndroidClient client){
        try {
            client.publish(topic,data.getBytes(),2,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}

package hienlt.hutech.iotapp.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

import hienlt.hutech.iotapp.R;
import hienlt.hutech.iotapp.adapter.DeviceAdapter;
import hienlt.hutech.iotapp.core.BaseFragment;
import hienlt.hutech.iotapp.core.Config;
import hienlt.hutech.iotapp.interfaces.IOnOffClick;
import hienlt.hutech.iotapp.interfaces.PubSubCallBack;
import hienlt.hutech.iotapp.model.Device;
import hienlt.hutech.iotapp.utils.MQTTUtils;

/**
 * Created by hienl on 12/15/2016.
 */

public class DeviceFragment extends BaseFragment implements MqttCallback, PubSubCallBack, IOnOffClick {
    RecyclerView recyclerView;
    DeviceAdapter adapter;
    ArrayList<Device> devices;
    MqttAndroidClient client;
    ProgressDialog dialog;
    MqttConnectOptions options;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_devices;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setupMQTT();
        initListDevices();
        adapter = new DeviceAdapter(getActivity(),devices, this);
        recyclerView.setAdapter(adapter);
    }

    private void initListDevices() {
        devices = new ArrayList<>();
        devices.add(new Device("Thiết bị 1"));
        devices.add(new Device("Thiết bị 2"));
        devices.add(new Device("Thiết bị 3"));
        devices.add(new Device("Thiết bị 4"));
        devices.add(new Device("Thiết bị 5"));
        devices.add(new Device("Thiết bị 6"));
        devices.add(new Device("Thiết bị 7"));
        devices.add(new Device("Thiết bị 8"));
    }

    private void setupMQTT() {
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(getActivity(), Config.SERVER,
                        clientId);
        options = new MqttConnectOptions();
        options.setUserName(Config.USER);
        options.setPassword(Config.PASSWORD.toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
    }

    private void connectMqtt() {
        try {
            IMqttToken token = client.connect(options);
            dialog.setTitle("Tải dữ liệu");
            dialog.setMessage("Tải danh sách thiết bị");
            dialog.show();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        client.subscribe(Config.TOPIC_BOARD,2);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    MQTTUtils.publish(Config.TOPIC_SERVER,"status",client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    dialog.dismiss();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(this);
    }

    @Override
    public void connectionLost(Throwable cause) {
        if(dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String data = new String(message.getPayload());
        if(data.toLowerCase().startsWith("{S".toLowerCase())){
            parseDeviceStatus(data);
        }
        adapter.notifyDataSetChanged();
        if(dialog.isShowing())
            dialog.dismiss();
    }

    private void parseDeviceStatus(String data) {
        int start = 2;
        int pos = 0;
        for(int i=start;i<start+8;i++){
            boolean isOnline = data.charAt(i) == '1' ? true : false;
            devices.get(pos).setTurnOn(isOnline);
            pos++;
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }

    @Override
    public void subscribe(String topic, int qos) {

    }

    @Override
    public void publish(byte[] data, String topic, int qos) {
        try {
            if(!client.isConnected()) return;
            client.publish(topic,data,qos,false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onOffClick(View view, int pos, boolean isOn) {
        if(client.isConnected()){
            String action = null;
            if(isOn){
                action = "Out"+(pos+1)+"=1";
            }else{
                action = "Out"+(pos+1)+"=0";
            }
            MQTTUtils.publish(Config.TOPIC_SERVER,action,client);
            dialog.setTitle("Điều khiển thiết bị");
            dialog.setMessage("Đang "+(isOn == true ? "mở" : "tắt")+" thiết bị "+(pos+1));
            dialog.show();
        }else{
            Toast.makeText(getActivity(), "Kết nối server đã có lỗi, không thể điều khiển thiết bị", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        connectMqtt();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(client.isConnected()){
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}

package hienlt.hutech.iotapp.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
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

import hienlt.hutech.iotapp.R;
import hienlt.hutech.iotapp.core.BaseFragment;
import hienlt.hutech.iotapp.core.Config;
import hienlt.hutech.iotapp.utils.MQTTUtils;

/**
 * Created by hienl on 12/15/2016.
 */

public class HomeFragment extends BaseFragment implements MqttCallback {
    TextView tvStatus,tvDeviceStatus;
    MqttAndroidClient client;
    ProgressDialog dialog;
    MqttConnectOptions options;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvStatus = (TextView) view.findViewById(R.id.tvStatus);
        tvDeviceStatus = (TextView) view.findViewById(R.id.tvDeviceStatus);
        dialog = new ProgressDialog(getActivity());
        setupMQTT();
    }

    @Override
    public void onStart() {
        super.onStart();
        connectMqttServer();
    }

    private void setupMQTT() {
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(getActivity(), Config.SERVER,
                        clientId);
        client.setCallback(this);
        options = new MqttConnectOptions();
        options.setUserName(Config.USER);
        options.setPassword(Config.PASSWORD.toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
    }

    private void connectMqttServer() {
        try {
            dialog.setTitle("Connect...");
            dialog.setMessage("Đang kết nối tới máy chủ");
            dialog.show();
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    if(dialog.isShowing())
                        dialog.dismiss();
                    tvStatus.setText("Tình trạng: Đã kết nối");
                    try {
                        client.subscribe(Config.TOPIC_BOARD,2);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    MQTTUtils.publish(Config.TOPIC_SERVER,"status",client);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    if(dialog.isShowing())
                        dialog.dismiss();
                    Toast.makeText(getActivity(), "Không thể kết nối tới server", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
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

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String data = new String(message.getPayload());
        if(data.toLowerCase().startsWith("{S".toLowerCase())){
            parseDeviceStatus(data);
        }
    }

    private void parseDeviceStatus(String data) {
        //{S000000000}
        int start = 2;
        StringBuilder builder = new StringBuilder();
        for(int i=start;i<start+8;i++){
            String status = data.charAt(i) == '1' ? "Mở" : "Tắt";
            builder.append("Thiết bị "+(i-1)+": "+status);
            if(i < start+7){
                builder.append("\r\n");
            }
        }
        tvDeviceStatus.setText(builder.toString());
    }


    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}

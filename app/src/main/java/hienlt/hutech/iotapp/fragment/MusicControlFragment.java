package hienlt.hutech.iotapp.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;

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
 * Created by hienl on 1/10/2017.
 */

public class MusicControlFragment extends BaseFragment implements MqttCallback, View.OnClickListener {
    MqttAndroidClient client;
    MqttConnectOptions options;
    ProgressDialog dialog;
    boolean isPlay = false;
    boolean isMute = false;
    int playPausePos = 10;
    int mutePos = 13;

    ImageButton btnPlayPause, btnNext, btnPrev, btnMute;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_music_control;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        setupMQTT();
    }

    private void connectMQTT() {
        try {
            IMqttToken token = client.connect(options);
            dialog.setTitle("Connecting...");
            dialog.setMessage("Đang kết nối thiết bị");
            dialog.show();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //Subscribe topic status
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
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        client.setCallback(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindView(view);
    }

    private void bindView(View view) {
        btnPlayPause = (ImageButton) view.findViewById(R.id.btnPlayPause);
        btnNext = (ImageButton) view.findViewById(R.id.btnNext);
        btnPrev = (ImageButton) view.findViewById(R.id.btnPrev);
        btnMute = (ImageButton) view.findViewById(R.id.btnMute);

        btnPlayPause.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrev.setOnClickListener(this);
        btnMute.setOnClickListener(this);
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

    @Override
    public void connectionLost(Throwable cause) {
        if(dialog.isShowing())
            dialog.dismiss();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String data = new String(message.getPayload(),"UTF-8");
        if(data.toLowerCase().startsWith("{S".toLowerCase())){
            isPlay = (data.charAt(playPausePos) == '1') ? true : false;
            isMute = (data.charAt(mutePos) == '0') ? true : false;
            updatePlayState();
            updateMuteState();
            if(dialog.isShowing())
                dialog.dismiss();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //Close progress dialog

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case  R.id.btnPlayPause:
                String action = null;
                if(isPlay){
                    MQTTUtils.publish(Config.TOPIC_SERVER,"pause",client);
                    action = "Đang tắt nhạc";
                }
                else{
                    MQTTUtils.publish(Config.TOPIC_SERVER,"play",client);
                    action = "Đang bật nhạc";
                }
                dialog.setMessage(action);
                dialog.show();
                break;
            case  R.id.btnNext:
                MQTTUtils.publish(Config.TOPIC_SERVER,"next",client);
                dialog.setMessage("Chuyển sang bài hát tiếp theo");
                dialog.show();
                break;
            case R.id.btnPrev:
                MQTTUtils.publish(Config.TOPIC_SERVER,"prev",client);
                dialog.setMessage("Trờ về bài hát trước");
                dialog.show();
                break;
            case R.id.btnMute:
                action = null;
                if(!isMute){
                    MQTTUtils.publish(Config.TOPIC_SERVER,"mute",client);
                    action = "Đang tắt âm thanh";
                }
                else{
                    MQTTUtils.publish(Config.TOPIC_SERVER,"unmute",client);
                    action = "Đang mở âm thanh";
                }
                dialog.setMessage(action);
                dialog.show();
                break;
        }
    }

    private void updatePlayState(){
        if(isPlay)
            btnPlayPause.setImageResource(R.drawable.play);
        else
            btnPlayPause.setImageResource(R.drawable.pause);
    }

    private void updateMuteState(){
        if(isMute)
            btnMute.setImageResource(R.drawable.mute);
        else
            btnMute.setImageResource(R.drawable.unmute);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        connectMQTT();
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

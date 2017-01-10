package hienlt.hutech.iotapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hienlt.hutech.iotapp.core.Command;
import hienlt.hutech.iotapp.core.Config;
import hienlt.hutech.iotapp.fragment.DeviceFragment;
import hienlt.hutech.iotapp.fragment.HomeFragment;
import hienlt.hutech.iotapp.fragment.MusicControlFragment;
import hienlt.hutech.iotapp.utils.FragmentUtils;
import hienlt.hutech.iotapp.utils.MQTTUtils;


public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener {
    MqttAndroidClient client;
    Drawer drawer;
    Toolbar toolbar;
    private static final int REQUEST_CODE = 1234;
    private String[] filterAction = {"mở","tắt","sáng","tối","bật","dừng"};
    private String[] filterDevice = {"đèn","thiết bị"};
    private String[] filterNum = {"1","2","3","4","5","6","7","8","một","hai","ba","bốn","năm","sáu","xấu","bảy","tám"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpDrawer();
        setupMQTT();
    }

    private void setupMQTT() {
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this, Config.SERVER,
                        clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(Config.USER);
        options.setPassword(Config.PASSWORD.toCharArray());
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        try {
            IMqttToken token = client.connect(options);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setUpDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withDisplayBelowStatusBar(true)
                .withToolbar(toolbar)
                .build();
        PrimaryDrawerItem homeItem = new PrimaryDrawerItem().withName("Home").withIdentifier(1);
        PrimaryDrawerItem deviceItem = new PrimaryDrawerItem().withName("Thiết bị").withIdentifier(2);
        PrimaryDrawerItem musicItem = new PrimaryDrawerItem().withName("Nhạc").withIdentifier(3);
        PrimaryDrawerItem settingItem = new PrimaryDrawerItem().withName("Cài đặt").withIdentifier(4);
        drawer.addItems(homeItem, deviceItem, musicItem,settingItem);
        drawer.setOnDrawerItemClickListener(this);
        drawer.setSelection(1,true);
    }

    private void setSubscribe() {
        try {
            client.subscribe("ConcirrusLinkIt/Analog",0);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        Fragment fragment = new Fragment();
        switch ((int)drawerItem.getIdentifier()){
            case 1:
                fragment = new HomeFragment();
                break;
            case 2:
                fragment = new DeviceFragment();
                break;
            case 3:
                fragment = new MusicControlFragment();
                break;
        }
        FragmentUtils.replace(getSupportFragmentManager(),R.id.content,fragment);
        drawer.closeDrawer();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.ic_act_voice:
                runVoiceReconition();
                break;
        }
        return true;
    }

    private void runVoiceReconition() {
        if(isConnected()){
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói gì đó để điều khiển thiết bị");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 100);

            startActivityForResult(intent, REQUEST_CODE);
        }
        else{
            Toast.makeText(getApplicationContext(), "Plese Connect to Internet", Toast.LENGTH_LONG).show();
        }
    }

    public  boolean isConnected()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        if (net!=null && net.isAvailable() && net.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean found = false;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> listExtra = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.d("hienlt0610",listExtra.size()+"");
            StringBuilder builder = new StringBuilder();
            List<String> filterActionList = Arrays.asList(filterAction);
            List<String> filterDeviceList = Arrays.asList(filterDevice);
            List<String> filterNumList = Arrays.asList(filterNum);
            String result = "";
            outerloop:
            for(String s : listExtra){
                if(s.toLowerCase().equals("mở nhạc") || s.toLowerCase().equals("bật nhạc")){
                    MQTTUtils.publish(Config.TOPIC_SERVER,"play",client);
                    Toast.makeText(this, "Đã mở phát nhạc", Toast.LENGTH_SHORT).show();
                    found = true;
                    break outerloop;
                }
                else if(s.toLowerCase().equals("tắt nhạc")){
                    Toast.makeText(this, "Đã tắt nhạc", Toast.LENGTH_SHORT).show();
                    MQTTUtils.publish(Config.TOPIC_SERVER,"pause",client);
                    found = true;
                    break outerloop;
                }
                else if(s.toLowerCase().equals("trở về")){
                    MQTTUtils.publish(Config.TOPIC_SERVER,"prev",client);
                    Toast.makeText(this, "Lui về bài hát trước", Toast.LENGTH_SHORT).show();
                    found = true;
                    break outerloop;
                }
                else if(s.toLowerCase().equals("tiến tới")){
                    MQTTUtils.publish(Config.TOPIC_SERVER,"next",client);
                    Toast.makeText(this, "Sang bài hát tiếp theo", Toast.LENGTH_SHORT).show();
                    found = true;
                    break outerloop;
                }
                else if(containsIgnoreCase(s,filterActionList)){
                    if(containsIgnoreCase(s,filterDeviceList)){
                        if(containsIgnoreCase(s,filterNumList)){
                            result = s;
                            found = true;
                            break outerloop;
                        }
                    }
                }
            }
            if(found){
                if(!result.isEmpty()){
                    requestTurn(result);
                }
            }
            else{
                Toast.makeText(this, "Không hiểu lệnh, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestTurn(String result) {
        result = result.toLowerCase().replace("tám","8");
        result = result.toLowerCase().replace("bảy","7");
        result = result.toLowerCase().replace("sáu","6");
        result = result.toLowerCase().replace("xấu","6");
        result = result.toLowerCase().replace("năm","5");
        result = result.toLowerCase().replace("bốn","4");
        result = result.toLowerCase().replace("ba","3");
        result = result.toLowerCase().replace("hai","2");
        result = result.toLowerCase().replace("một","1");
        Toast.makeText(this, result,Toast.LENGTH_SHORT).show();

        Command command = getCommand(result);
        if(command.isDeviceValid()){
            String action = null;
           if(command.getAction().equals("on")){
               action = "Out"+(command.getDevice())+"=1";
           }else{
               action = "Out"+(command.getDevice())+"=0";
           }
            MQTTUtils.publish(Config.TOPIC_SERVER,action,client);
        }
    }

    public boolean containsIgnoreCase(String str, List<String> list){
        for(String i : list){
            if(str.toLowerCase().contains(i.toLowerCase()))
                return true;
        }
        return false;
    }

    private Command getCommand(String command){
        Command cmd = new Command();
        for(String s : filterDevice){
            int i;
            if((i = command.indexOf(s)) != -1){
                String[] split = command.split(s);
                if(containsIgnoreCase(split[0], Arrays.asList(new String[]{"mở","bật","sáng"}))){
                    cmd.setAction("on");
                }else if(containsIgnoreCase(split[0], Arrays.asList(new String[]{"tắt","tối","dừng"}))){
                    cmd.setAction("off");
                }
                if(split.length > 1)
                    cmd.setDevice(strToInt(split[1]));
                cmd.setDeviceName(s);

                break;
            }
        }
        return cmd;
    }

    private int strToInt(String strNum){
        strNum = strNum.trim();
        try{
            int d = Integer.parseInt(strNum);
            return d;
        }catch (NumberFormatException e){

        }
        return -1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(client.isConnected()){
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}

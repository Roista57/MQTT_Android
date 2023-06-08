package com.example.mqttandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Switch;
import android.os.Handler;

import com.example.myapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MainActivity extends AppCompatActivity {

    private MqttAndroidClient mqttAndroidClient;
    private Button buttonStop;
    private Button buttonLow;
    private Button buttonMedium;
    private Button buttonHigh;
    private Switch switchDirectControl;
    private TextView textviewData;
    private TextView textviewTime;
    private TextView connectStatus;
    private TextView textviewTemp;
    private TextView textviewHumi;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonLow = (Button) findViewById(R.id.buttonLow);
        buttonMedium = (Button) findViewById(R.id.buttonMedium);
        buttonHigh = (Button) findViewById(R.id.buttonHigh);

        buttonStop.setEnabled(false); // 기본 상태에는 Stop 버튼은 누를 수 없도록 설정

        textviewData = (TextView) findViewById(R.id.textviewData);
        textviewTime = (TextView) findViewById(R.id.textviewTime);
        connectStatus = (TextView) findViewById(R.id.connectStatus);
        textviewTemp = (TextView) findViewById(R.id.temp);
        textviewHumi = (TextView) findViewById(R.id.humi);

        switchDirectControl = findViewById(R.id.switch_direct_control);

        // 시간 딜레이
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateDateTime();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        mqttAndroidClient = new MqttAndroidClient(this, "tcp://broker.mqtt-dashboard.com:1883", MqttClient.generateClientId());

        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());    //mqtttoken 이라는것을 만들어 connect option을 달아줌
            token.setActionCallback(new IMqttActionListener() {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());    //연결에 성공한경우
                    Log.e("Connect_success", "Success");
                    connectStatus.setText("Connect");
                    connectStatus.setTextColor(Color.GREEN);
                    try {
                        mqttAndroidClient.subscribe("sensors", 0 );   //연결에 성공하면 jmlee 라는 토픽으로 subscribe함
                        mqttAndroidClient.subscribe("response", 0);
                        mqttAndroidClient.publish("request", "1".getBytes(), 0 , false );
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {   //연결에 실패한경우
                    Log.e("connect_fail", "Failure " + exception.toString());
                    connectStatus.setText("Disconnect");
                    connectStatus.setTextColor(Color.RED);
                }
            });
        } catch (
                MqttException e) {
            e.printStackTrace();
        }

        // Switch
        switchDirectControl.setOnCheckedChangeListener((buttonView, isChecked) -> {
            try{
                if (isChecked) {
                    mqttAndroidClient.publish("control", "1".getBytes(), 0 , false );
                }else {
                    mqttAndroidClient.publish("control", "0".getBytes(), 0 , false );
                }
            }catch (Exception e) {e.printStackTrace();}
        });

        // Stop 버튼
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttAndroidClient.publish("control", "2".getBytes(), 0 , false );
                    buttonStop.setEnabled(false);
                    buttonLow.setEnabled(true);
                    buttonMedium.setEnabled(true);
                    buttonHigh.setEnabled(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        // Low 버튼
        buttonLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttAndroidClient.publish("control", "3".getBytes(), 0 , false );
                    buttonStop.setEnabled(true);
                    buttonLow.setEnabled(false);
                    buttonMedium.setEnabled(true);
                    buttonHigh.setEnabled(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        // Medium 버튼
        buttonMedium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttAndroidClient.publish("control", "4".getBytes(), 0 , false );
                    buttonStop.setEnabled(true);
                    buttonLow.setEnabled(true);
                    buttonMedium.setEnabled(false);
                    buttonHigh.setEnabled(true);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });

        // High 버튼
        buttonHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mqttAndroidClient.publish("control", "5".getBytes(), 0 , false );
                    buttonStop.setEnabled(true);
                    buttonLow.setEnabled(true);
                    buttonMedium.setEnabled(true);
                    buttonHigh.setEnabled(false);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });



        // MQTT 클라이언트 콜백 함수 설정
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            // 구독한 토픽의 메시지를 받은 경우
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toast.makeText(MainActivity.this, topic, Toast.LENGTH_SHORT).show();
                // Sensor 토픽인 경우
                if (topic.equals("sensors")){
                    String msg = new String(message.getPayload());
                    Log.e("Sensor Message", msg);
                    String[] msgSplit = msg.split(",");
                    textviewTemp.setText(msgSplit[0]+"°C");
                    textviewHumi.setText(msgSplit[1]+"%");
                }
                // Response 토픽인 경우
                if (topic.equals("response")){
                    String msg = new String(message.getPayload());
                    Log.e("Response Message", msg);
                    String[] msgSplit = msg.split(",");
                    int mobileControl = Integer.parseInt(msgSplit[0]);
                    int control = Integer.parseInt(msgSplit[1]);
                    if(mobileControl == 1) switchDirectControl.setChecked(true);
                    else if(mobileControl == 0) switchDirectControl.setChecked(false);
                    switch(control){
                        case 2:
                            buttonStop.setEnabled(false);
                            buttonLow.setEnabled(true);
                            buttonMedium.setEnabled(true);
                            buttonHigh.setEnabled(true);
                            break;
                        case 3:
                            buttonStop.setEnabled(true);
                            buttonLow.setEnabled(false);
                            buttonMedium.setEnabled(true);
                            buttonHigh.setEnabled(true);
                            break;
                        case 4:
                            buttonStop.setEnabled(true);
                            buttonLow.setEnabled(true);
                            buttonMedium.setEnabled(false);
                            buttonHigh.setEnabled(true);
                            break;
                        case 5:
                            buttonStop.setEnabled(true);
                            buttonLow.setEnabled(true);
                            buttonMedium.setEnabled(true);
                            buttonHigh.setEnabled(false);
                            break;
                    }
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    private void updateDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
        Date now = new Date();

        String date = dateFormat.format(now);
        String time = timeFormat.format(now);

        textviewData.setText(date);
        textviewTime.setText(time);
    }

    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(true);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setWill("aaa", "I am going offline".getBytes(), 1, true);
        return mqttConnectOptions;
    }
}

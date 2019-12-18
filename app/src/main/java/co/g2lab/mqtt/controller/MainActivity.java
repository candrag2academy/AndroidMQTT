package co.g2lab.mqtt.controller;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;

import co.g2lab.mqtt.R;
import co.g2lab.mqtt.adapter.NasabahAdapter;
import co.g2lab.mqtt.helper.MqttHelper;
import co.g2lab.mqtt.model.Nasabah;

public class MainActivity extends AppCompatActivity {

    ListView nasabahListView;
    TextView refreshTextView, topikEditText, messageEditText;
    Button sendButton;
    NasabahAdapter nasabahAdapter;
    private List<Nasabah> listNasabah;
    private MqttHelper mqttHelper;
    String TOPIK_KEY = "XXX123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById();
        onClickGroup();
        initData();

    }
    void findViewById(){
        nasabahListView = (ListView) findViewById(R.id.nasabahListView);
        refreshTextView = (TextView) findViewById(R.id.connectTextView);
        topikEditText = (EditText) findViewById(R.id.topikEditText);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        sendButton = (Button) findViewById(R.id.sendButton);
    }
    void onClickGroup(){
        refreshTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMqtt();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mqttHelper.publishMessage(messageEditText.getText().toString(),topikEditText.getText().toString());
                //sendMqtt(messageEditText.getText().toString());
            }
        });
        nasabahListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Toast.makeText(MainActivity.this, listNasabah.get(position).getNama(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    void initData(){
        mqttHelper = new MqttHelper(getApplicationContext());

        listNasabah = new ArrayList();
        listNasabah.clear();
        nasabahAdapter = new NasabahAdapter(getApplicationContext(), listNasabah);
        nasabahListView.setAdapter(nasabahAdapter);
        nasabahAdapter.notifyDataSetChanged();



    }

    void startMqtt() {
        mqttHelper.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean b, String s) {
                Toast.makeText(MainActivity.this, "Mqtt connected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(MainActivity.this, "Mqtt not connected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                if (topic.equals("g2lab/"+TOPIK_KEY)) {
                    try {
                        JsonParser parser = new JsonParser();
                        JsonElement mJson =  parser.parse(mqttMessage.toString());
                        Gson gson = new Gson();
                        Nasabah nasabah= gson.fromJson(mJson, Nasabah.class);
                        listNasabah.add(nasabah);
                        nasabahAdapter.notifyDataSetChanged();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }


                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}

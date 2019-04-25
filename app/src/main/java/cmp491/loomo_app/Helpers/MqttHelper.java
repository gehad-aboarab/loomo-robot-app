package cmp491.loomo_app.Helpers;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import cmp491.loomo_app.Android.LoomoApplication;

public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;

    private final String serverUri = "tcp://m24.cloudmqtt.com:17852";
    String clientId;
    private final String subscriptionTopic = "server-to-loomo/#";
    private final String username = "gwvgvrbb";
    private final String password = "HoftA-90m-BL";
    private final String TAG = "MqttHelper_Tag";
    private Context context;

    public MqttHelper(Application application){
        clientId = ((LoomoApplication)application).deviceId;
        context = application.getApplicationContext();
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) { mqttAndroidClient.setCallback(callback); }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setKeepAliveInterval(10);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {
            mqttAndroidClient.connect(mqttConnectOptions, context, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                    Log.d(TAG, "onSuccess: connectsuccess");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) { Log.w(TAG, "Failed to connect to: " + serverUri + exception.toString()); }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }
    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w(TAG,"Subscribed!");
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) { Log.w(TAG, "Subscribed fail!"); }
            });
        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }
}

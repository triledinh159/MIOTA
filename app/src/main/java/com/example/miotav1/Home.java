package com.example.miotav1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult;
import com.amplifyframework.auth.cognito.result.GlobalSignOutError;
import com.amplifyframework.auth.cognito.result.HostedUIError;
import com.amplifyframework.auth.cognito.result.RevokeTokenError;
import com.amplifyframework.core.Amplify;




import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;

class MqttMessageReceiver extends AsyncTask<Void, String, Void> {
    private LineChart mChart;
    private Mqtt5BlockingClient mqttClient;
    private TextView tvReceivedMessage;


    MqttMessageReceiver(LineChart mChart, Mqtt5BlockingClient mqttClient, TextView tvReceivedMessage) {
        this.mChart = mChart;
        this.mqttClient = mqttClient;
        this.tvReceivedMessage = tvReceivedMessage;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try (final Mqtt5BlockingClient.Mqtt5Publishes publishes = mqttClient.publishes(MqttGlobalPublishFilter.ALL)) {
            while (true) {
                Optional<Mqtt5Publish> message = publishes.receive(10, TimeUnit.SECONDS);

                if (message.isPresent()) {
                    String receivedMessage = new String(message.get().getPayloadAsBytes(), UTF_8);
                    Log.d("mqtt-tri", "Received message: " + receivedMessage);
                    Gson gson = new Gson();
                    Stat stat = gson.fromJson(receivedMessage, Stat.class);
                    addEntry(receivedMessage);
                    publishProgress(receivedMessage);
                } else {
                    Log.d("mqtt-tri", "No message received within the specified time.");
                }
            }
        } catch (Exception e) {
            Log.e("mqtt-tri", "Error receiving message: " + e.getMessage());
        }
        return null;
    }
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Memory Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColors(ColorTemplate.VORDIPLOM_COLORS[0]);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(10f);
        // To show values of each point
        set.setDrawValues(true);

        return set;
    }

    private void addEntry(String receivedMessage) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(receivedMessage, JsonObject.class);

            if (jsonObject.has("value")) {
                float numericValue = jsonObject.get("value").getAsFloat();
                LineData data = mChart.getData();

                if (data != null) {
                    ILineDataSet set = data.getDataSetByIndex(0);

                    if (set == null) {
                        set = createSet();
                        data.addDataSet(set);
                    }

                    data.addEntry(new Entry(set.getEntryCount(), numericValue), 0);

                    // let the chart know its data has changed
                    data.notifyDataChanged();
                    mChart.notifyDataSetChanged();

                    // limit the number of visible entries
                    mChart.setVisibleXRangeMaximum(15);

                    // move to the latest entry
                    mChart.moveViewToX(data.getEntryCount());
                }
            }
        } catch (JsonSyntaxException e) {
            Log.e("mqtt-tri", "Error parsing JSON: " + e.getMessage());
        }
    }



    @Override
    protected void onProgressUpdate(String... values) {
        // Update UI on the main thread
        tvReceivedMessage.setText(values[0]);
    }
}

public class Home extends AppCompatActivity {
    private LineChart mChart;

    private boolean isChartVisible = true;
    private static final float TOTAL_MEMORY = 16.0f;
    private static final float LIMIT_MAX_MEMORY = 12.0f;
    private String host, username, password, topic;
    private Mqtt5BlockingClient mqttClient;
    private TextView tvReceivedMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mChart = (LineChart) findViewById(R.id.chart);

        setupChart();
        setupAxes();
        setupData();
        setLegend();
        findViewById(R.id.btnToggleChart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleChartVisibility();
            }
        });

        // Set up the Signout button click listener
        findViewById(R.id.btnSignOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut(); // Make sure this method is being called
            }

        });


        tvReceivedMessage = findViewById(R.id.tvReceivedMessage);
//
//        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("triledinh159");
//        options.setPassword("Tri12345".toCharArray());
//
//        String clientId = MqttClient.generateClientId();
//        MqttAndroidClient client =
//                new MqttAndroidClient(this.getApplicationContext(), "tcp://d567f3932ca749f78c9e75dac4e4eab5.s2.eu.hivemq.cloud:8883",
//                        clientId);
//
//        try {
//            IMqttToken token = client.connect();
//            token.setActionCallback(new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    // We are connected
//                    Log.d("mqtt-tri", "onSuccess");
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    // Something went wrong e.g. connection timeout or firewall problems
//                    Log.d("mqtt-tri", "onFailure");
//
//                }
//            });
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
        host = "d567f3932ca749f78c9e75dac4e4eab5.s2.eu.hivemq.cloud";
        username = "trild";
        password = "Tri123456";
        topic = "my/test/topic";

        try{
        // create an MQTT client
        mqttClient = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();


        // connect to HiveMQ Cloud with TLS and username/pw
        mqttClient.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();

        Log.d("mqtt-tri", "Connected successfully");

            try {
                MqttMessageReceiver mqttMessageReceiver = new MqttMessageReceiver(mChart, mqttClient, tvReceivedMessage);
                mqttMessageReceiver.execute();

                // The rest of your existing code...
            } catch (Exception e) {
                Log.e("mqtt-tri", "er=");
            }



            /*       final Mqtt5BlockingClient.Mqtt5Publishes publishes = mqttClient.publishes(MqttGlobalPublishFilter.ALL);*/

        // subscribe to the topic "my/test/topic"
        mqttClient.subscribeWith()
                .topicFilter("my/test/topic")
                .send();


        findViewById(R.id.btnPublish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    publish(); // Call the publish() method
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    } catch (Exception e){
            Log.e("mqtt-tri", "errrrrrrrrr");
        }
    }

    private void toggleChartVisibility() {
        if (isChartVisible) {
            // If chart is currently visible, hide it
            mChart.setVisibility(View.GONE);
        } else {
            // If chart is currently hidden, show it
            mChart.setVisibility(View.VISIBLE);
        }
        isChartVisible = !isChartVisible;
    }

        private void publish() throws InterruptedException {
        final String host = "d567f3932ca749f78c9e75dac4e4eab5.s2.eu.hivemq.cloud";
        final String username = "trild";
        final String password = "Tri123456";

        // create an MQTT client
        final Mqtt5BlockingClient client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();

        // connect to HiveMQ Cloud with TLS and username/pw
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();

        // set a callback that is called when a message is received (using the async API style)
        Log.d("mqtt-tri", "check check check");


        // publish a message to the topic "my/test/topic"
        client.publishWith()
                .topic("my/test/topic")
                .payload(UTF_8.encode("{\"value\": 500}"))
                .send();


    }


    // Method to handle Signout
    private void signOut() {
        Amplify.Auth.signOut(signOutResult -> {
            if (signOutResult instanceof AWSCognitoAuthSignOutResult.CompleteSignOut) {
                // Sign Out completed fully and without errors.
                Log.i("AuthQuickStart", "Signed out successfully");
                //Back To Login screen
                navigateToLogin();
            } else if (signOutResult instanceof AWSCognitoAuthSignOutResult.PartialSignOut) {
                // Sign Out completed with some errors. User is signed out of the device.
                AWSCognitoAuthSignOutResult.PartialSignOut partialSignOutResult =
                        (AWSCognitoAuthSignOutResult.PartialSignOut) signOutResult;

                HostedUIError hostedUIError = partialSignOutResult.getHostedUIError();
                if (hostedUIError != null) {
                    Log.e("AuthQuickStart", "HostedUI Error", hostedUIError.getException());
                    // Optional: Re-launch hostedUIError.getUrl() in a Custom tab to clear Cognito web session.
                }

                GlobalSignOutError globalSignOutError = partialSignOutResult.getGlobalSignOutError();
                if (globalSignOutError != null) {
                    Log.e("AuthQuickStart", "GlobalSignOut Error", globalSignOutError.getException());
                    // Optional: Use escape hatch to retry revocation of globalSignOutError.getAccessToken().
                }

                RevokeTokenError revokeTokenError = partialSignOutResult.getRevokeTokenError();
                if (revokeTokenError != null) {
                    Log.e("AuthQuickStart", "RevokeToken Error", revokeTokenError.getException());
                    // Optional: Use escape hatch to retry revocation of revokeTokenError.getRefreshToken().
                }
            } else if (signOutResult instanceof AWSCognitoAuthSignOutResult.FailedSignOut) {
                AWSCognitoAuthSignOutResult.FailedSignOut failedSignOutResult =
                        (AWSCognitoAuthSignOutResult.FailedSignOut) signOutResult;
                // Sign Out failed with an exception, leaving the user signed in.
                Log.e("AuthQuickStart", "Sign out Failed", failedSignOutResult.getException());
            }
        });
    }
    // Method to navigate to the Login screen
    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the back stack
        startActivity(intent);
        finish(); // Close the current activity (Home)
    }
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAndRestartApp(); // Modified: finish the activity and restart the app
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void finishAndRestartApp() {
        finish(); // Finish the current activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent); // Restart the app by starting MainActivity
        moveTaskToBack(true); // Move the task to the back of the stack
        android.os.Process.killProcess(android.os.Process.myPid()); // Kill the process
        System.exit(1); // Exit the app
    }

    private void setupChart() {
        // disable description text
        mChart.getDescription().setEnabled(false);
        // enable touch gestures
        mChart.setTouchEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);
        // enable scaling
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        // set an alternative background color
        mChart.setBackgroundColor(Color.DKGRAY);
    }

    private void setupAxes() {
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(TOTAL_MEMORY);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Add a limit line
//        LimitLine ll = new LimitLine(LIMIT_MAX_MEMORY, "Upper Limit");
//        ll.setLineWidth(2f);
//        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
//        ll.setTextSize(10f);
//        ll.setTextColor(Color.WHITE);
//        // reset all limit lines to avoid overlapping lines
//        leftAxis.removeAllLimitLines();
//        leftAxis.addLimitLine(ll);
//        // limit lines are drawn behind data (and not on top)
//        leftAxis.setDrawLimitLinesBehindData(true);
    }

    private void setupData() {
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);
    }

    private void setLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.CIRCLE);
        l.setTextColor(Color.WHITE);
    }
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect the MQTT client when the activity is destroyed
        if (mqttClient != null && mqttClient.getState().isConnected()) {
            mqttClient.disconnect();
        }
    }
}
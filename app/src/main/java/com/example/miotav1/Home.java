package com.example.miotav1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult;
import com.amplifyframework.auth.cognito.result.GlobalSignOutError;
import com.amplifyframework.auth.cognito.result.HostedUIError;
import com.amplifyframework.auth.cognito.result.RevokeTokenError;
import com.amplifyframework.core.Amplify;


import org.apache.commons.io.FileUtils;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
//import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.material.navigation.NavigationView;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

class MqttMessageReceiver extends AsyncTask<Void, String, Void> {

    private Mqtt5BlockingClient mqttClient;
    private TextView tvReceivedMessage;

    MqttMessageReceiver(Mqtt5BlockingClient mqttClient, TextView tvReceivedMessage) {
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

    @Override
    protected void onProgressUpdate(String... values) {
        // Update UI on the main thread
        tvReceivedMessage.setText(values[0]);
    }
}

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int LAYOUT_HOME = 0;
    private String host, username, password, topic;
    private Mqtt5BlockingClient mqttClient;
    private TextView tvReceivedMessage;

    private int mCurrentLayout = LAYOUT_HOME;

    private DrawerLayout mDrawerLayout;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        FrameLayout frameLayout = findViewById(R.id.content_frame);
        frameLayout.removeAllViews();  // Xóa tất cả các views hiện tại trong FrameLayout
        View inflatedView = getLayoutInflater().inflate(R.layout.activity_home_screen, frameLayout, true);


        tvReceivedMessage = findViewById(R.id.tvReceivedMessage);

        try{
            getInfo();
        } catch (Exception e){
            Log.e("mqtt-tri", "errrrrrrrrr");
        }
    }

    private void getInfo() throws InterruptedException {
        try {

            File exampleFile = new File(getApplicationContext().getFilesDir(), "user.json");
            Amplify.Storage.downloadFile(
                    "user.json",
                    exampleFile,
                    result -> {
                        Log.i("MyAmplifyApp", "Successfully downloaded: " + result.getFile().getName());
                        try {
                            String jsonString = FileUtils.readFileToString(result.getFile(), StandardCharsets.UTF_8);
                            processConfig(jsonString);
                            Log.d("mqtt-tri", "read" + jsonString);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }


                    },
                    error -> Log.e("MyAmplifyApp",  "Download Failure", error)
            );
        } catch(Exception error) {
            Log.e("MyAmplifyApp", "Download Failure", error);
        }
    }
    private void processConfig(String jsonString) {
        try {
            JSONObject configObject = new JSONObject(jsonString);
            JSONArray devicesArray = configObject.getJSONArray("devices");
            List<String> topics = new ArrayList<>();

            for (int i = 0; i < devicesArray.length(); i++) {
                JSONObject deviceObject = devicesArray.getJSONObject(i);
                String topic = deviceObject.getString("topic");
                topics.add(topic);
                Log.d("mqtt-tri", topic);
            }

            subscribeToTopics(topics);

        } catch (JSONException e) {
            Log.e("mqtt-tri", "Error parsing JSON: " + e.getMessage());
        }
    }
    private void subscribeToTopics(List<String> topics) {
        host = "d567f3932ca749f78c9e75dac4e4eab5.s2.eu.hivemq.cloud";
        username = "trild";
        password = "Tri123456";
        try {
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

            // Subscribe to all topics
            for (String topic : topics) {
                mqttClient.subscribeWith()
                        .topicFilter(topic)
                        .send();
                Log.d("mqtt-tri", "Subscribed to topic: " + topic);
            }

            // Receive messages for all subscribed topics
            try {
                // Create an instance of MqttMessageReceiver and execute it
                MqttMessageReceiver mqttMessageReceiver = new MqttMessageReceiver(mqttClient, tvReceivedMessage);
                mqttMessageReceiver.execute();

                // The rest of your existing code...
            } catch (Exception e) {
                Log.e("mqtt-tri", "er=");
            }

        } catch (Exception e) {
            Log.e("mqtt-tri", "Error connecting to MQTT broker: " + e.getMessage());
        }
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
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect the MQTT client when the activity is destroyed
        if (mqttClient != null && mqttClient.getState().isConnected()) {
            mqttClient.disconnect();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
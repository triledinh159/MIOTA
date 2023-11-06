package com.example.miotav1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.auth.AuthUser;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.AWSDataStorePlugin;
import com.amplifyframework.datastore.generated.model.AmplifyModelProvider;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;
        try {
            Amplify.Auth.getCurrentUser(
                    result -> Log.i("AuthQuickstart", "Current user details are:" + result.toString()),
                    error -> Log.e("AuthQuickstart", "getCurrentUser failed with an exception: " + error)
                    );
        } catch (Exception error) {
            intent = new Intent(getApplicationContext(), Login.class);
        }
        //intent = new Intent(getApplicationContext(), Home.class);
        intent = new Intent(getApplicationContext(), Login.class);
        //start
        startActivity(intent);
        finish();
    }
}
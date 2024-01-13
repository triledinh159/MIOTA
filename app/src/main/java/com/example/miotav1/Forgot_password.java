package com.example.miotav1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.ForgotPasswordResult;
import com.amplifyframework.core.Amplify;

public class Forgot_password extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    private EditText usernameEditText;
    private EditText confirmationCodeEditText;
    private EditText newPasswordEditText;

    private Button sendCode;

    private Button changePassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        // Initialize UI components
        usernameEditText = findViewById(R.id.usernameEditText);
        confirmationCodeEditText = findViewById(R.id.confirmationCodeEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        sendCode = findViewById(R.id.sendCodeButton);
        changePassword = findViewById(R.id.changePasswordButton);


    }

    public void onSendCodeClick(View view) {
        String username = usernameEditText.getText().toString();
        Amplify.Auth.resetPassword(
                username,
                result -> Log.i("AuthQuickstart", result.toString()),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }


    public void onChangePasswordClick(View view) {
        String username = usernameEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmationCode = confirmationCodeEditText.getText().toString().trim();

        Amplify.Auth.confirmResetPassword(
                username,
                newPassword,
                confirmationCode,
                () -> Log.i("AuthQuickstart", "New password confirmed"),
                error -> Log.e("AuthQuickstart", error.toString())
        );
    }

    private void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}


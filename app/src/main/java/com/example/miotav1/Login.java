package com.example.miotav1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.result.AuthSignInResult;
import com.amplifyframework.core.Amplify;

public class Login extends AppCompatActivity {
 private Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView forgot_password = findViewById(R.id.forgot_password);
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(Login.this, "Forgot Password", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Login.this, Forgot_password.class);
                // Pass data if needed
                // intent.putExtra("device_id", clickedDevice.getID());
                startActivity(intent);
            }
        });
    }

    public void onPressLogin(View view) {
        EditText txtEmail = findViewById(R.id.txtEmail);
        EditText txtPassword = findViewById(R.id.txtPassword);

        showProgressDialog();

        Amplify.Auth.signIn(
                txtEmail.getText().toString(),
                txtPassword.getText().toString(),
                this::onLoginSucces,
                this::onLoginError
        );
    }
    private ProgressDialog progressDialog;

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false); // Ngăn chặn việc đóng progress bar khi chạm vào màn hình
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void onLoginError(AuthException e) {
        this.runOnUiThread(() ->{
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            dismissProgressDialog();
        });
    }

    private void onLoginSucces(AuthSignInResult authSignInResult) {
        Intent intent = new Intent(Login.this, Home.class);
        startActivity(intent);
    }

    public void onJoinPressed(View view) {
        Intent intent = new Intent(this, Join.class);
        startActivity(intent);
    }
}
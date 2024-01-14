package com.example.miotav1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.auth.AuthException;
import com.amplifyframework.auth.result.AuthSignInResult;
import com.amplifyframework.auth.result.AuthSignUpResult;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.DataStoreException;
import com.amplifyframework.datastore.DataStoreItemChange;
import com.amplifyframework.datastore.generated.model.User;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EmailConfirm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_confirm);
    }

    public void onConfirmButtonPressed(View view) {
        EditText txtConfirmationCode = findViewById(R.id.txtConfirmationCode);

        Amplify.Auth.confirmSignUp(
                getEmail(),
                txtConfirmationCode.getText().toString(),
                this::onSuccess,
                this::onError
        );
    }

    private void onError(AuthException e) {
        runOnUiThread(() -> Toast
                .makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                .show());
    }

    private void onSuccess(AuthSignUpResult authSignUpResult) {

        // Define the local file path for the JSON file
        // Define the S3 key for the object file
        String localName = getEmail() + ".json";
        String s3Key ="tempUser.json";
        getInfo(s3Key, localName);
        // Upload the JSON template to S3
        //uploadFileToS3(localName);


    }


    private void reLogin() {
        String username = getEmail();
        String password = getPassword();

        Amplify.Auth.signIn(
                username,
                password,
                this::onLoginSuccess,
                this::onError
        );
    }

    private void onLoginSuccess(AuthSignInResult authSignInResult) {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
        finish();
    }

    private <T extends Model> void onSavedSuccess(DataStoreItemChange<T> tDataStoreItemChange) {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void onError(DataStoreException e) {
        runOnUiThread(() -> Toast
                .makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                .show());
    }

    private String getName() {
        return getIntent().getStringExtra("name");
    }

    private String getPassword() {
        return getIntent().getStringExtra("password");
    }

    private String getEmail() {
        return getIntent().getStringExtra("email");
    }
    private void uploadFileToS3(String localName) {
        File exampleFile = new File(getApplicationContext().getFilesDir(), localName);
        Log.d("mqtt-triJsonFileName", "JsonFileName: " + localName);
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
//            writer.append("Example file contents");
//            writer.close();
//        } catch (Exception exception) {
//            Log.e("mqtt-triFirst", "Upload failed", exception);
//        }

        Amplify.Storage.uploadFile(
                localName,
                exampleFile,
                result -> Log.i("mqtt-triFirst", "Successfully uploaded: " + result.getKey()),
                storageFailure -> Log.e("mqtt-triFirst", "Upload failed", storageFailure)
        );
    }

    private void getInfo(String s3Key, String localName) {
        new DownloadFileAsyncTask(s3Key, localName, new AsyncTaskCallback() {
            @Override
            public void onTaskComplete() {
                // The AsyncTask is complete, now upload to S3
                uploadFileToS3(localName);
            }
        }).execute();
    }

    private interface AsyncTaskCallback {
        void onTaskComplete();
    }

    private class DownloadFileAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String s3Key;
        private final String localName;
        private final AsyncTaskCallback callback;

        public DownloadFileAsyncTask(String s3Key, String localName, AsyncTaskCallback callback) {
            this.s3Key = s3Key;
            this.localName = localName;
            this.callback = callback;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d("mqtt-triJsonFileName", "JsonFileName: " + s3Key);
                File exampleFile = new File(getApplicationContext().getFilesDir(), localName);
                Amplify.Storage.downloadFile(
                        s3Key,
                        new File(getApplicationContext().getFilesDir() + "/" + localName),
                        result -> {
                            Log.i("mqtt-triFirst", "First Successfully downloaded: " + result.getFile().getName());
                            try {
                                String jsonString = FileUtils.readFileToString(result.getFile(), StandardCharsets.UTF_8);
                                Log.d("mqtt-triFirst", "read" + jsonString);

                                // Introduce a delay of 1 second before uploading to S3
                                new Handler().postDelayed(() -> {
                                    uploadFileToS3(localName);

                                    // Introduce another delay of 1 second before reLogin
                                    new Handler().postDelayed(() -> reLogin(), 1000);
                                }, 1000);

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        },
                        error -> Log.e("mqtt-triFirst", "First Download Failure", error)
                );
            } catch (Exception error) {
                Log.e("mqtt-triFirst", "First Download Failure", error);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Notify the callback that the AsyncTask is complete
            callback.onTaskComplete();
        }
    }
}

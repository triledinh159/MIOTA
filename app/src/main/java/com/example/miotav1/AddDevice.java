package com.example.miotav1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AddDevice extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        final CheckBox getTypedata = findViewById(R.id.data);
        final CheckBox getTypecontrol = findViewById(R.id.control);
        final EditText getDevice= findViewById(R.id.getDevice);
        final EditText getTopic = findViewById(R.id.getTopic);
        final EditText getName = findViewById(R.id.getName);
        Button btnConfirm = findViewById(R.id.btnconfirm);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int typeDevice = 0;
                if (getTypecontrol.isChecked()) {typeDevice = 1;}
                if(getTypedata.isChecked()) {typeDevice = 0;}
                String device = getDevice.getText().toString();
                String topic = getTopic.getText().toString();
                String name = getName.getText().toString();
                Device newDevice = new Device(String.valueOf(typeDevice),device, topic, name);
                updateJsonFile(newDevice);
                //Trả về newDevice thông qua Intent
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_Device", newDevice);
                setResult(RESULT_OK, resultIntent);

                //Kết thúc hoạt động
                finish();
            }
        });
    }
    private void updateJsonFile(Device newDevice) {
        // Read existing JSON content from the file
        File file = new File(getFilesDir(), "user.json");
        StringBuilder jsonContent = new StringBuilder();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append('\n');
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Parse existing JSON content
        try {
            JSONObject jsonData = new JSONObject(jsonContent.toString());

            // Update devices array
            JSONArray devicesArray = jsonData.getJSONArray("devices");
            JSONObject newDeviceJson = new JSONObject();
            newDeviceJson.put("type", String.valueOf(newDevice.TypeDevice()));
            newDeviceJson.put("device", newDevice.getDevice());
            newDeviceJson.put("topic", newDevice.getTopic());
            newDeviceJson.put("name", newDevice.getName());
            devicesArray.put(newDeviceJson);

            // Write the updated JSON content back to the file
            FileWriter writer = new FileWriter(file);
            writer.write(jsonData.toString());
            writer.close();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
}

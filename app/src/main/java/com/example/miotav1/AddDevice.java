package com.example.miotav1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

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
                    Device newDevice = new Device(typeDevice,device, topic, name);
                //Trả về newDevice thông qua Intent
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_Device", newDevice);
                setResult(RESULT_OK, resultIntent);

                //Kết thúc hoạt động
                finish();
            }
        });
    }
}

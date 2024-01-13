package com.example.miotav1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.core.Amplify;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {
    private Context context;
    private List<Device> mListDevice;

    private int tempPosition;

    private static final int REQUEST_CODE_DETAIL = 1;
    public DeviceAdapter(Context context, List<Device> mListDevice) {
        this.context = context;
        this.mListDevice = mListDevice;
    }



    public List<Device> getmListDevice() {
        return mListDevice;
    }

    public void setmListDevice(List<Device> mListDevice) {
        this.mListDevice = mListDevice;
    }

    class DeviceHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDevice, tvDevice_statistic, tvDeviceStatistic;

        ImageView Imgbx, Imgconnect;
        Switch swControl;

        Button btnDelete;
        LinearLayout llparent;


        public DeviceHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.nameAtHome);
            tvDevice = itemView.findViewById(R.id.nameDevice);
            Imgbx = itemView.findViewById(R.id.img_box);
            Imgconnect = itemView.findViewById(R.id.Img_connect);
            tvDevice_statistic = itemView.findViewById(R.id.device_static);
            swControl = itemView.findViewById(R.id.sw_control);
            llparent = itemView.findViewById(R.id.item_device);
            btnDelete = itemView.findViewById(R.id.button);
            tvDeviceStatistic = itemView.findViewById(R.id.device_static);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the position of the clicked item
                    int position = getAdapterPosition();
                    // Check if the position is valid
                    if (position != RecyclerView.NO_POSITION) {
                        // Remove the item from the list
                        mListDevice.remove(position);
                        // Notify the adapter that the item is removed
                        notifyItemRemoved(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public DeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view, parent, false);
        DeviceHolder holder = new DeviceHolder(view);

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Device removedDevice = mListDevice.remove(position);
                    notifyItemRemoved(position);
                    updateJsonFileAfterDelete(removedDevice);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Device clickedDevice = mListDevice.get(position);
                    Intent intent = new Intent(context, Detail_device_click_on.class);
                    // Pass data if needed
                    // intent.putExtra("device_id", clickedDevice.getID());
                    intent.putExtra("name", clickedDevice.getName());
                    intent.putExtra("device", clickedDevice.getDevice());
                    intent.putExtra("topic", clickedDevice.getTopic());
                    ((Activity) context).startActivityForResult(intent, REQUEST_CODE_DETAIL);            }
            }
        });

        return holder;
    }
    @Override
    public void onBindViewHolder(@NonNull DeviceHolder holder, int position) {
        Device device = mListDevice.get(position);

        // Make sure to check for null before accessing properties
        if (device != null) {
            holder.tvName.setText(TextUtils.isEmpty(device.getName()) ? "" : device.getName());
            holder.tvDevice.setText(TextUtils.isEmpty(device.getDevice()) ? "" : device.getDevice());

            // Check data or control device data -> 1, control -> 0
            holder.tvDevice_statistic.setVisibility(device.TypeDevice() == 1? View.GONE : View.VISIBLE);
            holder.swControl.setVisibility(device.TypeDevice() == 1 ? View.VISIBLE : View.GONE);
            holder.tvDeviceStatistic.setText(device.getStatisticValue());
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to start Detail_device_click_on activity
                Intent intent = new Intent(context, Detail_device_click_on.class);

                // Pass data to the intent
                intent.putExtra("name", device.getName());
                intent.putExtra("device", device.getDevice());
                intent.putExtra("topic", device.getTopic());
                intent.putExtra("statistic", device.getStatistic());
                // Start the activity
                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_DETAIL);            }
        });
        holder.btnDelete.setTag(position);
    }

    private void updateJsonFileAfterDelete(Device removedDevice) {
        File file = new File(context.getFilesDir(), "user.json");

        // Read existing JSON content from the file
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

            // Update devices array by removing the deleted device
            JSONArray devicesArray = jsonData.getJSONArray("devices");
            for (int i = 0; i < devicesArray.length(); i++) {
                JSONObject deviceJson = devicesArray.getJSONObject(i);
                if (deviceJson.optString("device").equals(removedDevice.getDevice())) {
                    devicesArray.remove(i);
                    break;  // Exit the loop once the device is found and removed
                }
            }

            // Write the updated JSON content back to the file
            FileWriter writer = new FileWriter(file);
            writer.write(jsonData.toString());
            writer.close();
            uploadFileToS3();

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }
    private void uploadFileToS3() {
        File file = new File(context.getFilesDir(), "user.json");

        Amplify.Storage.uploadFile(
                "user.json",
                file,
                result -> Log.i("MyAmplifyApp", "Successfully uploaded: " + result.getKey()),
                error -> Log.e("MyAmplifyApp", "Upload failed", error)
        );
    }

    @Override
    public int getItemCount() {
        return mListDevice.size();
    }


    // Add a method to get the DeviceHolder at a given position

}


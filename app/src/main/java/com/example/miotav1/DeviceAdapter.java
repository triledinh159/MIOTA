package com.example.miotav1;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceHolder> {
    private Context context;
    private List<Device> mListDevice;

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
        TextView tvName, tvDevice, tvDevice_statistic;
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
                    mListDevice.remove(position);
                    notifyItemRemoved(position);
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
                    context.startActivity(intent);
                }
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
        }
        holder.btnDelete.setTag(position);
    }

    @Override
    public int getItemCount() {
        return mListDevice.size();
    }
}


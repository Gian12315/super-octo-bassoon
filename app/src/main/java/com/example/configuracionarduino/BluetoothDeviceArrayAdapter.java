package com.example.configuracionarduino;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {
    private final List<BluetoothDevice> devices;

    public BluetoothDeviceArrayAdapter(Context context, List<BluetoothDevice> devices) {
        super(context, R.layout.bluetooth_device_item, devices);
        this.devices = devices;
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View item = inflater.inflate(R.layout.bluetooth_device_item, null);

        TextView deviceName = item.findViewById(R.id.tvDeviceName);
        TextView deviceAddress = item.findViewById(R.id.tvDeviceAddress);

        BluetoothDevice device = devices.get(position);
        deviceName.setText(device.getName());
        deviceAddress.setText(device.getAddress());

        return item;
    }
}

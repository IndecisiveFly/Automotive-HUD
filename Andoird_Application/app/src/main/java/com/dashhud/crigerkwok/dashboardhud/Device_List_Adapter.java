package com.dashhud.crigerkwok.dashboardhud;

/**
 * Created by Dustin on 2/1/2018.
 */

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class Device_List_Adapter extends ArrayAdapter<BluetoothDevice>{
    private LayoutInflater li;
    private ArrayList<BluetoothDevice> devices_array;
    private int view_resource_id;

    public Device_List_Adapter(Context context, int resource_id, ArrayList<BluetoothDevice> devices)
    {
        super(context, resource_id, devices);
        this.devices_array = devices;
        li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view_resource_id = resource_id;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = li.inflate(view_resource_id, null);

        BluetoothDevice device = devices_array.get(position);

        if(device != null)
        {
            TextView device_name = convertView.findViewById(R.id.device_name);
            TextView device_address = convertView.findViewById(R.id.device_address);

            if(device_name != null)
            {
                device_name.setText(device.getName());
            }
            if(device_address != null)
            {
                device_address.setText(device.getAddress());
            }
        }
        return convertView;
    }


}

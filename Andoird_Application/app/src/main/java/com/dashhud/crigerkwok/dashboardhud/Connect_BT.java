package com.dashhud.crigerkwok.dashboardhud;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class Connect_BT extends AppCompatActivity {

    private static final String TAG = "Connect_BT";

    BluetoothAdapter BT_adapter;

    Button BT_toggle;
    Button BT_discoverable;
    Button BT_connect;

    TextView BT_status;

    public ArrayList<BluetoothDevice> BT_devices = new ArrayList<>();
    public Device_List_Adapter device_list_adapter;
    ListView new_devices_list;


    String bluetooth_on = "Bluetooth is ON";
    String bluetooth_off = "Bluetooth is OFF";

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect__bt);

        BT_toggle = findViewById(R.id.bluetooth_toggle_btn);
        BT_discoverable = findViewById(R.id.discoverable_btn);
        BT_connect = findViewById(R.id.connect_btn);

        BT_status = findViewById(R.id.bluetooth_status);

        BT_adapter = BluetoothAdapter.getDefaultAdapter();

        new_devices_list = findViewById(R.id.devices_list_view);

        pref = getSharedPreferences("status", Context.MODE_PRIVATE);

        check_bt();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(broadcast_receiver1);
        unregisterReceiver(broadcast_receiver2);
        unregisterReceiver(broadcast_receiver3);
        BT_adapter.cancelDiscovery();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        check_bt();
    }

    //onClick for Bluetooth ON/OFF button, changes Bluetooth state and sets appropriate views/strings
    public void bluetooth_on_off(View v)
    {
        if(BT_adapter == null)
        {
            Log.d(TAG, "Does not have BT capabilities");
        }
        if(!BT_adapter.isEnabled())
        {
            BT_adapter.enable();

            IntentFilter BTif = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcast_receiver1, BTif);

            BT_status.setText(bluetooth_on);

            BT_connect.setVisibility(View.VISIBLE);
        }
        if(BT_adapter.isEnabled())
        {
            BT_adapter.disable();

            IntentFilter BTif = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcast_receiver1, BTif);

            BT_status.setText(bluetooth_off);

            BT_connect.setVisibility(View.GONE);
        }
    }

    //initial onCreate check of Bluetooth status, sets appropriate views/strings.
    public void check_bt()
    {
        if(BT_adapter == null)
        {
            Log.d(TAG, "Does not have BT capabilities");
        }
        if(!BT_adapter.isEnabled())
        {
            BT_status.setText(bluetooth_off);
            BT_connect.setVisibility(View.GONE);
        }
        if(BT_adapter.isEnabled())
        {
            BT_status.setText(bluetooth_on);
            BT_connect.setVisibility(View.VISIBLE);
        }
    }

    //onClick for enabling discoverable mode for 5 minutes
    public void bluetooth_discoverable(View v)
    {
        Log.d(TAG, "bluetooth_discoverable: Making device discoverable for 300 seconds");

        Intent discoverable_i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverable_i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverable_i);

        IntentFilter intentfilter = new IntentFilter(BT_adapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(broadcast_receiver2, intentfilter);
    }

    //onClick for showing a list of devices
    public void find_devices(View v)
    {
        Log.d(TAG, "find_devices: Looking for unpaired devices.");
        
        if(BT_adapter.isDiscovering())
        {
            BT_adapter.cancelDiscovery();
            Log.d(TAG, "find_devices: Canceling discovery.");

            //check bluetooth permissions in manifest
            checkBTpermissions();

            BT_adapter.startDiscovery();
            IntentFilter discover_i = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcast_receiver3, discover_i);
        }
        if(!BT_adapter.isDiscovering())
        {
            //check bluetooth permissions in manifest
            checkBTpermissions();

            BT_adapter.startDiscovery();
            IntentFilter discover_i = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(broadcast_receiver3, discover_i);
        }
    }

    //receiver for Bluetooth ON/OFF
    private final BroadcastReceiver broadcast_receiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BT_adapter.ACTION_STATE_CHANGED)) {
                // Discovery has found a device. Get the BluetoothDevice
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BT_adapter.ERROR);

                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "broadcast_receiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "broadcast_receiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "broadcast_receiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    //receiver for Discoverable
    private final BroadcastReceiver broadcast_receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        {
            final String action = intent.getAction();

            if (action.equals(BT_adapter.ACTION_SCAN_MODE_CHANGED))
            {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "broadcast_receiver2: Discoverability Enabled.");
                        break;
                    //Device is not in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "broadcast_receiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "broadcast_receiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "broadcast_receiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "broadcast_receiver2: Connected.");
                        break;
                }
            }
        };
        }
    };

    //receiver for Discover devices
    private BroadcastReceiver broadcast_receiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BT_devices.add(device);

                Log.d(TAG, "onReceive: " + device.getName() + " " + device.getAddress());

                device_list_adapter = new Device_List_Adapter(context, R.layout.activity_device__list__adapter, BT_devices);
                //new_devices_list.setAdapter(device_list_adapter);
            }
        }
    };

    //Required for API 23+ to check bluetooth permissions
    private void checkBTpermissions()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COURSE_LOCATION");
            if(permissionCheck != 0)
            {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
            else
            {
                Log.d(TAG, "checkBTpermissions: No need to check permissions. SDK version < LOLLIPOP.");
            }
        }
    }
}

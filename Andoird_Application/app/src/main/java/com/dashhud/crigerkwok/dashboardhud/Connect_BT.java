package com.dashhud.crigerkwok.dashboardhud;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Connect_BT extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "Connect_BT";

    //Controller controller;

    BluetoothAdapter BT_adapter;

    Button BT_toggle;
    Button BT_discoverable;
    Button BT_reconnect;
    Button BT_scan;

    public ArrayList<BluetoothDevice> BT_devices_list;
    public Device_List_Adapter device_list_adapter;
    ListView new_devices_list;

    String bluetooth_on = "Turn Bluetooth OFF";
    String bluetooth_off = "Turn Bluetooth ON";

    String toastText = "";
    Boolean device_found = false;
    Button connect_service;
    EditText send_et;
    Button send_btn;
    private static final UUID app_uuid = UUID.fromString("00101101-0000-1000-8000-A0803F9B34FB");
    BluetoothDevice BT_device;

    BT_Connection bt_connection;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect__bt);

        BT_toggle = findViewById(R.id.bluetooth_toggle_btn);
        BT_discoverable = findViewById(R.id.discoverable_btn);
        BT_reconnect = findViewById(R.id.reconnect_btn);
        BT_scan = findViewById(R.id.scan_btn);
        connect_service = findViewById(R.id.establish_connection_btn);
        send_et = findViewById(R.id.send_et);
        send_btn = findViewById(R.id.send_btn);

        BT_adapter = BluetoothAdapter.getDefaultAdapter();

        new_devices_list = findViewById(R.id.devices_list_view);

        //Broadcasts when bond state changes (pairing)
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(broadcast_receiver, filter);

        new_devices_list.setOnItemClickListener(Connect_BT.this);

        pref = getSharedPreferences("status", Context.MODE_PRIVATE);

        check_bt();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(broadcast_receiver);
        BT_adapter.cancelDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        check_bt();
    }

    //onClick for Bluetooth ON/OFF button, changes Bluetooth state and sets appropriate views/strings
    public void bluetooth_on_off(View v) {
        if (BT_adapter == null) {
            Log.d(TAG, "Does not have BT capabilities");
        }
        if (!BT_adapter.isEnabled()) {
            BT_adapter.enable();

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcast_receiver, filter);
        }
        if (BT_adapter.isEnabled()) {
            BT_adapter.disable();

            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(broadcast_receiver, filter);
        }

        SystemClock.sleep(1000);
        check_bt();
    }

    //initial onCreate check of Bluetooth status, sets appropriate views/strings.
    public void check_bt() {
        if (BT_adapter == null) {
            Log.d(TAG, "Does not have BT capabilities");
        }
        if (!BT_adapter.isEnabled()) {
            BT_toggle.setText(bluetooth_off);
            BT_discoverable.setEnabled(false);
            BT_reconnect.setEnabled(false);
            BT_scan.setEnabled(false);
        }
        if (BT_adapter.isEnabled()) {
            BT_toggle.setText(bluetooth_on);
            BT_discoverable.setEnabled(true);
            BT_scan.setEnabled(true);

            check_connect();
        }
    }

    //checks if there is a last known device, to set up buttons accordingly
    public void check_connect() {
        String last_used_device = pref.getString("last_BT_device_name", null);

        if (last_used_device != null) {
            BT_reconnect.setEnabled(true);
        } else {
            BT_reconnect.setEnabled(false);
        }
    }

    //onClick for enabling discoverable mode for 30 seconds
    public void bluetooth_discoverable(View v) {
        Log.d(TAG, "bluetooth_discoverable: Making device discoverable for 30 seconds");

        Intent discoverable_i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverable_i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
        startActivity(discoverable_i);
    }

    //onClick for attempting a reconnect to last paired device
    public void attempt_reconnect(View v)
    {
        String last_used_device = pref.getString("last_BT_device_name", null);

        BT_devices_list = new ArrayList<>();

        if(last_used_device != null)
        {
            toastText = "Checking for known paired device: " + last_used_device;
            Toast.makeText(Connect_BT.this, toastText, Toast.LENGTH_SHORT).show();
            Set<BluetoothDevice> paired_devices = BT_adapter.getBondedDevices();
            for(BluetoothDevice paired_device : paired_devices)
            {
                if(paired_device.getName().equals(last_used_device))
                {
                    toastText = "Found device: " + paired_device.getName() + " @ " + paired_device.getAddress();
                    Toast.makeText(Connect_BT.this, toastText, Toast.LENGTH_SHORT).show();
                    BT_device = paired_device;

                    device_found = true;

                    //bt_connection = new BT_Connection(Connect_BT.this);
                    //connect_service(BT_device, app_uuid);

                    Intent a = new Intent(this, Control_and_location.class);
                    startActivity(a);
                    break;
                }
                else
                {
                    device_found = false;
                }
            }
            if(!device_found)
            {
                toastText = "Previous device not found";
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
                BT_reconnect.setEnabled(false);
            }
        }
    }

    //onClick for showing a list of devices
    public void find_devices(View v)
    {
        //String last_used_device = pref.getString("last_BT_device_address", null);

        BT_devices_list = new ArrayList<>();

        Log.d(TAG, "find_devices: Looking for unpaired devices.");
        if (BT_adapter.isDiscovering()) {
            BT_adapter.cancelDiscovery();
            Log.d(TAG, "find_devices: Canceling discovery.");

            //check bluetooth permissions in manifest
            checkBTpermissions();

            BT_adapter.startDiscovery();
        }
        if (!BT_adapter.isDiscovering()) {
            //check bluetooth permissions in manifest
            checkBTpermissions();

            BT_adapter.startDiscovery();
        }
    }

    //What happens when a discovered device is clicked on
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        //first cancel discovery because its very memory intensive
        BT_adapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You clicked a device.");

        String device_name = BT_devices_list.get(i).getName();
        String device_addr = BT_devices_list.get(i).getAddress();

        Log.d(TAG, "onItemClick: device_name = " + device_name);
        Log.d(TAG, "onItemClick: device_addr = " + device_addr);

        Set<BluetoothDevice> paired_devices = BT_adapter.getBondedDevices();
        if (paired_devices.size() > 0)
        {
            for(BluetoothDevice device : paired_devices)
            {
                if(device_name.equals(device.getName()))
                {
                    Intent a = new Intent(this, Control_and_location.class);
                    startActivity(a);
                    break;
                }
            }
            //String text = "No connection found";
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }

        //create the bond
        Log.d(TAG, "Trying to pair with " + device_name);
        BT_devices_list.get(i).createBond();

        BT_device = BT_devices_list.get(i);
        //create a new connection to clicked device, starts with accept thread (blocking call)
        bt_connection = new BT_Connection(Connect_BT.this);

        //connect_service(BT_device, app_uuid);
        //connect gets called once connection is accepted by clicked device
        //bt_connection.startClient(BT_device, app_uuid);
        //controller.transfer_connection(bt_connection);
        //Intent a = new Intent(this, Controller.class);
        //startActivity(a);
    }

    BroadcastReceiver broadcast_receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            Log.d(TAG, "action = " + action);

            //Bluetooth On/Off
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                // Discovery has found a device. Get the BluetoothDevice
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "Bluetooth: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "Bluetooth: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "Bluetooth: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "Bluetooth: STATE TURNING ON");
                        break;
                }
            }

            //Discoverability
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    //Device is in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "Discoverability Enabled.");
                        break;
                    //Device is not in Discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "Connected.");
                        break;
                }
            }

            //Scan for devices
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName() != null)
                {
                    BT_devices_list.add(device);

                    Log.d(TAG, "Found: " + device.getName() + " at " + device.getAddress());

                    device_list_adapter = new Device_List_Adapter(context, R.layout.activity_device__list__adapter, BT_devices_list);
                    new_devices_list.setAdapter(device_list_adapter);

                    toastText = "Discovered: " + device.getName();
                    Toast.makeText(Connect_BT.this, toastText, Toast.LENGTH_SHORT).show();
                }
            }

            //Pairing with devices
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //case 1: bonded already
                if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    Log.d(TAG, "Pairing: BOND_BONDED");
                    BT_device = device;
                    bt_connection.startClient(BT_device, app_uuid);

                    Intent a = new Intent(Connect_BT.this, Control_and_location.class);
                    startActivity(a);
                }
                //case 2: creating a bond
                if(device.getBondState() == BluetoothDevice.BOND_BONDING)
                {
                    Log.d(TAG, "Pairing: BOND_BONDING");
                }
                //case 3: breaking a bond
                if(device.getBondState() == BluetoothDevice.BOND_NONE)
                {
                    Log.d(TAG, "Pairing: BOND_NONE");
                }

                SharedPreferences.Editor editor = pref.edit();
                String name = device.getName();
                editor.putString("last_BT_device_name", name);
                editor.apply();
            }

            //Supposed to know if Bluetooth connection is made, doesn't do it
            if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED))
            {
                Toast.makeText(Connect_BT.this, "Successfully connected to device", Toast.LENGTH_SHORT).show();
                //move_activity();
            }
        }
    };

    //Required for API 23+ to check bluetooth permissions
    private void checkBTpermissions()
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

    //Manual attempt at starting connection service
    public void connect_service(BluetoothDevice device, UUID uuid)
    {
        Log.d(TAG, "connection_service: Initializing RFCOM Bluetooth Connection.");

        String name = device.getName();
        String address = device.getAddress();
        Toast.makeText(this, name + " @ " + address, Toast.LENGTH_LONG).show();

        bt_connection.startClient(device, uuid);

        //move_activity();
    }

    public void start_connection(View v)
    {
        connect_service(BT_device, app_uuid);
    }

    public void send_message(View v)
    {
        byte[] bytes = send_et.getText().toString().getBytes(Charset.defaultCharset());
        bt_connection.write(bytes);
    }

    public void move_activity()
    {
        Intent a = new Intent(this, Controller.class);
        startActivity(a);
    }


}

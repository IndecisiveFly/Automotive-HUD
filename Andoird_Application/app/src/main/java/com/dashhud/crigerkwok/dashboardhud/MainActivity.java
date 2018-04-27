package com.dashhud.crigerkwok.dashboardhud;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final String TAG = "Main";

    LinearLayout connect_layout;
    LinearLayout control_layout;

    SharedPreferences pref;

    //Before connection established
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

    private BT_Service bt_service;

    //After connection established elements
    TextView device_status;
    TextView dest_status;
    TextView fm_current;
    TextView saved_fm;

    TextView location_is;
    TextView current_address;
    TextView current_speed_calc;
    Spinner color_spinner;
    Spinner timer_spinner;

    String old_address = "";
    String old_speed = "";
    Integer address_counter = 0;
    Integer address_timer = 0;
    String timer_choice;
    String last_color;
    String speed_units = "";
    Button switch_speed;

    Geocoder gc;
    List<Address> addresses;
    LocationManager locationManager;
    Double latitude;
    Double longitude;
    int speed;
    float get_speed;
    float mph_calc;
    Location prev_location;

    Button set_station;
    String station;

    Boolean sent;
    Integer counter;

    ImageButton fm_back;
    ImageButton fm_forward;

    SeekBar fm_select;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect_layout = findViewById(R.id.connect_layout);
        control_layout = findViewById(R.id.control_layout);

        pref = getSharedPreferences("status", Context.MODE_PRIVATE);

        //Elements of "before"
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
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(broadcast_receiver, filter);

        new_devices_list.setOnItemClickListener(MainActivity.this);

        //Elements of "after"
        device_status = findViewById(R.id.device_connection);
        dest_status = findViewById(R.id.dest_set_text);
        fm_current = findViewById(R.id.FM_frequency_text);
        saved_fm = findViewById(R.id.saved_station_str);

        location_is = findViewById(R.id.location_is);
        current_address = findViewById(R.id.location_address);
        current_speed_calc = findViewById(R.id.speed_mph);

        sent = false;
        counter = 5;

        switch_speed = findViewById(R.id.switch_units);
        speed_units = pref.getString("speed_units", "mph");
        color_spinner = findViewById(R.id.color_change_sp);
        timer_spinner = findViewById(R.id.timer_change_sp);
        address_timer = pref.getInt("timer_choice", 5);
        last_color = pref.getString("color_choice", "White");
        ArrayAdapter<CharSequence> color_adapter = ArrayAdapter.createFromResource(this,
                R.array.colors_array, android.R.layout.simple_spinner_item);
        ArrayAdapter<CharSequence> timer_adapter = ArrayAdapter.createFromResource(this,
                R.array.timer_array, android.R.layout.simple_spinner_item);
        color_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timer_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        color_spinner.setAdapter(color_adapter);
        timer_spinner.setAdapter(timer_adapter);

        color_spinner.setSelection(pref.getInt("color_position", 0));
        timer_spinner.setSelection(pref.getInt("timer_position", 0));

        color_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String color_choice = parent.getItemAtPosition(position).toString();
                change_color(color_choice);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        timer_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timer_choice = parent.getItemAtPosition(position).toString();
                change_timer(timer_choice);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        gc = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        set_station = findViewById(R.id.set_station_btn);
        fm_back = findViewById(R.id.FM_prev);
        fm_forward = findViewById(R.id.FM_next);
        fm_select = findViewById(R.id.FM_radio_select);

        saved_fm.setText(pref.getString("saved_fm", ""));
        fm_current.setText(pref.getString("last_fm", "88.1"));
        station = pref.getString("saved_fm", "");
        switch_speed.setText(pref.getString("speed_type", "Switch to km/h"));

        String last = pref.getString("last_fm", "88.1");
        Double a = Double.parseDouble(last);
        a = a - 88.1;
        int b = (int) (a/0.2);
        fm_select.setProgress(b);

        check_bt();
        check_perms();
        update();

        final Handler handler=new Handler();
        handler.post(new Runnable(){
            @Override
            public void run() {
                update();
                handler.postDelayed(this,1000); // set time here to refresh textView
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(broadcast_receiver);
        BT_adapter.cancelDiscovery();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        check_bt();
        check_perms();
        update();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        System.exit(0);
    }

    //Begin section of "before" area functions
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
            Set<BluetoothDevice> paired_devices = BT_adapter.getBondedDevices();
            for(BluetoothDevice paired_device : paired_devices)
            {
                if(paired_device.getName().equals(last_used_device))
                {
                    BT_device = paired_device;

                    device_found = true;

                    bt_service = new BT_Service(MainActivity.this);
                    connect_service(BT_device, app_uuid);

                    transition_control();

                    break;
                }
                else
                {
                    device_found = false;
                }
            }
            /*if(!device_found)
            {
                toastText = "Previous device not found";
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
                BT_reconnect.setEnabled(false);
            }*/
        }
        else
        {
            toastText = "No previous device paired with this application";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    //onClick for showing a list of devices
    public void find_devices(View v)
    {
        BT_devices_list = new ArrayList<>();
        device_list_adapter = new Device_List_Adapter(this, R.layout.activity_device__list__adapter, BT_devices_list);
        new_devices_list.setAdapter(device_list_adapter);

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
                    bt_service = new BT_Service(MainActivity.this);
                    connect_service(device, app_uuid);
                    break;
                }
            }
            //String text = "No connection found";
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
        //else {
            //create the bond
            Log.d(TAG, "Trying to pair with " + device_name);
            BT_devices_list.get(i).createBond();

            BT_device = BT_devices_list.get(i);

            //bt_service = new BT_Service(MainActivity.this);
            //connect_service(BT_device, app_uuid);
            //connect gets called once connection is accepted by clicked device
        //}
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
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
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
                    //bt_service.startClient(BT_device, app_uuid);
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
                Toast.makeText(MainActivity.this, "Successfully connected to device", Toast.LENGTH_SHORT).show();
                transition_control();
            }

            if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            {
                Toast.makeText(MainActivity.this, "Device connection dropped", Toast.LENGTH_SHORT).show();
                transition_bt();
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

        bt_service.startClient(device, uuid);
    }

    public void send_message(View v)
    {
        byte[] bytes = send_et.getText().toString().getBytes(Charset.defaultCharset());
        bt_service.write(bytes);
    }
    //End area of "before" functions






    //Transition area start
    public void transition_view(View v)
    {
        if(connect_layout.getVisibility() == View.VISIBLE)
        {
            connect_layout.setVisibility(View.GONE);
        }
        else
        {
            connect_layout.setVisibility(View.VISIBLE);
        }
        if(connect_layout.getVisibility() == View.VISIBLE)
        {
            control_layout.setVisibility(View.GONE);
        }
        else
        {
            control_layout.setVisibility(View.VISIBLE);
        }
    }

    //change view to controls
    public void transition_control()
    {
        connect_layout.setVisibility(View.GONE);
        control_layout.setVisibility(View.VISIBLE);

        counter = 0;
    }

    //change view to bluetooth buttons
    public void transition_bt()
    {
        control_layout.setVisibility(View.GONE);
        connect_layout.setVisibility(View.VISIBLE);
    }
    //Transition area end






    //Start area of "after" functions
    public void update()
    {
        //update phone's location
        try {
            addresses = gc.getFromLocation(latitude, longitude, 1);
            //String address_l = addresses.get(0).getAddressLine(0);
            String st_num = addresses.get(0).getSubThoroughfare();
            String st_name = addresses.get(0).getThoroughfare();
            String city = addresses.get(0).getLocality();
            String address = st_num + " " + st_name + ", " + city;
            current_address.setText(address);

            String calc_mph = Integer.toString(speed);
            String display_speed;
            if(speed_units.equals("mph"))
            {
                display_speed = calc_mph + " MPH";
            }
            else
            {
                display_speed = calc_mph + " km/h";
            }
            current_speed_calc.setText(display_speed);

            if (!old_speed.equals(calc_mph))
            {
                try {
                    old_speed = calc_mph;
                    String speed_package = "s " + calc_mph;
                    Log.d(TAG, speed_package);
                    byte[] bytes = speed_package.getBytes(Charset.defaultCharset());
                    bt_service.write(bytes);
                } catch (NullPointerException d) {

                }
            }

            if (address_counter.equals(address_timer))
            {
                try {
                    address_counter = 0;
                    String address_package = "l " + address;
                    Log.d(TAG, address_package);
                    byte[] bytes = address_package.getBytes(Charset.defaultCharset());
                    bt_service.write(bytes);
                } catch (NullPointerException d) {

                }
            }

            if (old_address.equals(address)) {
                address_counter++;
            } else {
                address_counter = 0;
            }

            old_address = address;
            //String timer_status = address_counter + " out of " + address_timer;
            //Log.d(TAG, timer_status);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        //keep track of seekbar progress, ensure seekbar matches up with value in textview
        fm_select.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean from_user) {

                Double sb_current = progress * 0.2;
                sb_current = sb_current + 88.1;
                DecimalFormat df = new DecimalFormat("#.#");
                df.setRoundingMode(RoundingMode.CEILING);
                station = df.format(sb_current);
                fm_current.setText(station);

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("last_fm", station);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if(!sent) {
            wait_to_send();
            counter++;
        }
    }

    public void next_station(View v)
    {
        fm_select.incrementProgressBy(1);
    }

    public void prev_station(View v)
    {
        fm_select.incrementProgressBy(-1);
    }

    public void save_station(View v)
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("saved_fm", station);
        editor.apply();

        saved_fm.setText(station);

        /*try{
            byte[] bytes = station.getBytes(Charset.defaultCharset());
            bt_service.write(bytes);
        }
        catch (NullPointerException d)
        {

        }*/

        String toastText = "Saved: " + station;
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

        //finish();
        //startActivity(getIntent());
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            try {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                get_speed = location.getSpeed();

                //Log.d(TAG, "Coords: " + latitude + " lat " + longitude + " long at speed of: " + get_speed);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mph_calc = get_speed * (float) 2.236;

            if(speed_units.equals("km/h"))
            {
                mph_calc = (float) (mph_calc * 1.60934);
            }

            speed = Math.round(mph_calc);
            prev_location = location;

            //update();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    };

    public void check_perms()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        locationManager.requestLocationUpdates(GPS_PROVIDER,1000,0, locationListener);
        locationListener.onLocationChanged(location);
    }



    //depending on user's choice of location timer update, location_timer will be set
    //make use of spinner selection
    public void change_timer(String timer)
    {
        //spinner position integer
        int selection = timer_spinner.getSelectedItemPosition();
        //displayed integer value for selected spinner position
        Integer int_timer = Integer.parseInt(timer);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("timer_choice", int_timer);
        editor.putInt("timer_position", selection);
        editor.apply();

        address_timer = int_timer;
        address_counter = 0;
    }

    //change color based on spinner choice, similar to above
    public void change_color(String color)
    {
        int selection = color_spinner.getSelectedItemPosition();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("color_choice", color);
        editor.putInt("color_position", selection);
        editor.apply();

        switch (color) {
            case "White":
                current_speed_calc.setTextColor(Color.WHITE);
                break;
            case "Yellow":
                current_speed_calc.setTextColor(Color.YELLOW);
                break;
            case "Blue":
                current_speed_calc.setTextColor(Color.BLUE);
                break;
            case "Lime":
                current_speed_calc.setTextColor(Color.GREEN);
                break;
            case "Cyan":
                current_speed_calc.setTextColor(Color.CYAN);
                break;
            case "Red":
                current_speed_calc.setTextColor(Color.RED);
                break;
        }

        try{
            color = "c " + color;
            Log.d(TAG, color);
            byte[] bytes = color.getBytes(Charset.defaultCharset());
            bt_service.write(bytes);
        }
        catch (NullPointerException d)
        {

        }
    }

    //change units button, just swaps units between mph and km/h
    public void change_units(View v)
    {
        String type;
        if(speed_units.equals("mph"))
        {
            speed_units = "km/h";
            type = "Change to MPH";
            switch_speed.setText(type);

            try{
                String units= "k";
                Log.d(TAG, units);
                byte[] bytes = units.getBytes(Charset.defaultCharset());
                bt_service.write(bytes);
            }
            catch (NullPointerException d)
            {

            }
        }
        else
        {
            speed_units = "mph";
            type = "Change to km/h";
            switch_speed.setText(type);

            try{
                String units= "m";
                Log.d(TAG, units);
                byte[] bytes = units.getBytes(Charset.defaultCharset());
                bt_service.write(bytes);
            }
            catch (NullPointerException d)
            {

            }
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putString("speed_type", type);
        editor.putString("speed_units", speed_units);
        editor.apply();
    }

    public void toggle_displays(View v)
    {
        if(current_speed_calc.getVisibility() == View.VISIBLE)
        {
            location_is.setVisibility(View.GONE);
            current_address.setVisibility(View.GONE);
            current_speed_calc.setVisibility(View.GONE);
        }
        else
        {
            location_is.setVisibility(View.VISIBLE);
            current_address.setVisibility(View.VISIBLE);
            current_speed_calc.setVisibility(View.VISIBLE);
        }
    }

    public void wait_to_send()
    {
        Log.d(TAG, "wait_to_send called, counter at " + counter);
        if(!sent) {
            if (counter == 4) {
                try {
                    String color_package = "c " + last_color;
                    Log.d(TAG, color_package);
                    byte[] bytes = color_package.getBytes(Charset.defaultCharset());
                    bt_service.write(bytes);
                } catch (NullPointerException d) {

                }
                if (speed_units.equals("mph")) {
                    try {
                        String units = "m";
                        Log.d(TAG, units);
                        byte[] bytes = units.getBytes(Charset.defaultCharset());
                        bt_service.write(bytes);
                    } catch (NullPointerException d) {

                    }
                } else {
                    try {
                        String units = "k";
                        Log.d(TAG, units);
                        byte[] bytes = units.getBytes(Charset.defaultCharset());
                        bt_service.write(bytes);
                    } catch (NullPointerException d) {

                    }
                }
                sent = true;
            }

        }
    }
}

package com.dashhud.crigerkwok.dashboardhud;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import java.io.IOException;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static android.location.LocationManager.GPS_PROVIDER;

public class Control_and_location extends AppCompatActivity implements LocationListener {

    private static final String TAG = "Control and Location";

    TextView device_status;
    TextView dest_status;
    TextView fm_current;
    TextView saved_fm;

    TextView current_address;
    TextView current_speed_calc;

    Geocoder gc;
    List<Address> addresses;
    LocationManager locationManager;
    Double latitude;
    Double longitude;
    int speed;
    float get_speed;
    float mph_calc;
    Location prev_location;

    Button navigation;
    Button set_station;

    BT_Connection bt_connection;
    Button connect;
    TextView other_station;
    //private static final UUID app_uuid = UUID.fromString("00101101-0000-1000-8000-A0803F9B34FB");
    BluetoothDevice bt_device;

    ImageButton fm_back;
    ImageButton fm_forward;

    SeekBar fm_select;

    //String connected = "Connected";
    //String not_connected = "Not Connected";

    SharedPreferences pref;
    SharedPreferences dest_pref;

    String station;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_and_location);

        device_status = findViewById(R.id.device_connection);
        dest_status = findViewById(R.id.dest_set_text);
        fm_current = findViewById(R.id.FM_frequency_text);
        saved_fm = findViewById(R.id.saved_station_str);

        current_address = findViewById(R.id.location_address);
        current_speed_calc = findViewById(R.id.speed_mph);

        gc = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        navigation = findViewById(R.id.setup_nav_btn);
        set_station = findViewById(R.id.set_station_btn);

        //start send/receive data section
        other_station = findViewById(R.id.saved_from_other);
        connect = findViewById(R.id.connect_bt_service);
        //end send/receive data section

        fm_back = findViewById(R.id.FM_prev);
        fm_forward = findViewById(R.id.FM_next);

        fm_select = findViewById(R.id.FM_radio_select);

        pref = getSharedPreferences("status", Context.MODE_PRIVATE);
        dest_pref = getSharedPreferences("destination", Context.MODE_PRIVATE);

        saved_fm.setText(pref.getString("saved_fm", ""));
        fm_current.setText(pref.getString("last_fm", "88.1"));
        station = pref.getString("saved_fm", "");

        String last = pref.getString("last_fm", "88.1");
        Double a = Double.parseDouble(last);
        a = a - 88.1;
        int b = (int) (a/0.2);
        fm_select.setProgress(b);

        //get selected device and start bluetooth connection service when this activity launches.
        Gson gson = new Gson();
        String json = pref.getString("device", "");
        bt_device = gson.fromJson(json, BluetoothDevice.class);

        //keep statuses current
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
    protected void onResume() {
        super.onResume();
        check_perms();
        update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(this);
    }

    public void update()
    {
        //update phone's location
        try {
            addresses = gc.getFromLocation(latitude, longitude, 1);
            String address_l = addresses.get(0).getAddressLine(0);
            //Log.d(TAG, address_l);
            current_address.setText(address_l);

            String calc_mph = Integer.toString(speed) + "  MPH";
            current_speed_calc.setText(calc_mph);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //keep track of seekbar progress, ensure seekbar matches up with value in textview
        fm_select.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean from_user) {

                //progress = pref.getInt("current_station", 0);

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

        //byte[] bytes = station.getBytes(Charset.defaultCharset());
        //bt_connection.write(bytes);

        String toastText = "Saved: " + station;
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

        finish();
        startActivity(getIntent());
    }

    //Manual attempt at starting connection service
    public void connect_service(View v)
    {
        String name = bt_device.getName();
        String address = bt_device.getAddress();
        Toast.makeText(this, name + " @ " + address, Toast.LENGTH_LONG).show();
        //bt_connection.startClient(bt_device, app_uuid);
    }

    public void to_destination(View v)
    {
        Intent a = new Intent(Control_and_location.this, Destination_Info.class);
        startActivity(a);
    }

    public void transfer_connection(BT_Connection connection)
    {
        bt_connection = connection;
    }

    @Override
    public void onLocationChanged(Location location) {

        try {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            get_speed = location.getSpeed();

            Log.d(TAG, "Coords: " + latitude + " lat " + longitude + " long at speed of: " + get_speed);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        mph_calc = get_speed * (float) 2.236;
        speed = Math.round(mph_calc);
        prev_location = location;

        update();
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
        locationManager.requestLocationUpdates(GPS_PROVIDER,1000,0,this);
        onLocationChanged(location);
    }
}

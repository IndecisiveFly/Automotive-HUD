package com.dashhud.crigerkwok.dashboardhud;

import android.Manifest;
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
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.GPS_PROVIDER;

public class Destination_Info extends AppCompatActivity implements LocationListener{

    TextView current_address;
    TextView current_speed_calc;
    TextView get_speed_mps;
    TextView get_speed_mph;

    TextView saved_address;
    TextView saved_state;
    TextView saved_city;

    EditText address;
    EditText state;
    EditText city;

    Button set_dest;

    Geocoder gc;
    List<Address> addresses;
    LocationManager locationManager;
    Double latitude;
    Double longitude;
    int speed;
    float get_speed;
    float mph_calc;
    Location prev_location;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination__info);

        current_address = findViewById(R.id.current_address);
        current_speed_calc = findViewById(R.id.get_speed_round);
        get_speed_mps = findViewById(R.id.get_speed_mps);
        get_speed_mph = findViewById(R.id.get_speed_mph);

        saved_address = findViewById(R.id.saved_address);
        saved_state = findViewById(R.id.saved_state);
        saved_city = findViewById(R.id.saved_city);

        address = findViewById(R.id.address_edit);
        state = findViewById(R.id.state_edit);
        city = findViewById(R.id.city_edit);

        set_dest = findViewById(R.id.set_destination_btn);

        gc = new Geocoder(this, Locale.getDefault());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        pref = getSharedPreferences("destination", Context.MODE_PRIVATE);

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
        update();
    }

    public void update()
    {
        //update phone's location
        try {
            addresses = gc.getFromLocation(latitude, longitude, 1);
            String address_l = addresses.get(0).getAddressLine(0);
            current_address.setText(address_l);
            String mph = Integer.toString(speed) + "  MPH";
            String mps = Float.toString(get_speed) + "  M/S";
            String calc_mph = Float.toString(mph_calc) + "  MPH";
            current_speed_calc.setText(mph);
            get_speed_mps.setText(mps);
            get_speed_mph.setText(calc_mph);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //update saved destination for current session
        saved_address.setText(pref.getString("saved_address", ""));
        saved_state.setText(pref.getString("saved_state", ""));
        saved_city.setText(pref.getString("saved_city", ""));
    }

    public void set_destination(View v)
    {
        String address_s = address.getText().toString();
        String state_s = state.getText().toString();
        String city_s = city.getText().toString();

        //save edit text fields into shared preferences for this session
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("saved_address", address_s);
        editor.putString("saved_state", state_s);
        editor.putString("saved_city", city_s);
        editor.apply();

        //move back to controller
        Intent a = new Intent(Destination_Info.this, Controller.class);
        startActivity(a);
    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        get_speed = location.getSpeed();
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
        locationManager.requestLocationUpdates(GPS_PROVIDER,500,0,this);
        onLocationChanged(location);
    }
}

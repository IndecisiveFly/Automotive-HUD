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
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Destination_Info extends AppCompatActivity implements LocationListener{

    TextView current_address;
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

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination__info);

        current_address = findViewById(R.id.current_address);
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
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
        Location location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        onLocationChanged(location);
    }
}

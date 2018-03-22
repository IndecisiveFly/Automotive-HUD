package com.dashhud.crigerkwok.dashboardhud;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Destination_Info extends AppCompatActivity {

    TextView current_address;
    TextView saved_address;
    TextView saved_state;
    TextView saved_city;

    EditText address;
    EditText state;
    EditText city;

    Button set_dest;

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

        pref = getSharedPreferences("destination", Context.MODE_PRIVATE);

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
}

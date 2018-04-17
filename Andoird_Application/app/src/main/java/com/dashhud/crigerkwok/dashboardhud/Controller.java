package com.dashhud.crigerkwok.dashboardhud;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.UUID;

public class Controller extends AppCompatActivity {

    TextView device_status;
    TextView dest_status;
    TextView fm_current;
    TextView saved_fm;

    Button navigation;
    Button set_station;

    BT_Service bt_service;
    Button connect;
    TextView other_station;
    private static final UUID app_uuid = UUID.fromString("00101101-0000-1000-8000-A0803F9B34FB");
    BluetoothDevice bt_device;

    ImageButton fm_back;
    ImageButton fm_forward;

    SeekBar fm_select;

    String connected = "Connected";
    String not_connected = "Not Connected";

    SharedPreferences pref;
    SharedPreferences dest_pref;

    String station;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);

        device_status = findViewById(R.id.device_connection);
        dest_status = findViewById(R.id.dest_set_text);
        fm_current = findViewById(R.id.FM_frequency_text);
        saved_fm = findViewById(R.id.saved_station_str);

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
        update_station();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dest_pref.edit().clear().apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        update_station();
    }

    public void update_station()
    {
        //check if there is a destination set (if any of the 3 strings are null, not set)
        String a = dest_pref.getString("saved_address", "");
        String s = dest_pref.getString("saved_state", "");
        String c = dest_pref.getString("saved_city", "");
        if(a.equals("") || s.equals("") || c.equals(""))
        {
            dest_status.setText("Not Set");
        }
        else
        {
            dest_status.setText("Set");
        }

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
        //bt_service.write(bytes);

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
        //bt_service.startClient(bt_device, app_uuid);
    }

    public void to_destination(View v)
    {
        Intent a = new Intent(Controller.this, Destination_Info.class);
        startActivity(a);
    }

    public void transfer_connection(BT_Service connection)
    {
        bt_service = connection;
    }
}

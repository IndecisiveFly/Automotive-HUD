package com.dashhud.crigerkwok.dashboardhud;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.UUID;

public class Controller extends AppCompatActivity {

    TextView device_status;
    TextView gmaps_status;
    TextView fm_current;
    TextView saved_fm;

    Button connect_gmaps;
    Button set_station;

    //BT_Connection bt_connection;
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
        gmaps_status = findViewById(R.id.maps_connected_text);
        fm_current = findViewById(R.id.FM_frequency_text);
        saved_fm = findViewById(R.id.saved_station_str);

        connect_gmaps = findViewById(R.id.connect_google_maps);
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

        //start bluetooth connection service when this activity launches.
        //bt_device = bt_connection.getDevice();

        //keep statuses current
        update_station();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dest_pref.edit().clear().apply();
    }

    public void update_station()
    {
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

    /* Manual attempt at starting connection service
    public void connect_service(View v)
    {
        bt_connection.startClient(bt_device, app_uuid);
    }
    */

    public void google_maps(View v)
    {
        Intent a = new Intent(Controller.this, Destination_Info.class);
        startActivity(a);
    }
}

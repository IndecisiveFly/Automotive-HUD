package com.dashhud.crigerkwok.dashboardhud;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Activity1 extends AppCompatActivity {

    TextView gmaps_status;
    TextView fm_current;

    Button connect_gmaps;

    ImageButton fm_back;
    ImageButton fm_forward;

    SeekBar fm_select;

    String connected = "Connected";
    String not_connected = "Not Connected";

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1);

        gmaps_status = findViewById(R.id.maps_connected_text);
        fm_current = findViewById(R.id.FM_frequency_text);

        connect_gmaps = findViewById(R.id.connect_google_maps);

        fm_back = findViewById(R.id.FM_prev);
        fm_forward = findViewById(R.id.FM_next);

        fm_select = findViewById(R.id.FM_radio_select);

        pref = getSharedPreferences("status", Context.MODE_PRIVATE);

        update_station();

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
                String station = df.format(sb_current);
                fm_current.setText(station);

                /*SharedPreferences.Editor editor = pref.edit();
                editor.putInt("current_station", fm_select.getProgress());
                editor.apply();*/
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
}

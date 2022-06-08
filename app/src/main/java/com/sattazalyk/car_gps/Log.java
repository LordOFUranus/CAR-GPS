package com.sattazalyk.car_gps;

import android.location.Location;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

public class Log extends AppCompatActivity {

    ListView lv_log;
    Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        lv_log = findViewById(R.id.lv_log);
        btn_delete = findViewById(R.id.btn_delete);

        GPS_Data gps_data = (GPS_Data)getApplicationContext();
        List<Location> locationList = gps_data.getLocationPoints();

        lv_log.setAdapter(new ArrayAdapter<Location>(this, android.R.layout.simple_list_item_1,locationList));
        btn_delete.setOnClickListener(view->{
            locationList.clear();
            recreate();
        });
    }
}
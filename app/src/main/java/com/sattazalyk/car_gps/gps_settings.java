package com.sattazalyk.car_gps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.google.android.gms.location.*;
import com.google.android.material.navigation.NavigationView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.sattazalyk.car_gps.GPS_Data;

import java.util.List;

public class gps_settings extends AppCompatActivity implements View.OnClickListener {
    public static final int DEFAULT_UPDATE_INTERVAL =25;
    public static final int FAST_UPDATE_INTERVAL = 1;
    private static final int PERMISSION_FINE_LOCATION = 99;


    //UI

    private TextView tv_lat, tv_lon, tv_altitude, tv_speed, tv_sensor, tv_update, tv_adress, tv_accuracy, tv_point_count;

    private Switch sw_locationupdates, sw_gps;

    private Button btn_map, btn_point, btn_log_point,btn_web, btn_content;

    boolean updateOn = false;
    private NavigationView nv_map;
    //Location Request  - настройки для FusedLocationProviderClient

    LocationRequest locationRequest;

    // Google API локационные сервисы
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallBack;

    Location locationLog;

    List<Location> locationLogList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_settings);

        ///присвоение переменным UI в gps_settings

        nv_map = findViewById(R.id.nv_map);
        nv_map.setItemIconTintList(null);


        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_update = findViewById(R.id.tv_updates);
        tv_adress = findViewById(R.id.tv_address);
        tv_point_count = findViewById(R.id.tv_point_count);

        sw_gps = findViewById(R.id.sw_gps_map);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates_map);

        btn_map = findViewById(R.id.btn_map);
        btn_point = findViewById(R.id.btn_point);
        btn_log_point = findViewById(R.id.btn_log_point);
        btn_web = findViewById(R.id.btn_web);
        btn_content = findViewById(R.id.btn_content);

        ///настройки приоритетов локационных запросов Request

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateUIValues(locationResult.getLastLocation());
            }
        };
        btn_content.setOnClickListener(view -> {
            Intent intent = new Intent(gps_settings.this, content.class);
            startActivity(intent);
        });


        btn_web.setOnClickListener(view -> {
            Intent intent = new Intent(gps_settings.this, WIALON.class);
            startActivity(intent);
        });
        sw_gps.setOnClickListener(view -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Используется GPS-датчик");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Используется башни и Wi-Fi");
            }
        });

        sw_locationupdates.setOnClickListener(view -> {
            if (sw_locationupdates.isChecked()) {
                startLocationUpdates();
            } else {
                stopLocationUpdate();
            }
        });


        btn_point.setOnClickListener(view -> {
            GPS_Data gps_data = (GPS_Data) getApplicationContext();
            locationLogList = gps_data.getLocationPoints();
            locationLogList.add(locationLog);
        });

        btn_log_point.setOnClickListener(view -> {
            Intent intent = new Intent(gps_settings.this, Log.class);
            startActivity(intent);

        });

        updateGPS();
    }

    private void startLocationUpdates() {
        tv_update.setText("Локация отслеживается");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
        updateGPS();
    }

    private void stopLocationUpdate() {
        tv_update.setText("Данные не отслеживается");
        tv_adress.setText("Не отслеживается");
        tv_speed.setText("Не отслеживается");
        tv_lat.setText("Не отслеживается");
        tv_lon.setText("Не отслеживается");
        tv_altitude.setText("Не отслеживается");
        tv_accuracy.setText("Не отслеживается");
        tv_sensor.setText("Не отслеживается");
        tv_point_count.setText("Не отслеживаются");

        fusedLocationProviderClient.removeLocationUpdates(locationCallBack);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERMISSION_FINE_LOCATION:;
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                updateGPS();
            }
            else{
                Toast.makeText(this, "Этому приложению нужно разрешение на местоположение",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void updateGPS(){
        //получение разрешения местоположения

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(gps_settings.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            //предоставление разрешения
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                //получение разрешения. Вкладывание значение местоположения в UI
                updateUIValues(location);
                locationLog = location;
            });
        }
        else{
            //не получено разрешение
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.M){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION);
            }
        }

    }

    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        if(location.hasAltitude()){
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        }
        else tv_altitude.setText("Высота недоступна");
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else tv_speed.setText("Нет данных о скорости");

        Geocoder geocoder = new Geocoder(gps_settings.this);

        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_adress.setText(addressList.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_adress.setText("Невозможно получить адрес");
        }

        GPS_Data gps_data = (GPS_Data) getApplicationContext();
        locationLogList = gps_data.getLocationPoints();
        tv_point_count.setText(Integer.toString(locationLogList.size()));

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_map) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
    }

}
package com.sattazalyk.car_gps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.GeoApiContext;
import com.sattazalyk.car_gps.databinding.ActivityMapsBinding;
import com.sattazalyk.car_gps.directionhelper.FetchURL;
import com.sattazalyk.car_gps.directionhelper.TaskLoadedCallback;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.sattazalyk.car_gps.GPS_Data;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    //UI
    private TextView tv_geocode, tv_pointcounter_map, tv_user_name;
    private Switch sw_locationupdates, sw_gps;
    private Button btn_settings, btn_log, btn_point_map;


    private  boolean sw_locationupdates_isChecked;
    private  boolean sw_gps_isChecked;



    private LatLng u;

    //Location Request  - настройки для FusedLocationProviderClient

    LocationRequest locationRequest;

    // Google API локационные сервисы
    FusedLocationProviderClient fusedLocationProviderClient;

    LocationCallback locationCallBack;


    //Скорость запросов
    public static final int DEFAULT_UPDATE_INTERVAL =10;
    public static final int FAST_UPDATE_INTERVAL = 5;

    private static final int PERMISSION_FINE_LOCATION = 99;


    List<Location> locationLogList;
    Location locationLog;

    Polyline userPolyline;
    MarkerOptions uMarker;

    LatLng post = new LatLng(53.284792,69.382134);
    MarkerOptions placeB = new MarkerOptions().position(post).title("Почта");

    private GeoApiContext mGeoApiContext = null;


    //инициализации
    private void initializationUI(){
        tv_geocode = findViewById(R.id.tv_geocode);
        tv_pointcounter_map = findViewById(R.id.tv_pointcounter_map);
        sw_gps = findViewById(R.id.sw_gps_map);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates_map);
        btn_settings = findViewById(R.id.btn_settings);
        btn_log = findViewById(R.id.btn_log);
        btn_point_map = findViewById(R.id.btn_point_map);

    }

    private void initializationLocationRequest(){
        locationRequest = new LocationRequest();
        locationRequest.setInterval(100 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(100 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        locationCallBack = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull @NotNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateUIValues(locationResult.getLastLocation());
            }
        };
    }


    //SharePref

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String SWITCH_UPDATE = "sw_locationupdates";
    public static final String SWITCH_GPS = "sw_gps";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        initializationUI();
        initializationLocationRequest();

        sw_gps.setOnClickListener(view -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                saveData();
                //tv_sensor.setText("Используется GPS-датчик");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                saveData();
                //tv_sensor.setText("Используется башни и Wi-Fi");
            }
        });

        sw_locationupdates.setOnClickListener(view -> {
            if (sw_locationupdates.isChecked()) {
                saveData();
                startLocationUpdates();
            } else {
                stopLocationUpdate();
                saveData();
            }
        });

        btn_log.setOnClickListener(v->{
            Intent intent = new Intent(MapsActivity.this, Log.class);
            startActivity(intent);
        });

        btn_point_map.setOnClickListener(view -> {
            GPS_Data gps_data = (GPS_Data) getApplicationContext();
            locationLogList = gps_data.getLocationPoints();
            locationLogList.add(locationLog);
        });

        loadData();

        GPS_Data gps_data = (GPS_Data) getApplicationContext();
        locationLogList = gps_data.getLocationPoints();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateGPS();

    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(SWITCH_UPDATE, sw_locationupdates.isChecked());
        editor.putBoolean(SWITCH_GPS, sw_gps.isChecked());
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        sw_locationupdates_isChecked = sharedPreferences.getBoolean(SWITCH_UPDATE, false);
        sw_gps_isChecked = sharedPreferences.getBoolean(SWITCH_GPS, false);

        sw_locationupdates.setChecked(sw_locationupdates_isChecked);
        sw_gps.setChecked(sw_gps_isChecked);
    }

    private void startLocationUpdates() {
        //tv_update.setText("Локация отслеживается");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.getMainLooper());
        updateGPS();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(u, 17f));
    }

    private void stopLocationUpdate() {

      /*tv_speed.setText("Не отслеживается");
        tv_lat.setText("Не отслеживается");
        tv_lon.setText("Не отслеживается");
        tv_accuracy.setText("Не отслеживается");;*/

        tv_pointcounter_map.setText("-");
        tv_geocode.setText("Не отслеживается");
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            //предоставление разрешения
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                u = new LatLng(location.getLatitude(), location.getLongitude());
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
        /*tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        if(location.hasSpeed()){
            tv_speed.setText(String.valueOf(location.getSpeed()));
        }
        else tv_speed.setText("Нет данных о скорости");*/

        Geocoder geocoder = new Geocoder(MapsActivity.this);

        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_geocode.setText(addressList.get(0).getAddressLine(0));
        }
        catch (Exception e){
            tv_geocode.setText("Невозможно получить адрес");
        }

        GPS_Data gps_data = (GPS_Data) getApplicationContext();
        locationLogList = gps_data.getLocationPoints();
        tv_pointcounter_map.setText(Integer.toString(locationLogList.size()));
        uMarker = new MarkerOptions().position(u).title("Ваше местоположение");
        try {
            mMap.addMarker(uMarker);
            mMap.addMarker(placeB);;
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for (Location l : locationLogList) {
            LatLng latLng = new LatLng(l.getLatitude(), l.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Lat: "+l.getLatitude() +" Lon: "+l.getLongitude()+" #"+ locationLogList.indexOf(l)+1);
            mMap.addMarker(markerOptions);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_settings){
            Intent intent = new Intent(this, gps_settings.class);
            startActivity(intent);
        }
    }

    private void mapInit(){
        if(mGeoApiContext ==null){
            mGeoApiContext= new GeoApiContext.Builder().apiKey(getString(R.string.google_maps_key)).build();
        }
    }


}


    /////wialon



   /*
    private String getUrl(LatLng origin, LatLng destination, String directionMode) {
        String strOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String strDestination = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=" + directionMode;
        String parametrs = strOrigin + "&" + strDestination + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output+ "?" + parametrs
                +"&key="+getString(R.string.google_maps_key);
        return url;
    }

    String url = getUrl(uMarker.getPosition(),placeB.getPosition(), "driving");
        new FetchURL(MapsActivity.this).execute(url,"driving");

    @Override
    public void onTaskDone(Object... values) {
        if(userPolyline!=null) userPolyline.remove();
        userPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

    */

    /* private void direction(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = Uri.parse("https://maps.googleapis.com/maps/api/directions/json")
                .buildUpon().appendQueryParameter("origin",String.valueOf(u))
                .appendQueryParameter("destination", String.valueOf(post))
                .appendQueryParameter("mode","driving")
                .appendQueryParameter("key", "AIzaSyDsQJ0Z1CkOGcu0DOoU79JZ_NMvuXy9oE8")
                .toString();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String status = response.getString("status");
                    if(status.equals("OK")){

                        JSONArray routes = response.getJSONArray("routes");
                        ArrayList<LatLng> points;
                        PolylineOptions polylineOptions = null;

                        for (int i=0; i<routes.length();i++){
                            points = new ArrayList<>();
                            polylineOptions = new PolylineOptions();
                            JSONArray legs = routes.getJSONObject(i).getJSONArray("legs");

                            for (int j=0; j<legs.length(); j++){
                                JSONArray steps  = legs.getJSONObject(j).getJSONArray("steps");
                                for(int k=0; k<steps.length();k++){
                                    String polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points");
                                    List<LatLng> list = decodePoly(polyline);

                                    for(int l = 0; l< list.size();l++){
                                        LatLng position = new LatLng((list.get(l)).latitude, (list.get(l).longitude));
                                        points.add(position);
                                    }
                                }
                            }
                            polylineOptions.addAll(points);
                            polylineOptions.width(10);
                            polylineOptions.color(ContextCompat.getColor(MapsActivity.this, android.R.color.holo_green_dark));
                            polylineOptions.geodesic(true);
                        }
                        assert polylineOptions != null;
                        mMap.addPolyline(polylineOptions);
                        LatLngBounds bounds = new LatLngBounds.Builder().include(u).include(post).build();
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,point.x, 150,30));
                    }
                } catch (JSONException e){}
            }
        },
        new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

            }
        });
        RetryPolicy retryPolicy = new DefaultRetryPolicy(30000,DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonObjectRequest.setRetryPolicy(retryPolicy);
        requestQueue.add(jsonObjectRequest);
    }

    private List<LatLng> decodePoly(String encode){
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encode.length();
        int lat = 0, lng =0;
        while(index<len){
            int b, shift= 0, result = 0;
            do {
                b = encode.charAt(index++);
                result |=(b&0x1f)<<shift;
                shift +=5;

            }while (b>=0x20);
            int dlat = (result&1) !=0 ? ~(result >>1):(result >> 1);
            lat += dlat;

            shift=0;
            result=0;
            do {
                b = encode.charAt(index++) - 63;
                result |= (b & 0x1f) <<shift;
                shift+=5;

            }while (b>0x20);
        int dlng = ((result & 1) !=0?~(result):(result>>1));
        lng=dlng;
        LatLng p = new LatLng(((double) lat/1E5),
                ((double)lng/1E5) );
            poly.add(p);
        }
        return poly;
    }*/
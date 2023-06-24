1package com.android.washroomfinder;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.washroomfinder.JsonUtils;
import com.android.washroomfinder.persistance.Repository;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int LOCATION_SETTINGS_REQUEST = 100;
    MapView mapView;
    MapController mapController;
    boolean entered = false;
    double latitude = 0.0, longitude = 0.0;
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    Marker currentLocation;
    int vanish = 0;
    Marker marker;



    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean firstStart = prefs.getBoolean("firstStart", true);

        if (firstStart) {
            showStartDialog();
        }

        requestPermissions();
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        //mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setMultiTouchControls(true);
        mapController = (MapController) mapView.getController();
        mapController.setZoom(16);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        currentLocation = new Marker(mapView);



       // StreamResult result = new StreamResult(new File(android.os.Environment.getExternalStorageDirectory(), "upload_data.xml"));


        String json = JsonUtils.getJsonFromAsset(this, "info.json");
        Gson gson = new Gson();
        WashRoom myObject = gson.fromJson(json, WashRoom.class);
        for (int i = 1; i < myObject.getToiletten().size(); i++) {
            addMarker(myObject.getToiletten().get(i));
        }
        observeSavedJson();

        btnCLick();

        promptUser();
        getLastKnownLocation();
        gettingLastKnownLocation();
        setLocationCallback();
        startLocationUpdates();

        GeoPoint gpt = new GeoPoint(52.5200, 13.4050);
        mapController.setCenter(gpt);
        //observeMarkers();

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                if (marker == null){
                    currentLocation.showInfoWindow();
                }
                else if (!marker.isInfoWindowShown()){
                    currentLocation.showInfoWindow();
                }
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                if (marker == null){
                    currentLocation.showInfoWindow();
                }
                else
                if (!marker.isInfoWindowShown()){
                    currentLocation.showInfoWindow();
                }
                return false;
            }
        });

    }


    void promptUser() {

        int locationInterval = 1000;
        int locationFastestInterval = 500;
        int locationMaxWaitTime = 100;
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
                .setWaitForAccurateLocation(true)
                .setMinUpdateIntervalMillis(locationFastestInterval)
                .setMaxUpdateDelayMillis(locationMaxWaitTime)
                .build();


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {


            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });


    }

    void btnCLick() {
        Button button = findViewById(R.id.btn);
        button.setOnClickListener(v -> {
        if (latitude == 0.0){
            String name = getString(R.string.currentLocation);
            Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
        }
        else {

                Intent intent = new Intent(this, NextActivity.class);
                intent.putExtra(LATITUDE,latitude);
                intent.putExtra(LONGITUDE, longitude);
                startActivity(intent);

        }
        });
    }
    String checkError(String message){
        if (message == null || message.trim().equals("")){
            return getString(R.string.unknown);
        }
        else{
            return message;
        }
    }

    void addMarker(MainJsonClass descriptionInfo) {

        String numbers;
        String coins;
        String NFC;
        String app;
        String street;
        String postalCode;
        String price;
        String hand;

        numbers = checkError(descriptionInfo.getNumber());
        street = checkError(descriptionInfo.getStreet());
        postalCode = checkError(descriptionInfo.getPostalCode());
        price = checkError(descriptionInfo.getPrice());

        if (descriptionInfo.getHandicappedAccessible() == null || descriptionInfo.getHandicappedAccessible().equals("0")) {
            hand = getString(R.string.no);
        } else if (descriptionInfo.getHandicappedAccessible().equals("1")){
            hand = getString(R.string.yes);
        }
        else {
            hand = getString(R.string.unknown);
        }

        if (descriptionInfo.getCanBePayedWithCoins() == null || descriptionInfo.getCanBePayedWithCoins().equals("0")) {
            coins = getString(R.string.no);
        } else if (descriptionInfo.getCanBePayedWithCoins().equals("1")){
            coins = getString(R.string.yes);
        }
        else {
            coins = getString(R.string.unknown);
        }


        if (descriptionInfo.getCanBePayedWithNFC() == null || descriptionInfo.getCanBePayedWithNFC().equals("0")) {
            NFC = getString(R.string.no);
        } else if (descriptionInfo.getCanBePayedWithNFC().equals("1")){
            NFC = getString(R.string.yes);
        }
        else {
            NFC = getString(R.string.unknown);

        }


        if (descriptionInfo.getCanBePayedInApp() == null || descriptionInfo.getCanBePayedInApp().equals("0")) {
            app = getString(R.string.no);
        } else if ( descriptionInfo.getCanBePayedInApp().equals("1")){
            app = getString(R.string.yes);
        }
        else{
            app = getString(R.string.unknown);

        }



        String description =getString(R.string.street) + " : " + street + "<br/>" +
                getString(R.string.number) + " : " + numbers + "<br/>" +
                getString(R.string.postal_code) + " : " + postalCode + "<br/>" +
                getString(R.string.handicapped_accessible) + " : " + hand + "<br/> " +
                getString(R.string.price) + " : " + price + "<br/>" +
                getString(R.string.can_payed_be_coins) + " : " + coins + "<br/>" +
                getString(R.string.can_be_payed_in_app) + " : " + app + "<br/>" +
                getString(R.string.can_be_payed_with_NFC) + " : " + NFC;

        Drawable drawable = ContextCompat.getDrawable(this,R.drawable.marker);
        String lat = descriptionInfo.getLatitude().replace(",", ".");
        String lon = descriptionInfo.getLongitude().replace(",", ".");
        Marker marker = new Marker(mapView);


        marker.setPosition(new GeoPoint(Double.parseDouble(lat), Double.parseDouble(lon)));
        marker.setTitle(descriptionInfo.description);
        marker.setSnippet(description);
       // marker.setIcon(drawable);

//        marker.setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_TOP);
//        marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_TOP);
        mapView.getOverlays().add(marker);


        marker.setOnMarkerClickListener((marker1, mapView) -> {
            Log.d(TAG, "onMarkerClick: marker is clicked");
            marker1.showInfoWindow();
            MainActivity.this.marker = marker1;
            GeoPoint gpt = new GeoPoint(marker1.getPosition().getLatitude(), marker1.getPosition().getLongitude());
            mapController.animateTo(gpt);
            return false;
        });

    //    if (marker.isInfoWindowShown())
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkingLocationsEnabled(Boolean location, String locationName) {
        if (location != null && location) {
            Log.d(TAG, "MainActivity: camera enabled");
        } else {
            requestPermissions(new String[]{locationName}, LOCATION_SETTINGS_REQUEST);
        }
    }

    void requestPermissions() {
        ActivityResultLauncher<String[]> someActivityResultLauncher1 =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean courseLocation = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            Boolean backgroundLocation = result.getOrDefault(Manifest.permission.ACCESS_BACKGROUND_LOCATION, false);

                            checkingLocationsEnabled(fineLocation, Manifest.permission.ACCESS_FINE_LOCATION);
                            checkingLocationsEnabled(courseLocation, Manifest.permission.ACCESS_COARSE_LOCATION);
                            //   checkingLocationsEnabled(backgroundLocation, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                        }
                    }
                });
        ActivityResultLauncher<Intent> someActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == Activity.RESULT_OK) {
                            }
                        });

        someActivityResultLauncher1.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                //     Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            Log.d(TAG,"last known location, provider: %s, location: %s"+ provider+
                    l);

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                Log.d(TAG,"found best last known location: %s"+ l);
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }
    void setLocationCallback() {
       // Log.d(TAG, "setLocationCallback: 365456454545654");
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null){
                    Log.d(TAG, "onLocationResult: location results was null");
                    return;
                }
                Log.d(TAG, "onLocationResult: location results was not null");
                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    mapView.getOverlays().remove(currentLocation);

                  //  if (vanish % 3 != 0) {
                        Drawable drawable = ContextCompat.getDrawable(MainActivity.this, R.drawable.current_location);
                        currentLocation.setPosition(new GeoPoint(latitude, longitude));
                      //  currentLocation.setIcon(drawable);
                        currentLocation.setTitle(getString(R.string.current_location));
                        //currentLocation.showInfoWindow();
                        mapView.getOverlays().add(currentLocation);

                    if (!entered){
                        entered = true;
                        GeoPoint gpt = new GeoPoint(location.getLatitude(), location.getLongitude());
                        mapController.setCenter(gpt);
                    }
                }
            }
        };

    }
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    void gettingLastKnownLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {

                        GeoPoint gpt = new GeoPoint(location.getLatitude(), location.getLongitude());
                        mapController.setCenter(gpt);

                        Log.d(TAG, "gettingLastKnownLocation: location "+location);
                    }
                    else {
                        Log.d(TAG, "gettingLastKnownLocation: location was null");
                    }
                });


    }

    private void observeSavedJson(){
        Repository.getInstance(this).getSavedJson().observe(this, new Observer<List<MainJsonClass>>() {
            @Override
            public void onChanged(List<MainJsonClass> mainJsonClasses) {
                if (mainJsonClasses != null){
                    for (MainJsonClass jsonClass : mainJsonClasses){
                        addMarker(jsonClass);
                    }
                }
                else{
                    Log.d(TAG, "onChanged: json was null");
                }
            }
        });
    }

    private void showStartDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.instruction_title))
                .setMessage(getString(R.string.app_message))
                .setPositiveButton("ok", (dialog, which) -> dialog.dismiss())
                .create().show();
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firstStart", false);
        editor.apply();
    }
}






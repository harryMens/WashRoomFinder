package com.android.washroomfinder;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.VIBRATE;

import static com.android.washroomfinder.MainActivity.LATITUDE;
import static com.android.washroomfinder.MainActivity.LONGITUDE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import androidx.lifecycle.Observer;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.washroomfinder.persistance.Repository;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class NextActivity extends AppCompatActivity {

    private static final String TAG = "NextActivity";
    Button save;
    EditText street, number, postalCode, price, coins;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        save = findViewById(R.id.save);
        street = findViewById(R.id.street);
        number = findViewById(R.id.number);
        postalCode = findViewById(R.id.postal_code);
        price = findViewById(R.id.price);
        coins = findViewById(R.id.coins);

        if (!checkPermission()){
            requestPermission();
        }

        double latitude = getIntent().getDoubleExtra(LATITUDE,0.0);
        double longitude = getIntent().getDoubleExtra(LONGITUDE,0.0);
        save.setOnClickListener(v->{
            if (latitude != 0.0){
                MainJsonClass mainJsonClass = new MainJsonClass();
                String lat = String.valueOf(latitude);
                String lt = lat.replace(".",",");
                String lon = String.valueOf(longitude);
                String lo = lon.replace(".",",");

                mainJsonClass.setLatitude(lt);
                mainJsonClass.setLongitude(lo);
                mainJsonClass.setStreet(street.getText().toString());
                mainJsonClass.setNumber(number.getText().toString());
                mainJsonClass.setPostalCode(postalCode.getText().toString());
                mainJsonClass.setPrice(price.getText().toString());
                mainJsonClass.setCanBePayedWithCoins(coins.getText().toString());
                insertJson(mainJsonClass);

//                WashRoom washRoom = new WashRoom();
//                washRoom.setToiletten(Collections.singletonList(mainJsonClass));
//                String json = new Gson().toJson(washRoom);
//
//// write the JSON string to the file
//
//                String filename = "info.json";
//                if (isExternalStorageWritable()) {
//                    try {
//                        File file = new File(getExternalFilesDir(null), filename);
//                        FileOutputStream outputStream = new FileOutputStream(file);
//                        outputStream.write(json.getBytes());
//                        outputStream.close();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Log.e("ERROR", "External storage is not writable");
//                }

// helper method to check if external storage is writable
                finish();
            }
            else{
                Toast.makeText(this, getString(R.string.location_error), Toast.LENGTH_SHORT).show();
            }
            finish();
        });

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    private void save(Context context, String jsonString) throws IOException {
        File rootFolder = context.getExternalFilesDir(null);
        File jsonFile = new File(rootFolder, "info.json");
        FileWriter writer = new FileWriter(jsonFile);
        writer.write(jsonString);
        writer.close();
        //or IOUtils.closeQuietly(writer);
    }
    boolean checkPermission() {

        int fineLocation = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int courseLocation = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_COARSE_LOCATION);
        return fineLocation == PackageManager.PERMISSION_GRANTED && courseLocation == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        int PERMISSION_REQUEST_CODE = 200;
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
    }
    private void insertJson(MainJsonClass jsonClass){
        LiveDataReactiveStreams.fromPublisher( Repository.getInstance(this).insetJson(jsonClass)
       ).observe(this, new Observer<Long>() {
         @Override
         public void onChanged(Long aLong) {
             if (aLong == null){
                 Log.d(TAG, "onChanged: it was null");
             }
             else{
                 Log.d(TAG, "onChanged: data was inserted successfully");
             }
         }
     });
    }
}
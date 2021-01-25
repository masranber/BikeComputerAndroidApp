package com.masranber.bikecomputer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.masranber.bikecomputer.ui.main.BikeComputerFragment;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BikeComputerFragment.newInstance())
                    .commitNow();
        }

        checkAndRequestPermissions();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static final String[] REQUIRED_PERMISSION_LIST = new String[] {
            //Manifest.permission.VIBRATE, // Gimbal rotation
            //Manifest.permission.INTERNET, // API requests
            //Manifest.permission.ACCESS_WIFI_STATE, // WIFI connected products
            //Manifest.permission.ACCESS_COARSE_LOCATION, // Maps
            //Manifest.permission.ACCESS_NETWORK_STATE, // WIFI connected products
            Manifest.permission.ACCESS_FINE_LOCATION, // Maps
            //Manifest.permission.CHANGE_WIFI_STATE, // Changing between WIFI and USB connection
            //Manifest.permission.WRITE_EXTERNAL_STORAGE, // Log files
            Manifest.permission.BLUETOOTH, // Bluetooth connected products
            Manifest.permission.BLUETOOTH_ADMIN, // Bluetooth connected products
            //Manifest.permission.READ_EXTERNAL_STORAGE, // Log files
            //Manifest.permission.READ_PHONE_STATE, // Device UUID accessed upon registration
            //Manifest.permission.RECORD_AUDIO // Speaker accessory
    };
    private static final int REQUEST_PERMISSION_CODE = 12345;
    private List<String> missingPermission = new ArrayList<>();

    public void checkAndRequestPermissions() {
        // Check for permissions
        for (String eachPermission : REQUIRED_PERMISSION_LIST) {
            if (ContextCompat.checkSelfPermission(this, eachPermission) != PackageManager.PERMISSION_GRANTED) {
                Log.i("PERMISSION",eachPermission+ " permission missing");
                missingPermission.add(eachPermission);
            }
        }
        // Request for missing permissions
        if (missingPermission.isEmpty()) {
            Log.i("PERMISSION",REQUIRED_PERMISSION_LIST.length+" permissions already granted");
        } else {
            Log.i("PERMISSION", "Requesting "+missingPermission.size()+" permissions");
            ActivityCompat.requestPermissions(this,
                    missingPermission.toArray(new String[missingPermission.size()]),
                    REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Check for granted permission and remove from missing list
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    missingPermission.remove(permissions[i]);
                }
            }
        }
        // If there is enough permission, we will start the registration
        if (missingPermission.isEmpty()) {
            Log.i("PERMISSION", "Permissions granted");
        } else {
            Log.i("PERMISSION", "Permissions denied");
            Toast.makeText(getApplicationContext(), "Missing permissions!", Toast.LENGTH_LONG).show();
        }
    }
}
package com.example.bringeraj.myfusedlocationprovider;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_LOCATION_CHECK_SETTINGS = 100;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //initialise the fused location client
        initialiseLocationResultCallback(); //initialise the location callback
        createLocationRequest(); //create the required location request
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startLocationUpdates(); //on opening of app start location updates
        requiredLocationRequest(); //check for required location settings then start calling location updates
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000); //1 second
        mLocationRequest.setFastestInterval(5000); //5 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); //High accuracy uses all sensors , gps and cell towers to fetch user location accurately - consume more battery
    }

    private void requiredLocationRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest); //this contains our required location settings request
        SettingsClient client = LocationServices.getSettingsClient(this); //Building up the settings client to access our devices location settings
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build()); //task to compare the device location settings with required location settings and handled the result in following callbacks
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //check for permission
                    //Permission Required
                } else {
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null /* Looper */);
                }
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this, REQUEST_LOCATION_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void initialiseLocationResultCallback() {
        mLocationCallback = new LocationCallback() { //fused location callback to get the result of updated location
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d(TAG, "onSuccess: Location : " + locationResult);
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    Toast.makeText(MainActivity.this, "Location : " + location, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    private void requestCurrentLocationOnce() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) { //check for permission
            //Permission Required
            Toast.makeText(this, "Location Permission Required", Toast.LENGTH_SHORT).show();
        } else { //If Permission Granted then request one time current location
            mFusedLocationClient.getLastLocation() //get last location - it contains the latest fetched location
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            Log.d(TAG, "onSuccess: Location : " + location);
                            if (location != null) {
                                Toast.makeText(MainActivity.this, "Location : " + location, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}

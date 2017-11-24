package com.android.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String LOG_PREFIX = "antlap";
    private static final String LOG_TAG = LOG_PREFIX + MainActivity.class.getSimpleName();

    private LocationManager locationManager;
    private boolean providerEnabled = false;

    @BindView(R.id.button) Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkPermission();
    }

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    /**
     * Check Permissions for ACCESS_FINE_LOCATION, SEND_SMS,...
     **/
    private void checkPermission() {
        List<String> permissionsReq = new ArrayList();
        int hasLocationPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsReq.add(Manifest.permission.ACCESS_FINE_LOCATION );
            Log.d(LOG_TAG, "checkPermission: Location Permission Denied!");
        }
/*
        int hasSMSPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsReq.add(Manifest.permission.SEND_SMS);
            Log.d(LOG_TAG, "checkPermission: need send sms permission");
        }
        ... more permissions request
*/
        if(permissionsReq.isEmpty()) { // Permission already granted, no runtime permissions needed!
            Log.d(LOG_TAG, "checkPermission: Location permission already granted, current SDK API Level = " + Build.VERSION.SDK_INT);
        } else {
            // Ask user for the permissions if we're on SDK M or later...
            // will be called onRequestPermissionsResult for responses
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this,
                        permissionsReq.toArray(new String[permissionsReq.size()]),
                        REQUEST_CODE_ASK_PERMISSIONS);
                Log.d(LOG_TAG, "checkPermission: Ask user for Location Permission");
            }
        }
    }

    /**
     * Callback received when a permissions request has been completed, only on SDK M or later...
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult: requestCode: " + requestCode);
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int i = 0; i < grantResults.length; i++) { // If request is cancelled, the result arrays are empty.
                    if( grantResults[i] == PackageManager.PERMISSION_GRANTED ) {
                        showMessage("Location permission granted, starting updates...");
                        Log.d(LOG_TAG, "onRequestPermissionsResult: Permission Granted = " + permissions[i] );
                    } else {  // The permission was denied, so we can show a message why we can't run the app
                        Toast.makeText(this, "Sorry, need location permission to start", Toast.LENGTH_LONG).show();
                        showMessage("Sorry, need location permission to start!");
                        Log.d(LOG_TAG, "onRequestPermissionsResult: Permission Denied = " + permissions[i] );
                        //finish();
                    }
                }
                break;
            } // Other permissions could go down here
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        List<String> providers = locationManager.getAllProviders();
        if(providerEnabled){
            // Already updating
            return;
        }

        // check for ACCESS_FINE_LOCATION permission - Called before onResume
        int checkPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "requestLocationUpdates: permission granted");
            for (String provider : providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(provider, 0, 0, this);
//                    locationManager.requestLocationUpdates(0, 0, createFineCriteria(), this, Looper.getMainLooper());
                    providerEnabled = true;
                    showMessage("Please switch GPS on");
                    Log.d(LOG_TAG, "requestLocationUpdates: active provider = " + provider);
                }
            }
            if (!providerEnabled) {
                Log.d(LOG_TAG, "requestLocationUpdates: No location providers enabled.");
            }
        } else {
            showMessage("Need location permission to start tracking...");
            Log.d(LOG_TAG, "requestLocationUpdates: Location Permission Denied.");
        }
    }

    /** this criteria needs high accuracy, high power, and cost */
    public static Criteria createFineCriteria() {
        Criteria c = new Criteria();
        c.setAccuracy(Criteria.ACCURACY_FINE);
        c.setAltitudeRequired(true);
        c.setBearingRequired(false);
        c.setSpeedRequired(false);
        c.setCostAllowed(true);
        c.setPowerRequirement(Criteria.POWER_HIGH);
        return c;
    }

    @Override
    protected void onPause() {
        locationManager.removeUpdates(this);
        providerEnabled = false;
        super.onPause();
    }

    private void locationUpdated(Location location) {
        List<Location> locations = ((MainApplication) getApplication()).getAllReceivedLocations();
        locations.add(location);

        String message = String.format(Locale.getDefault(),
                "Total location updates: %d.\n\nYou are now at: %.2f, %.2f, altitude: %.2f",
                locations.size(),
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude());
        showMessage(message);
    }

    public void onLocationChanged(Location location){
        // count and show this new location
        locationUpdated(location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
//        Log.d(LOG_TAG, "onStatusChanged: provider = " + provider + ", status = " + status);
    }

    public void onProviderEnabled(String provider) {
        Log.d(LOG_TAG, "onProviderEnabled: provider = " + provider);
    }

    public void onProviderDisabled(String provider) {
        Log.d(LOG_TAG, "onProviderDisabled: provider = " + provider);
    }

    private void showMessage(String msg){
        MainActivityFragment fragment = (MainActivityFragment)getSupportFragmentManager().findFragmentById(R.id.fragment);
        fragment.showMessage(msg);
    }

}

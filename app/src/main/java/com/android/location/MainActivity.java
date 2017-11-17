package com.android.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
        String[] permissionsReq = new String[1];

        int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION );
        if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
            permissionsReq[0] = Manifest.permission.ACCESS_FINE_LOCATION ;
            showMessage("Need location permission");
            Log.d(LOG_TAG, "checkPermission: need location permission");
        }
/*
        int hasSMSPermission = checkSelfPermission( Manifest.permission.SEND_SMS );
        if( hasSMSPermission != PackageManager.PERMISSION_GRANTED ) {
            permissionsReq[1] = Manifest.permission.SEND_SMS );
            showMessage("Need send sms permission");
            Log.d(LOG_TAG, "checkPermission: need send sms permission");
        }
        ... more permissions request
*/
        if( permissionsReq.length > 0 ) {
            // Ask user for the permissions if we're on SDK M or later...
            // will be called onRequestPermissionsResult for responses
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, permissionsReq, REQUEST_CODE_ASK_PERMISSIONS);
            }
        } else { // Permission already granted, no runtime permissions needed!
            showMessage("Location permission granted");
        }
    }

    /**
     * Callback received when a permissions request has been completed.
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
        Log.d(LOG_TAG, "requestLocationUpdates: permission granted = " + (PackageManager.PERMISSION_GRANTED == checkPermission));

        if (checkPermission == PackageManager.PERMISSION_GRANTED) {
            for (String provider : providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(provider, 0, 0, this);
                    providerEnabled = true;
                }
            }
            if (!providerEnabled) {
                Log.d(LOG_TAG, "requestLocationUpdates: No location providers enabled.");
            }
        } else {
            Log.d(LOG_TAG, "requestLocationUpdates: Need location permission to start...");
        }
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
                "Total location updates: %d.\n\nYou are now at: %.2f, %.2f",
                locations.size(),
                location.getLatitude(),
                location.getLongitude());
        findMainFragment().showMessage(message);
    }

    public void onLocationChanged(Location location) {
        locationUpdated(location);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Don't care
    }

    public void onProviderEnabled(String provider) {
        // Don't care
    }

    public void onProviderDisabled(String provider) {
        // Don't care
    }

    private void showMessage(String msg){
        findMainFragment().showMessage(msg);
        Log.d(LOG_TAG, "showMessage: " + msg);
    }

    private MainActivityFragment findMainFragment() {
        return (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }
}

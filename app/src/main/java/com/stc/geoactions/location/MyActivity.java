package com.stc.geoactions.location;

import android.arch.lifecycle.LifecycleActivity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.stc.geoactions.R;

public class MyActivity extends LifecycleActivity implements OnMapReadyCallback, MyLocationListener.Callback {
    private static final String TAG = "MyActivity";
    private static final int REQ_PERMISSION = 24;
    private MyLocationListener myLocationListener;
    private GoogleMap googleMap;
    private Location location;
    private TextView textLocation;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //error
        textLocation=(TextView) findViewById(R.id.textLocation);
        floatingActionButton=(FloatingActionButton)findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            if(!checkPermission()) askPermission();
            else myLocationListener.enable();
        });
        textLocation.setText("no location");
        myLocationListener = new MyLocationListener(this, getLifecycle(), this);
    }

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    myLocationListener.enable();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady ");
        this.googleMap=googleMap;
        if(location!=null)googleMap.moveCamera(CameraUpdateFactory.newLatLng(
                new LatLng(location.getLatitude(),location.getLongitude())));
    }

    @Override
    public void setLocation(Location location) {
        Log.d(TAG, "setLocation: "+location);
        this.location=location;
        if(googleMap!=null){

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(),location.getLongitude()),
                    googleMap.getCameraPosition().zoom<10 ? 12 : googleMap.getCameraPosition().zoom));
        }
        MarkerOptions markerOptions =new MarkerOptions();
        markerOptions.position(new LatLng(location.getLatitude(),location.getLongitude()));
        googleMap.addMarker(markerOptions);
        textLocation.setText(location.toString());
    }
}

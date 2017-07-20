package com.stc.geoactions.location;

import android.arch.lifecycle.LifecycleActivity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.stc.geoactions.R;

public class MyActivity extends LifecycleActivity implements OnMapReadyCallback, MyLocationListener.Callback {
    private static final String TAG = "MyActivity";
    private static final int REQ_PERMISSION = 24;
    private MyLocationListener myLocationListener;
    private GoogleMap googleMap;
    private Location location;
    private TextView textLocation;
    private FloatingActionButton floatingActionButton;
    public static final String PERIOD_ALL = "PERIOD_ALL";
    public static final String PERIOD_TODAY = "PERIOD_TODAY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        myLocationListener = new MyLocationListener(this, getLifecycle(), this);
        textLocation=(TextView) findViewById(R.id.textLocation);
        floatingActionButton=(FloatingActionButton)findViewById(R.id.floatingActionButton);
        floatingActionButton.setOnClickListener(v -> {
            if(!checkPermission()) askPermission();
            else myLocationListener.enable();
        });
        textLocation.setText("no location");
        textLocation.setOnClickListener(v -> showDialogSelectPeriod());

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
        setStyle();
    }
    private void setStyle(){
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.retro_map));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
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
        updateStatus(location);
    }

    private void updateStatus(Location location) {
        String status=myLocationListener.getAddress(location)+
                "\nacc: "+location.getAccuracy()+
                "\nspeed: "+location.getSpeed();
        textLocation.setText(status);
        Log.d(TAG, "updateStatus: "+status);
    }

    public void showHistoy(String period){
        if(googleMap!=null){
            googleMap.clear();
            PolylineOptions polylineOptions=myLocationListener.getHistory(period);
            polylineOptions.color(Color.RED);
            Log.d(TAG, "showHistoy: pointscount="+polylineOptions.getPoints().size());
            if(polylineOptions.getPoints().size()==0){
                Log.e(TAG, "showHistoy: empty" );
                Toast.makeText(this, "No data for this period", Toast.LENGTH_SHORT).show();
                return;
            }
            googleMap.addPolyline(polylineOptions);
            LatLngBounds.Builder builder=new LatLngBounds.Builder();
            for(LatLng latLng : polylineOptions.getPoints()){
                builder.include(latLng);
            }
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
        }else {
            Log.e(TAG, "showHistoy: error" );
            Toast.makeText(this, "Map error", Toast.LENGTH_SHORT).show();
        }
    }
    public void showDialogSelectPeriod(){
        AlertDialog.Builder aBuilder=new AlertDialog.Builder(this);
        aBuilder.setPositiveButton(PERIOD_ALL, (dialog, which) -> showHistoy(PERIOD_ALL))
                .setNegativeButton(PERIOD_TODAY, (dialog, which) -> showHistoy(PERIOD_TODAY))
                .setNeutralButton("clear map", (dialog, which) -> {
                    dialog.dismiss();
                    googleMap.clear();
                });
        aBuilder.setMessage("Select action:");
        aBuilder.create().show();
    }
}

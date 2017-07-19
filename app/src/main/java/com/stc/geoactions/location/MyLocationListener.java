package com.stc.geoactions.location;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import static android.arch.lifecycle.Lifecycle.State.STARTED;

/**
 * Created by artem on 7/19/17.
 */

    public class MyLocationListener implements
        LifecycleObserver,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final String TAG = "MyLocationListener";
    private final Context mContext;
    private final Callback callback;
    Lifecycle lifecycle;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;


    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;

    public MyLocationListener(Context context, Lifecycle lifecycle, Callback callback) {
        this.mContext=context;
        this.lifecycle=lifecycle;
        this.callback=callback;
        createGoogleApi();
    }
    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if ( googleApiClient == null ) {
            googleApiClient = new GoogleApiClient.Builder( mContext )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }


    public void enable() {
        if (lifecycle.getCurrentState().isAtLeast(STARTED)) {
            if(!googleApiClient.isConnected())googleApiClient.connect();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void stop() {
        if(!googleApiClient.isConnected())googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if ( lastLocation != null ) {
            Log.i(TAG, "LasKnown location. " +
                    "Long: " + lastLocation.getLongitude() +
                    " | Lat: " + lastLocation.getLatitude());
            callback.setLocation(lastLocation);
            startLocationUpdates();
        } else {
            Log.w(TAG, "No location retrieved yet");
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: "+connectionResult.getErrorMessage() );
        callback.setLocation(lastLocation);
    }

    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
         LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        callback.setLocation(lastLocation);
    }



    public interface Callback {
        void setLocation(Location location);
    }
}

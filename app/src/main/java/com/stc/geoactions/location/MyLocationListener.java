package com.stc.geoactions.location;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.stc.geoactions.data.Record;
import com.stc.geoactions.db.AppDatabase;
import com.stc.geoactions.db.RecordDao;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.arch.lifecycle.Lifecycle.State.STARTED;
import static com.stc.geoactions.location.MyActivity.PERIOD_ALL;
import static com.stc.geoactions.location.MyActivity.PERIOD_TODAY;

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
    private final int UPDATE_INTERVAL =  60000;
    private final int FASTEST_INTERVAL = 10000;
    private RecordDao recordDao;
    Geocoder geocoder;


    public MyLocationListener(Context context, Lifecycle lifecycle, Callback callback) {
        this.mContext=context;
        this.lifecycle=lifecycle;
        this.callback=callback;
        createGoogleApi();
        AppDatabase db = AppDatabase.getAppDatabase(context);
        recordDao=db.recordDao();
        geocoder=new Geocoder(context, Locale.getDefault());
        lifecycle.addObserver(this);
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


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void start() {
        Log.w(TAG, "start: "+lifecycle.getCurrentState() );
        if (lifecycle.getCurrentState().isAtLeast(STARTED)) {
            if(!googleApiClient.isConnected()){
                googleApiClient.connect();
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void stopLs() {
        Log.w(TAG, "stop: "+lifecycle.getCurrentState() );
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    void enable() {
        Log.w(TAG, "enable: "+lifecycle.getCurrentState() );
        if(googleApiClient.isConnected()){
            startLocationUpdates();
        }else {
            Toast.makeText(mContext, "not connected yet", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "enable: not connected" );
            googleApiClient.connect();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    void stop() {
        Log.w(TAG, "stop: connected: "+googleApiClient.isConnected());
        stopLocationUpdates();
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
        Log.w(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: "+connectionResult.getErrorMessage() );
        callback.setLocation(lastLocation);
    }

    private void startLocationUpdates(){
        Log.w(TAG, "startLocationUpdates()");
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
         LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
    private void stopLocationUpdates(){
        Log.w(TAG, "stopLocationUpdates()");
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"] \ndist: "+lastLocation.distanceTo(location));
        lastLocation = location;
        callback.setLocation(lastLocation);
        if(lastLocation!=null /*&& lastLocation.distanceTo(location)>5.0f*/) {
            Log.d(TAG, "onLocationChanged: insert");
            recordDao.insertItem(new Record(lastLocation));
        }
    }



    public interface Callback {
        void setLocation(Location location);
    }


    public PolylineOptions getHistory(String period){
        List<Record> records=recordDao.loadAllRecords();
        Log.d(TAG, "getHistory: "+records.size());
        records.sort(new Comparator<Record>() {
            @Override
            public int compare(Record o1, Record o2) {
                if(o1.getTimestamp()==o2.getTimestamp())return 0;
                else if(o1.getTimestamp()>o2.getTimestamp())return 1;
                else return -1;
            }
        });
        PolylineOptions polylineOptions=new PolylineOptions();
        for(Record record : records){
            if((period.equals(PERIOD_TODAY) && DateUtils.isToday(record.getTimestamp())) ||
                    period.equals(PERIOD_ALL)) {
                polylineOptions.add(record.getLatLng());
                Log.d(TAG, "getHistory: add item "+record.toString());
            }
        }
        return polylineOptions;
    }
    public String getAddress(Location location){
        String result="";
        List<Address> addresses=null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1 );
        } catch (IOException e) {
            e.printStackTrace();
            return "error getting address";
        }
        if(addresses==null || addresses.isEmpty()) return "no address";
        else {
            Address address=addresses.get(0);
            return address.getAddressLine(0)+"";
        }
    }
}

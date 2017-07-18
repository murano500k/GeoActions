package com.stc.geoactions;

import android.app.PendingIntent;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MyObserver implements LifecycleObserver {

    private static final String GEOFENCE_WRK = "GEOFENCE_WRK";
    private Geofence mGeofence;

    private List<Geofence> mGeofenceList;
    private Context mContext;

    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;

    public MyObserver(Context mContext, Location entry) {
        this.mContext = mContext;
        mGeofenceList=new ArrayList<>();
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(GEOFENCE_WRK)

                .setCircularRegion(
                        entry.getLatitude(),
                        entry.getLongitude(),
                        entry.getAccuracy()
                )
                .setExpirationDuration(60000L)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        if(mGeofencingClient ==null)mGeofencingClient= LocationServices.getGeofencingClient(mContext);
        mGeofencingClient.addGeofences(getGeofencingRequest(),getGeofencePendingIntent());

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}

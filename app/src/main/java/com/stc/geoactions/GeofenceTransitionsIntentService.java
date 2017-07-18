package com.stc.geoactions;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stc.geoactions.data.DbEntry;

/**
 * Created by artem on 7/13/17.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    private static final String TAG = "GeofenceTransitionsInte";
    private SharedPreferences mPrefs;
    DatabaseReference ref;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPrefs=PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String id= mPrefs.getString(Constants.MY_ID, null);
        if(id==null) throw new NullPointerException("no id");
        ref= FirebaseDatabase.getInstance().getReference(id);
        return super.onStartCommand(intent, flags, startId);
    }
    public GeofenceTransitionsIntentService() {
        super(TAG);

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "error");
            return;
        }else Log.w(TAG, "onHandleIntent: success");
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        addEntryToDb(geofenceTransition);


        // Get the transition type.

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            addEntryToDb(geofenceTransition);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type)+
                    geofenceTransition);
        }
    }
    private void addEntryToDb(int geofenceTransition){

        long timestamp=System.currentTimeMillis();
        String key= ref.push().getKey();
        ref.child(key).setValue(new DbEntry(key,timestamp,geofenceTransition));
    }

}

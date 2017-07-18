package com.stc.geoactions.data;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by artem on 7/17/17.
 */
@IgnoreExtraProperties
public class DbEntry {
    public String key;
    public long timestamp;
    public int isInside;

    public DbEntry(String key, long timestamp, int geofenceTransition) {
        this.timestamp = timestamp;
        this.isInside = geofenceTransition;
        this.key = key;
    }

    public DbEntry() {
    }
}

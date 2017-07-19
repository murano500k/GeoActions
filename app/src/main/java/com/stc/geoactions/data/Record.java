package com.stc.geoactions.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by artem on 7/19/17.
 */
@Entity(tableName = "records")
public class Record {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "lat")
    private double lat;

    @ColumnInfo(name = "lon")
    private double lon;

    @ColumnInfo(name = "speed")
    private double speed;

    @ColumnInfo(name = "acc")
    private double acc;

    public LatLng getLatLng(){
        return new LatLng(lat, lon);
    }
    public Record() {

    }

    public Record(Location location) {
        this.lat=location.getLatitude();
        this.lon=location.getLongitude();
        this.speed=location.getSpeed();
        this.acc=location.getAccuracy();
        this.timestamp=System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAcc() {
        return acc;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setAcc(double acc) {
        this.acc = acc;
    }
}
package com.stc.geoactions.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.stc.geoactions.data.Record;

import java.util.List;


@Dao
public interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertItem(Record position);

    @Query("SELECT * FROM records")
    public List<Record> loadAllRecords();
/*
    @Query("SELECT * FROM user WHERE timestamp AFTER :from")
    public List<Record> loadAllRecordsAfter(long from);*/

}
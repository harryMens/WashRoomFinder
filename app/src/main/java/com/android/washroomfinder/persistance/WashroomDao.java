package com.android.washroomfinder.persistance;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.washroomfinder.MainJsonClass;
import com.android.washroomfinder.WashRoom;

import java.util.List;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface WashroomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertTracks(MainJsonClass jsonClass);

    @Query("SELECT * FROM mainjsonclass")
    LiveData<List<MainJsonClass>> getJson();
}

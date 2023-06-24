package com.android.washroomfinder.persistance;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.android.washroomfinder.MainJsonClass;
import com.android.washroomfinder.WashRoom;

@Database(entities = MainJsonClass.class,version = 1)
//@TypeConverters({ConvertingDaily.class, ConvertingHourly.class})
//@TypeConverters({Converter.class, DoubleConverter.class})
public abstract class WashRoomDataBase extends RoomDatabase {
    public static final String DATABASE_NAME="name_database";

    private static WashRoomDataBase instance;
    public static WashRoomDataBase getInstance(final Context context){
        if (instance == null) {
            return Room.databaseBuilder(
                            context.getApplicationContext(),
                            WashRoomDataBase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
    public abstract WashroomDao getWashRoomDao();
}
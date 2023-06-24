package com.android.washroomfinder.persistance;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.android.washroomfinder.MainJsonClass;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class Repository {
    private static Repository instance;
    private WashRoomDataBase dataBase;

    public Repository(Context context) {

        if (dataBase == null) {
            dataBase = WashRoomDataBase.getInstance(context);
        }

    }

    public static Repository getInstance(final Context context){
        if (instance == null){
            instance = new Repository(context);
        }
        return instance;
    }

    public LiveData<List<MainJsonClass>> getSavedJson() {
        return dataBase.getWashRoomDao().getJson();
    }
    public Flowable<Long> insetJson(MainJsonClass jsonClass){
        return dataBase.getWashRoomDao().insertTracks(jsonClass)
                .toFlowable()
                .subscribeOn(Schedulers.io());
    }

}

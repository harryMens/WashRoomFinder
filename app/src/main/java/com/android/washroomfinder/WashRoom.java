package com.android.washroomfinder;

import androidx.room.Entity;

import java.util.List;

public class WashRoom {
    List<MainJsonClass> Toiletten;

    public List<MainJsonClass> getToiletten() {
        return Toiletten;
    }

    public void setToiletten(List<MainJsonClass> toiletten) {
        Toiletten = toiletten;
    }
}

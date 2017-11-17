package com.android.location;

import android.app.Application;
import android.location.Location;

import java.util.LinkedList;
import java.util.List;

public class MainApplication extends Application {

    private final List<Location> allReceivedLocations = new LinkedList<>();

    public List<Location> getAllReceivedLocations() {
        return allReceivedLocations;
    }
}

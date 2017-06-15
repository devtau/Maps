package com.devtau.maps;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Tau on 12.05.2017.
 */

public interface MapHelper extends
        OnMapReadyCallback,
        LocationListener {

    void handleGeoData();
    void goToLocation(LatLng location);
    void goToMyLocation();
    void goToDestination();
    void subscribeToLocationChange();
    void unsubscribeFromLocationChange();
}

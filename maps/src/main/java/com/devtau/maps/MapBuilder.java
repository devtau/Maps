package com.devtau.maps;

import android.support.v4.app.FragmentManager;
import java.util.ArrayList;

public class MapBuilder {

    private FragmentManager fragmentManager;
    private int containerId;
    private ArrayList<Place> places;
    private boolean placesAreVisible;
    private ArrayList<MapPolygon> mapPolygons;
    private boolean mapPolygonsAreVisible;
    private int markerResId;
    private boolean withBalloon;
    private String cityName;


    public MapBuilder(FragmentManager fragmentManager, int containerId, String cityName) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        this.cityName = cityName;
    }

    public MapBuilder withPlaces(ArrayList<Place> places, boolean visible) {
        this.places = places;
        placesAreVisible = visible;
        return this;
    }

    public MapBuilder withPolygons(ArrayList<MapPolygon> mapPolygons, boolean visible) {
        this.mapPolygons = mapPolygons;
        mapPolygonsAreVisible = visible;
        return this;
    }

    public MapBuilder withMarker(int markerResId) {
        this.markerResId = markerResId;
        return this;
    }

    public MapBuilder withBalloon(BalloonListener listener) {
        withBalloon = true;
        return this;
    }

    public void build() {
        MapFragment mapFragment = MapFragment.newInstance(places, placesAreVisible, mapPolygons, mapPolygonsAreVisible, markerResId, withBalloon, cityName);
        fragmentManager.beginTransaction().replace(containerId, mapFragment, MapFragment.FRAGMENT_TAG).commit();
    }
}

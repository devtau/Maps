package com.devtau.maps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import org.jetbrains.annotations.Contract;
import java.util.ArrayList;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapHelperImpl implements MapHelper {

    private static final String LOG_TAG = "MapHelperImpl";
    private static final int ZOOM = 14;//0,1,2 - максимально высоко, 5 - страна, 10 - город, 15 - квартал, 18 - дом, 20 - максимально близко
    private static final int PADDING = 80;

    private Fragment fragment;
    private GoogleMap map;
    private LatLng currentLocation;
    private LatLng destination;
    private String destinationTitle;
    private Marker currLocationMarker;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient fusedLocationClient;
    private PermissionHelper permissionHelper;

    private boolean configurationChanged;
    private ArrayList<Place> places;
    private boolean placesAreVisible;
    private ArrayList<MapPolygon> mapPolygons;
    private boolean polygonsAreVisible;
    private ArrayList<Marker> allocatedMarkers;
    private ArrayList<Polygon> allocatedPolygons;
    private int markerResId;
    private int defaultMarkerColorId;
    private BalloonListener listener;


//    MapHelperImpl(boolean configurationChanged, Fragment fragment) {
//        this(new LatLng(28.460362, -16.250817), "Marker in Tenerife", configurationChanged, fragment);
//    }
//
//    MapHelperImpl(@NonNull LatLng destination, @NonNull String destinationTitle, boolean configurationChanged, Fragment fragment) {
//        this.destination = destination;
//        this.destinationTitle = destinationTitle;
//        this.fragment = fragment;
//        this.configurationChanged = configurationChanged;
//        if (fragment.getContext() != null) {
//            googleApiClient = buildGoogleApiClient(fragment.getContext());
//            fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragment.getContext());
//        }
//        permissionHelper = new PermissionHelperImpl();
//    }


    MapHelperImpl(ArrayList<Place> places, boolean placesAreVisible,
                  ArrayList<MapPolygon> mapPolygons, boolean polygonsAreVisible,
                  boolean configurationChanged, @NonNull Fragment fragment,
                  int markerResId, BalloonListener listener) {
        this(places, placesAreVisible, mapPolygons, polygonsAreVisible, configurationChanged, fragment, markerResId, 0, listener);
    }

    private MapHelperImpl(ArrayList<Place> places, boolean placesAreVisible,
                          ArrayList<MapPolygon> mapPolygons, boolean polygonsAreVisible,
                          boolean configurationChanged, @NonNull Fragment fragment,
                          int markerResId, int defaultMarkerColorId, BalloonListener listener) {
        permissionHelper = new PermissionHelperImpl();
        this.places = places;
        this.placesAreVisible = placesAreVisible;
        this.mapPolygons = mapPolygons;
        this.polygonsAreVisible = polygonsAreVisible;
        this.fragment = fragment;
        this.configurationChanged = configurationChanged;
        if (fragment.getContext() != null) {
            googleApiClient = buildGoogleApiClient(fragment.getContext());
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(fragment.getContext());
        }
        this.markerResId = markerResId;
        this.defaultMarkerColorId = defaultMarkerColorId;
        this.listener = listener;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (fragment.getContext() == null) return;
        map = googleMap;
//        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(true);
        drawDestination();
        showPlaces(placesAreVisible);
        showPolygons(polygonsAreVisible);
        processBalloonIfPossible();
        map.setOnCameraIdleListener(() -> listener.onCameraMoved(getVisiblePlaces()));
        if (!configurationChanged) centerCameraIfPossible(false);
    }

    private void drawDestination() {
        if (destination == null) return;
        map.addMarker(new MarkerOptions().position(destination).title(destinationTitle));
    }

    private void processBalloonIfPossible() {
        if (listener != null) {
            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    return listener.bindBalloon((Place) marker.getTag());
                }
            });
            map.setOnInfoWindowClickListener(marker -> listener.onBalloonClicked((Place) marker.getTag()));
        }
    }

    private ArrayList<Place> getVisiblePlaces() {
        LatLngBounds latLngBounds = map.getProjection().getVisibleRegion().latLngBounds;
        double lowLat;
        double highLat;
        double lowLng;
        double highLng;

        if (latLngBounds.northeast.latitude < latLngBounds.southwest.latitude) {
            lowLat = latLngBounds.northeast.latitude;
            highLat = latLngBounds.southwest.latitude;
        } else {
            lowLat = latLngBounds.southwest.latitude;
            highLat = latLngBounds.northeast.latitude;
        }

        if (latLngBounds.northeast.longitude < latLngBounds.southwest.longitude) {
            lowLng = latLngBounds.northeast.longitude;
            highLng = latLngBounds.southwest.longitude;
        } else {
            lowLng = latLngBounds.southwest.longitude;
            highLng = latLngBounds.northeast.longitude;
        }

        ArrayList<Place> visiblePlaces = new ArrayList<>();
        for (Place next : places)
            if (lowLat <= next.getLat()  && next.getLat() <= highLat
                    && lowLng <= next.getLon()  && next.getLon() <= highLng)
                visiblePlaces.add(next);
        return visiblePlaces;
    }


    @Override
    public void onLocationChanged(Location location) {
        processMyLocation(location);
    }


    @Override
    public void handleGeoData() {
        if (fragment.getActivity() == null) return;
        if (!permissionHelper.checkGPSPermission(fragment.getContext())) {
            permissionHelper.requestGPSPermission(fragment);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(fragment.getActivity(), location -> {
            if (location != null) processMyLocation(location);
        });

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000) //2 seconds
                .setFastestInterval(1000) //1 seconds
//                .setSmallestDisplacement(0.1F) //1/10 meter
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) processMyLocation(location);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, fragment.getActivity().getMainLooper());
    }

    @Override
    public void goToLocation(LatLng location) {
        if (location != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location)
                    .zoom(ZOOM)
                    .bearing(0)//азимут. 0 - север, 90 - восток, 180 - юг, 270 - запад
                    .tilt(0)//0-90 условный угол наклона камеры к плоскости карты, где 0 - это взгляд перпендикулярно вниз, а 90 - около 45 градусов к плоскости
                    .build();
            Log.d(LOG_TAG, "currentZoom=" + map.getCameraPosition().zoom);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            String gpsUnavailableMsg;
            if (permissionHelper.checkGPSPermission(fragment.getContext())) {
                gpsUnavailableMsg = fragment.getString(R.string.geo_data_unavailable);
            } else {
                gpsUnavailableMsg = fragment.getString(R.string.permission_cancelled_msg_gps);
            }
            Toast.makeText(fragment.getContext(), gpsUnavailableMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void goToMyLocation() {
        goToLocation(currentLocation);
    }

    @Override
    public void centerCameraIfPossible(boolean animate) {
        if (destination != null) {
            if (animate) map.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, ZOOM));
            else map.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, ZOOM));
        } else if (AppUtils.notEmpty(places)) {
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for (Place next : places) bounds.include(next.getLatLng());
            try {
                if (animate) map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), PADDING));
                else map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), PADDING));
            } catch (IllegalStateException e) {
                map.setOnCameraIdleListener(() -> {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), PADDING));
                    map.setOnCameraIdleListener(() -> listener.onCameraMoved(getVisiblePlaces()));
                });
            }
        }
    }

    @Override
    public void subscribeToLocationChange() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void unsubscribeFromLocationChange() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void showPlaces(boolean show) {
        if (show && allocatedMarkers == null) drawPlaces();
        else if (allocatedMarkers != null) {
            for (Marker next : allocatedMarkers) next.remove();
            allocatedMarkers = null;
        }
    }

    @Override
    public void showPolygons(boolean show) {
        if (show && allocatedPolygons == null) drawPolygons();
        else if (allocatedPolygons != null) {
            for (Polygon next : allocatedPolygons) {
                next.remove();
            }
            allocatedPolygons = null;
        }
    }


    @NonNull
    private GoogleApiClient buildGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override public void onConnected(@Nullable Bundle bundle) {
                        handleGeoData();
                    }
                    @Override public void onConnectionSuspended(int i) { }
                })
                .addOnConnectionFailedListener(connectionResult -> Log.e(LOG_TAG, "ConnectionFailed"))
                .addApi(LocationServices.API)
                .build();
    }


    private void drawPlaces() {
        if (AppUtils.isEmpty(places) || fragment.getContext() == null) return;
        allocatedMarkers = new ArrayList<>();
        if (defaultMarkerColorId != 0) {
            for (Place next : places) addDefaultMarkerToMap(next, defaultMarkerColorId);
        } else if (markerResId != 0) {
            for (Place next : places) addCustomMarkerToMap(next, markerResId);
        }
    }

    private void drawPolygons() {
        if (AppUtils.isEmpty(mapPolygons) || fragment.getContext() == null || fragment.getView() == null) return;
        PolygonOptions polygonOptions;
        allocatedPolygons = new ArrayList<>();
        for (MapPolygon nextMapPolygon : mapPolygons) {
            int polygonColorInt = Color.parseColor(nextMapPolygon.getColor());
            polygonOptions = new PolygonOptions()
                    .strokeColor(polygonColorInt)
                    .fillColor(polygonColorInt)
                    .clickable(true);
            for (LatLng nextLatLng : nextMapPolygon.getBounds()) polygonOptions.add(nextLatLng);
            Polygon polygon = map.addPolygon(polygonOptions);
            polygon.setTag(nextMapPolygon);
            allocatedPolygons.add(polygon);

            map.setOnPolygonClickListener(clickedPolygon -> {
                if (clickedPolygon.getTag() == null) return;
                String title = ((MapPolygon) clickedPolygon.getTag()).getTitle();
//                Toast.makeText(fragment.getContext(), title, Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Snackbar.make(fragment.getView(), title, Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction(fragment.getString(android.R.string.ok), v -> snackbar.dismiss()).show();
            });
        }
    }

    private void processMyLocation(Location location) {
        if (location == null || !googleApiClient.isConnected()) return;
        if (currLocationMarker != null) currLocationMarker.remove();
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(currentLocation)
                .title(fragment.getString(R.string.current_location))
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_pin));
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currLocationMarker = map.addMarker(markerOptions);
    }

    private void addDefaultMarkerToMap(Place place, int defaultMarkerColorId) {
        if (fragment.getContext() == null) return;
        int color = ContextCompat.getColor(fragment.getContext(), defaultMarkerColorId);
        Bitmap markerBitmap = getMarkerIconFromDrawable(ContextCompat.getDrawable(fragment.getContext(), R.drawable.map_pin), color);
        addPlaceToMap(place, markerBitmap);
    }

    private void addCustomMarkerToMap(Place place, int markerResId) {
        if (fragment.getContext() == null) return;
        Bitmap markerBitmap = getMarkerIconFromDrawable(ContextCompat.getDrawable(fragment.getContext(), markerResId));
        addPlaceToMap(place, markerBitmap);
    }

    private void addPlaceToMap(Place place, Bitmap markerBitmap) {
        BitmapDescriptor placeDescriptor = BitmapDescriptorFactory.fromBitmap(markerBitmap);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(place.getLatLng())
                .icon(placeDescriptor)
                .title(place.getAddress())
//                .snippet("Population: 4,137,400")//вторичный текст серого цвета
                ;
        Marker marker = map.addMarker(markerOptions);
        marker.setTag(place);
        allocatedMarkers.add(marker);
    }

    @Contract("null -> null")
    private Bitmap getMarkerIconFromDrawable(Drawable drawable) {
        return getMarkerIconFromDrawable(drawable, -1);
    }

    @Contract("null, _ -> null")
    private Bitmap getMarkerIconFromDrawable(Drawable drawable, int color) {
        if (drawable == null) return null;
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        if (color != -1) drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);
        return bitmap;
    }

    private void showDistanceToTargetToast() {
        if (currentLocation == null || destination == null || fragment.getContext() == null) return;
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                destination.latitude, destination.longitude, results);
        String msg = "you are " + Math.round(results[0]/1000) + "km " + (int) results[0]%1000 + "m away";
        Toast.makeText(fragment.getContext(), msg, Toast.LENGTH_LONG).show();
    }
}

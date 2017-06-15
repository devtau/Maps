package com.devtau.maps;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapHelperImpl implements MapHelper {

    private static final String LOG_TAG = "MapHelperImpl";
    private static final int ZOOM = 14;//0 - максимально высоко, 5 - страна, 10 - город, 15 - квартал, 18 - дом, 21 - максимально близко
    private static final int MIN_ZOOM = 6;
    private Activity mActivity;
    private GoogleMap mMap;
    private LatLng mCurrentLocation;
    private LatLng mDestination;
    private String mDestinationTitle;
    private Marker mCurrLocationMarker;
    private GoogleApiClient mGoogleApiClient;
    private boolean mConfigurationChanged;
    
    public MapHelperImpl(boolean configurationChanged, Activity activity) {
        this(new LatLng(28.460362, -16.250817), "Marker in Tenerife", configurationChanged, activity);
    }
    
    public MapHelperImpl(@NonNull LatLng destination, @NonNull String destinationTitle, boolean configurationChanged, Activity activity) {
        mDestination = destination;
        mDestinationTitle = destinationTitle;
        mActivity = activity;
        mConfigurationChanged = configurationChanged;
        mGoogleApiClient = buildGoogleApiClient(activity);
    }
    
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.addMarker(new MarkerOptions().position(mDestination).title(mDestinationTitle));
//        addPlaceToMap(new LatLng(28.25, -16.4), ContextCompat.getColor(mActivity, R.color.colorAccent), "Some place");
        if (!mConfigurationChanged) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDestination, ZOOM));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        processMyLocation(location);
    }


    @Override
    public void handleGeoData() {
        PermissionHelper permissionHelper = new PermissionHelperImpl();
        if (!permissionHelper.checkGPSPermission(mActivity)) {
            permissionHelper.requestGPSPermission(mActivity);
            return;
        }
        processMyLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));

        LocationRequest locationRequest = new LocationRequest()
                .setInterval(5000) //5 seconds
                .setFastestInterval(3000) //3 seconds
//                .setSmallestDisplacement(0.1F) //1/10 meter
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
    }

    @Override
    public void goToLocation(LatLng location) {
        if (location != null) {
            float currentZoom = mMap.getCameraPosition().zoom;
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location)
                    .zoom(currentZoom < MIN_ZOOM ? ZOOM : currentZoom)
                    .bearing(0)//азимут. 0 - север, 90 - восток, 180 - юг, 270 - запад
                    .tilt(0)//0-90 условный угол наклона камеры к плоскости карты, где 0 - это взгляд перпендикулярно вниз, а 90 - около 45 градусов к плоскости
                    .build();
            Log.d("куку", "currentZoom=" + currentZoom);
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            String gpsUnavailableMsg;
            PermissionHelper permissionHelper = new PermissionHelperImpl();
            if (permissionHelper.checkGPSPermission(mActivity)) {
                gpsUnavailableMsg = mActivity.getString(R.string.geo_data_unavailable);
            } else {
                gpsUnavailableMsg = mActivity.getString(R.string.permission_cancelled_msg_gps);
            }
            Toast.makeText(mActivity, gpsUnavailableMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void goToMyLocation() {
        goToLocation(mCurrentLocation);
    }

    @Override
    public void goToDestination() {
        goToLocation(mDestination);
        showDistanceToTargetToast();
    }

    @Override
    public void subscribeToLocationChange() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void unsubscribeFromLocationChange() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


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

    private void processMyLocation(Location location) {
        if (location == null) return;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        mCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(mCurrentLocation)
                .title("Current location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_pin));
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        mCurrLocationMarker = mMap.addMarker(markerOptions);
    }
    
    private void addPlaceToMap(LatLng location, int color, String title) {
        Bitmap place = getMarkerIconFromDrawable(ContextCompat.getDrawable(mActivity, R.drawable.map_pin), color);
        BitmapDescriptor placeDescriptor = BitmapDescriptorFactory.fromBitmap(place);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(location)
                .title(title)
                .icon(placeDescriptor);
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag(title);//здесь можно добавить к маркеру объект
    }

    private Bitmap getMarkerIconFromDrawable(Drawable drawable, int color) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);
        return bitmap;
    }
    
    private void showDistanceToTargetToast() {
        float[] results = new float[1];
        Location.distanceBetween(mCurrentLocation.latitude, mCurrentLocation.longitude,
                mDestination.latitude, mDestination.longitude, results);
        String msg = "you are " + Math.round(results[0]/1000) + "km " + (int) results[0]%1000 + "m away";
        Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
    }
}

package com.devtau.maps;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener {

    private static final String LOG_TAG = "MapsActivity";
    private GoogleMap mMap;
    private LatLng latLng;
    private Marker currLocationMarker;
    private LatLngBounds.Builder boundsBuilder;
    private LatLngBounds.Builder myLocationBoundsBuilder;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initNavigationButtons();
        myLocationBoundsBuilder = new LatLngBounds.Builder();

        PermissionHelper permissionHelper = new PermissionHelperImpl();
        if (!permissionHelper.checkGPSPermission(this)) {
            permissionHelper.requestGPSPermission(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        addPlaceToMap(-30, 130, ContextCompat.getColor(this, R.color.colorPrimary));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        buildGoogleApiClient();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_pin));
        currLocationMarker = mMap.addMarker(markerOptions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelperImpl.GPS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            handleGeoData();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
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

    private void handleGeoData() {
        PermissionHelper permissionHelper = new PermissionHelperImpl();
        if (!permissionHelper.checkGPSPermission(this)) {
            permissionHelper.requestGPSPermission(this);
            return;
        }
        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            //place marker at current position
            //mMap.clear();
            latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = mMap.addMarker(markerOptions);
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000); //5 seconds
        locationRequest.setFastestInterval(3000); //3 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //locationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, this);
        myLocationBoundsBuilder = new LatLngBounds.Builder();
        myLocationBoundsBuilder.include(latLng);
    }

    private void initNavigationButtons() {
        View buttonMyLocation = findViewById(R.id.buttonMyLocation);
        buttonMyLocation.setOnClickListener(v -> {
            if (latLng != null) {
                showMe();
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
            } else {
                String gpsUnavailableMsg;
                PermissionHelper permissionHelper = new PermissionHelperImpl();
                if (permissionHelper.checkGPSPermission(this)) {
                    gpsUnavailableMsg = getString(R.string.geo_data_unavailable);
                } else {
                    gpsUnavailableMsg = getString(R.string.permission_cancelled_msg_gps);
                }
                Toast.makeText(this, gpsUnavailableMsg, Toast.LENGTH_SHORT).show();
            }
        });
        buttonMyLocation.setOnLongClickListener(v -> {
            showViewHint(v);
            return false;
        });

        View buttonResetLocation = findViewById(R.id.buttonResetLocation);
        buttonResetLocation.setOnClickListener(v -> resetMapToBounds());
        buttonResetLocation.setOnLongClickListener(v -> {
            showViewHint(v);
            return false;
        });
    }

    private void addPlaceToMap(double lat, double lon, int color) {
        Bitmap place = getMarkerIconFromDrawable(ContextCompat.getDrawable(this, R.drawable.map_pin), color);
        BitmapDescriptor placeDescriptor = BitmapDescriptorFactory.fromBitmap(place);
        LatLng position = new LatLng(lat, lon);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(placeDescriptor);
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag("Some place");
        boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(position);
    }

    public static Bitmap getMarkerIconFromDrawable(Drawable drawable, int color) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        drawable.draw(canvas);
        return bitmap;
    }

    private void resetMapToBounds() {
        LatLngBounds bounds = boundsBuilder.build();
        int padding = 30; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
//        CameraUpdate cu = CameraUpdateFactory.scrollBy(0, 500);
        mMap.animateCamera(cu);
    }

    private void showMe() {
        LatLngBounds bounds = myLocationBoundsBuilder.build();
        int padding = 30;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private void showViewHint(View view) {
        String contentDesc = view.getContentDescription().toString();
        if (!TextUtils.isEmpty(contentDesc)) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);

            Toast t = Toast.makeText(this, contentDesc, Toast.LENGTH_SHORT);
            int horizontalOffset = (int) AppUtils.convertDpToPixel(36, this);
            int verticalOffset = (int) AppUtils.convertDpToPixel(72, this);
            t.setGravity(Gravity.TOP | Gravity.START, pos[0] - horizontalOffset, pos[1] - verticalOffset);
            t.show();
        }
    }
}

package com.devtau.maps;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.SupportMapFragment;

public class MapsActivity extends FragmentActivity {

    private MapHelper mapHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mapHelper = new MapHelperImpl(savedInstanceState != null, this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapHelper);
        initNavigationButtons();

        PermissionHelper permissionHelper = new PermissionHelperImpl();
        if (!permissionHelper.checkGPSPermission(this)) {
            permissionHelper.requestGPSPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelperImpl.GPS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mapHelper.handleGeoData();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapHelper.subscribeToLocationChange();
    }

    @Override
    protected void onStop() {
        mapHelper.unsubscribeFromLocationChange();
        super.onStop();
    }

    private void initNavigationButtons() {
        View buttonMyLocation = findViewById(R.id.buttonMyLocation);
        buttonMyLocation.setOnClickListener(v -> mapHelper.goToMyLocation());
        buttonMyLocation.setOnLongClickListener(v -> {
            showHintAboveView(v);
            return false;
        });

        View buttonResetLocation = findViewById(R.id.buttonResetLocation);
        buttonResetLocation.setOnClickListener(v -> mapHelper.goToTenerife());
        buttonResetLocation.setOnLongClickListener(v -> {
            showHintAboveView(v);
            return false;
        });
    }

    private void showHintAboveView(View view) {
        String contentDesc = view.getContentDescription().toString();
        if (!TextUtils.isEmpty(contentDesc)) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);

            Toast toast = Toast.makeText(this, contentDesc, Toast.LENGTH_SHORT);
            int horizontalOffset = (int) AppUtils.convertDpToPixel(36, this);
            int verticalOffset = (int) AppUtils.convertDpToPixel(72, this);
            toast.setGravity(Gravity.TOP | Gravity.START, pos[0] - horizontalOffset, pos[1] - verticalOffset);
            toast.show();
        }
    }
}

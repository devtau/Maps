package com.devtau.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.devtau.maps.api.RESTClient;
import com.devtau.maps.api.RESTClientImpl;
import com.devtau.maps.api.RESTClientView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxbinding.widget.RxTextView;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import rx.android.schedulers.AndroidSchedulers;

public class MapFragment extends Fragment {

    public static final String FRAGMENT_TAG = "ru.devcluster.maps.MapsFragment";
    private static final String PLACES = "PLACES";
    private static final String PLACES_ARE_VISIBLE = "PLACES_ARE_VISIBLE";
    private static final String POLYGONS = "POLYGONS";
    private static final String POLYGONS_ARE_VISIBLE = "POLYGONS_ARE_VISIBLE";
    private static final String MARKER_RES_ID = "MARKER_RES_ID";
    private static final String WITH_BALLOON = "WITH_BALLOON";
    private static final String CITY_NAME = "CITY_NAME";

    private MapHelper mapHelper;
    private RESTClient restClient;
    private BalloonListener listener;
    private String cityName;

    private TextView addressSearch;
    private View buttonMyLocation;
    private View buttonResetLocation;


    public static MapFragment newInstance(ArrayList<Place> places, boolean placesAreVisible,
                                          ArrayList<MapPolygon> mapPolygons, boolean polygonsAreVisible,
                                          int markerResId, boolean withBalloon, String cityName) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(PLACES, places);
        args.putInt(PLACES_ARE_VISIBLE, placesAreVisible ? 1 : 0);
        args.putParcelableArrayList(POLYGONS, mapPolygons);
        args.putInt(POLYGONS_ARE_VISIBLE, polygonsAreVisible ? 1 : 0);
        args.putInt(MARKER_RES_ID, markerResId);
        args.putInt(WITH_BALLOON, withBalloon ? 1 : 0);
        args.putString(CITY_NAME, cityName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getArguments() == null || getArguments().getInt(WITH_BALLOON) != 1) return;
        if (getParentFragment() instanceof BalloonListener) {
            listener = (BalloonListener) getParentFragment();
        } else if (context instanceof BalloonListener) {
            listener = (BalloonListener) context;
        } else {
            throw new RuntimeException(getParentFragment().toString() + " must implement BalloonListener");
        }
    }

    public MapFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);
        if (getArguments() == null) return root;

        ArrayList<Place> places = getArguments().getParcelableArrayList(PLACES);
        boolean placesAreVisible = getArguments().getInt(PLACES_ARE_VISIBLE) == 1;
        ArrayList<MapPolygon> mapPolygons = getArguments().getParcelableArrayList(POLYGONS);
        boolean polygonsAreVisible = getArguments().getInt(POLYGONS_ARE_VISIBLE) == 1;
        int markerResId = getArguments().getInt(MARKER_RES_ID);
        cityName = getArguments().getString(CITY_NAME);

        mapHelper = new MapHelperImpl(places, placesAreVisible, mapPolygons, polygonsAreVisible,
                savedInstanceState != null, this, markerResId, listener);
        restClient = new RESTClientImpl(new RESTClientViewImpl());
        initUi(root);
        initNavigationButtons();
        initSearch();

        PermissionHelper permissionHelper = new PermissionHelperImpl();
        if (!permissionHelper.checkGPSPermission(getContext())) {
            permissionHelper.requestGPSPermission(this);
        }
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.some_definitely_unique_map_id);
        mapFragment.getMapAsync(mapHelper);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapHelper.subscribeToLocationChange();
    }

    @Override
    public void onStop() {
        mapHelper.unsubscribeFromLocationChange();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionHelperImpl.GPS_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mapHelper.handleGeoData();
        }
    }


    public void showPlaces(boolean show) {
        mapHelper.showPlaces(show);
    }

    public void showPolygons(boolean show) {
        mapHelper.showPolygons(show);
    }

    public void goToLocation(LatLng location) {
        mapHelper.goToLocation(location);
    }


    private void initUi(View root) {
        addressSearch = root.findViewById(R.id.addressSearch);
        buttonMyLocation = root.findViewById(R.id.buttonMyLocation);
        buttonResetLocation = root.findViewById(R.id.buttonResetLocation);
    }

    private void initNavigationButtons() {
        buttonMyLocation.setOnClickListener(v -> mapHelper.goToMyLocation());
        buttonMyLocation.setOnLongClickListener(v -> {
            showHintAboveView(v);
            return false;
        });

        buttonResetLocation.setOnClickListener(v -> mapHelper.centerCameraIfPossible(true));
        buttonResetLocation.setOnLongClickListener(v -> {
            showHintAboveView(v);
            return false;
        });
    }

    private void initSearch() {
        addressSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                requestGeoCoding(addressSearch.getText().toString());
                return true;
            }
            return false;
        });
        RxTextView.textChanges(addressSearch)
                .debounce(AppUtils.CLICKS_DEBOUNCE_RATE_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(CharSequence::toString)
                .subscribe(this::requestGeoCoding);
    }

    private void requestGeoCoding(String address) {
        if (!TextUtils.isEmpty(address)) restClient.geoCode(cityName, address);
    }

    private void showHintAboveView(View view) {
        String contentDesc = view.getContentDescription().toString();
        if (!TextUtils.isEmpty(contentDesc)) {
            int[] pos = new int[2];
            view.getLocationInWindow(pos);

            Toast toast = Toast.makeText(getContext(), contentDesc, Toast.LENGTH_SHORT);
            int horizontalOffset = (int) getResources().getDimension(R.dimen.horizontalOffset);
            int verticalOffset = (int) getResources().getDimension(R.dimen.verticalOffset);
            toast.setGravity(Gravity.TOP | Gravity.START, pos[0] - horizontalOffset, pos[1] - verticalOffset);
            toast.show();
        }
    }



    private class RESTClientViewImpl implements RESTClientView {
        @Override
        public Resources getResources() {
            return MapFragment.this.getResources();
        }

        @Override
        public Context getContext() {
            return MapFragment.this.getContext();
        }

        @Override
        public void showToast(String msg) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void showDialog(int msg) {
            try {
                new AlertDialog.Builder(getContext())
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .setMessage(msg).show();
            } catch (WindowManager.BadTokenException e) {
                showToast(getString(msg));
            }
        }

        @Override
        public void showLocation(float[] latLng, String address) {
            mapHelper.goToLocation(new LatLng(latLng[0], latLng[1]));
        }
    }
}

package com.devtau.mapsExample;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import com.devtau.maps.BalloonListener;
import com.devtau.maps.MapBuilder;
import com.devtau.maps.MapPolygon;
import com.devtau.maps.Place;

public class MapsActivity extends FragmentActivity implements BalloonListener {

    private boolean isTakeAwayBtnNeeded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        new MapBuilder(getSupportFragmentManager(), R.id.map, "Санкт-Петербург")
                .withPlaces(Place.Companion.getMock(), true)
                .withPolygons(MapPolygon.Companion.getMock(), true)
                .withMarker(R.drawable.pointer)
                .withBalloon(this)
                .build();
    }

    @Override
    public View bindBalloon(Place place) {
        if (place == null) return null;
        View balloonView = getLayoutInflater().inflate(R.layout.balloon, null);

        TextView address = balloonView.findViewById(R.id.address);
        TextView workTimeFrom = balloonView.findViewById(R.id.workTimeFrom);
        TextView workTimeTo = balloonView.findViewById(R.id.workTimeTo);
        View takeAway = balloonView.findViewById(R.id.takeAway);
        TextView phone = balloonView.findViewById(R.id.phone);

        address.setText(place.getAddress());
        workTimeFrom.setText(place.getWorkTimeFrom().substring(0, 5));
        workTimeTo.setText(place.getWorkTimeTo().substring(0, 5));
        takeAway.setVisibility(isTakeAwayBtnNeeded ? View.VISIBLE : View.GONE);
        phone.setText(place.getPhone());

        return balloonView;
    }

    @Override
    public void onBalloonClicked(Place place) {
        Toast.makeText(this, "place " + place.getAddress() + " clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCameraMoved(ArrayList<Place> visiblePlaces) {
        Toast.makeText(this, "onCameraMoved. visiblePlaces size = " + visiblePlaces.size(), Toast.LENGTH_SHORT).show();
    }
}

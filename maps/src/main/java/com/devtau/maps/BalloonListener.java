package com.devtau.maps;

import android.view.View;
import java.util.ArrayList;

public interface BalloonListener {
    View bindBalloon(Place place);
    void onBalloonClicked(Place place);
    void onCameraMoved(ArrayList<Place> visiblePlaces);
}

package com.devtau.maps

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize
import java.util.ArrayList

@Parcelize
data class MapPolygon(var id: Int, var title: String, var color: String, var bounds: ArrayList<LatLng>) : Parcelable {

    companion object {
        fun getMock() : ArrayList<MapPolygon> {
            val places = ArrayList<MapPolygon>()

            var bounds = ArrayList<LatLng>()
            bounds.add(LatLng(59.99665441611325,30.330899326414592))
            bounds.add(LatLng(59.98599067286552,30.29914197167827))
            bounds.add(LatLng(60.01702633423976,30.316823093504446))
            places.add(MapPolygon(1, "polygon 1", "#7d95B9C7", bounds))

            bounds = ArrayList()
            bounds.add(LatLng(59.97162356911795,30.259831516355998))
            bounds.add(LatLng(59.947854434254154,30.234688599014643))
            bounds.add(LatLng(59.942256728739096,30.27811892738378))
            bounds.add(LatLng(59.950179356240504,30.288418610000978))
            places.add(MapPolygon(2, "polygon 2", "#7d56A5EC", bounds))

            bounds = ArrayList()
            bounds.add(LatLng(59.93579665928558,30.31571276893651))
            bounds.add(LatLng(59.91632268008004,30.31828768959081))
            bounds.add(LatLng(59.91494355653585,30.35038836708105))
            bounds.add(LatLng(59.92347596077756,30.38437731971775))
            places.add(MapPolygon(3, "polygon 3", "#7dAFDCEC", bounds))

            bounds = ArrayList()
            bounds.add(LatLng(59.92907684746182,30.412358124161123))
            bounds.add(LatLng(59.890109499282886,30.464028198624025))
            bounds.add(LatLng(59.90809270932021,30.48331889728557))
            bounds.add(LatLng(59.944110905217954,30.477654071846107))
            places.add(MapPolygon(4, "polygon 4", "#7d837E7C", bounds))
            return places;
        }
    }
}
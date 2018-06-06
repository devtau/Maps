package com.devtau.maps

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Place(var id: Int, var address: String, var lat: Double, var lon: Double, val phone: String,
                 var workTimeFrom: String, var workTimeTo: String) : Comparable<Place>, Parcelable {

    override fun compareTo(other: Place): Int {
        return address.compareTo(other.address)
    }

    fun getLatLng() : LatLng {
        return LatLng(lat, lon)
    }

    companion object {
        fun getMock() : ArrayList<Place> {
            val places = java.util.ArrayList<Place>()
            places.add(Place(1, "some address 1", 59.950, 30.370, "+79219789900", "10:00", "23:00"))
            places.add(Place(2, "some address 2", 59.960, 30.390, "+79219789901", "10:01", "23:01"))
            places.add(Place(3, "some address 3", 59.965, 30.380, "+79219789902", "10:02", "23:02"))
            return places;
        }
    }
}
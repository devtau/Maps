package com.devtau.maps.api;

import com.devtau.maps.api.geoCoding.GeoCoderResponseMetaData;
import com.devtau.maps.api.geoCoding.GeoObject;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class GeoCodingResponse {

    private Response response;

    public float[] getLatLng() {
        if (response.geoObjectCollection == null ||
                response.geoObjectCollection.featureMember == null ||
                response.geoObjectCollection.featureMember.size() == 0 ||
                response.geoObjectCollection.featureMember.get(0) == null) return null;
        String pos = response.geoObjectCollection.featureMember.get(0).geoObject.point.pos;
        String[] posArray = pos.split(" ");
        float[] latLng = new float[2];
        //ушлепки из яндекса возвращают строку в формате lngLat, хотя их же GeoPoint принимает в формате latLng
        latLng[0] = Float.parseFloat(posArray[1]);
        latLng[1] = Float.parseFloat(posArray[0]);
        return latLng;
    }

    public String getAddress() {
        if (response.geoObjectCollection == null ||
                response.geoObjectCollection.featureMember == null ||
                response.geoObjectCollection.featureMember.size() == 0 ||
                response.geoObjectCollection.featureMember.get(0) == null) return null;
        return response.geoObjectCollection.featureMember.get(0).geoObject.name;
    }



    private class Response {
        @SerializedName("GeoObjectCollection")
        public GeoObjectCollection geoObjectCollection;
    }


    private class GeoObjectCollection {
        public MetaDataProperty metaDataProperty;
        public ArrayList<FeatureMember> featureMember = null;
    }


    private class MetaDataProperty {
        @SerializedName("GeocoderResponseMetaData")
        public GeoCoderResponseMetaData geoCoderResponseMetaData;
    }


    private class FeatureMember {
        @SerializedName("GeoObject")
        public GeoObject geoObject;
    }
}

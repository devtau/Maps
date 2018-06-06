package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class Locality {

    @SerializedName("LocalityName")
    public String localityName;

    @SerializedName("Thoroughfare")
    public Thoroughfare thoroughfare;
}

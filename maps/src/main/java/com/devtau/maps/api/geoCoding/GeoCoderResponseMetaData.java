package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class GeoCoderResponseMetaData {

    @SerializedName("request")
    public String request;

    @SerializedName("found")
    public String found;

    @SerializedName("results")
    public String results;
}

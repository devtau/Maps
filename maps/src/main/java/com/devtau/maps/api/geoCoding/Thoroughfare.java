package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class Thoroughfare {

    @SerializedName("ThoroughfareName")
    public String thoroughfareName;

    @SerializedName("Premise")
    public Premise premise;
}

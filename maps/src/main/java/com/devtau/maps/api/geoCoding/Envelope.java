package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class Envelope {

    @SerializedName("lowerCorner")
    public String lowerCorner;

    @SerializedName("upperCorner")
    public String upperCorner;

}

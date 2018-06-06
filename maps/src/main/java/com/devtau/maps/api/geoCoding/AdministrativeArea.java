package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class AdministrativeArea {

    @SerializedName("AdministrativeAreaName")
    public String administrativeAreaName;

    @SerializedName("Locality")
    public Locality locality;
}

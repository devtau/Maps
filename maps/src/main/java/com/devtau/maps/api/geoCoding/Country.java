package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class Country {

    @SerializedName("AddressLine")
    public String addressLine;

    @SerializedName("CountryNameCode")
    public String countryNameCode;

    @SerializedName("CountryName")
    public String countryName;

    @SerializedName("AdministrativeArea")
    public AdministrativeArea administrativeArea;
}

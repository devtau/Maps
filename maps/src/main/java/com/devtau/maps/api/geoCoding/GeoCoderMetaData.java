package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class GeoCoderMetaData {

    public String kind;
    public String text;
    public String precision;

    @SerializedName("Address")
    public Address address;

    @SerializedName("AddressDetails")
    public AddressDetails addressDetails;
}

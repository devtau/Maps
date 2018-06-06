package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Address {

    public String country_code;
    public String formatted;
    @SerializedName("Components")
    public ArrayList<Component> components = null;
    public String postal_code;
}

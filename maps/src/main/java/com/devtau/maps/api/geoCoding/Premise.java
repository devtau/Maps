package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class Premise {

    @SerializedName("PremiseNumber")
    public String premiseNumber;

    @SerializedName("PostalCode")
    public PostalCode postalCode;
}

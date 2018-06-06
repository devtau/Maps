package com.devtau.maps.api.geoCoding;

import com.google.gson.annotations.SerializedName;

public class GeoObject {

    public MetaDataProperty metaDataProperty;
    public String description;
    public String name;
    public BoundedBy boundedBy;
    @SerializedName("Point")
    public Point point;


    public class MetaDataProperty {
        @SerializedName("GeocoderMetaData")
        public GeoCoderMetaData geocoderMetaData;
    }
}

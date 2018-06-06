package com.devtau.maps.api;

public interface RESTClient {
	void geoCode(String cityName, String address);
	void geoCode(String cityName, String address, GeoCodingListener geoCodeListener);
}

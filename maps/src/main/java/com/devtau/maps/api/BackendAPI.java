package com.devtau.maps.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BackendAPI {

	String YANDEX_API_BASE_URL = "https://geocode-maps.yandex.ru";
	String GEO_CODING_ENDPOINT = "/1.x/?format=json";

	@GET(GEO_CODING_ENDPOINT)
	Call<GeoCodingResponse> geoCode(@Query("geocode") String address);
}

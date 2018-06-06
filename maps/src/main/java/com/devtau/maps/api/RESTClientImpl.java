package com.devtau.maps.api;

import android.support.annotation.NonNull;
import com.devtau.maps.AppUtils;
import com.devtau.maps.BuildConfig;
import com.devtau.maps.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RESTClientImpl implements RESTClient {

	private static final String LOG_TAG = "RESTClient";
	private static final int TIMEOUT_CONNECT = 10;
	private static final int TIMEOUT_READ = 60;
	private static final int TIMEOUT_WRITE = 120;
	private static OkHttpClient httpClientLogging;
	private static OkHttpClient httpClientNotLogging;
	private RESTClientView view;


	public RESTClientImpl(RESTClientView view) {
		this.view = view;
	}


	@Override
	public void geoCode(String cityName, String address) {
		if (!AppUtils.checkConnection(view.getContext())) return;
		Call<GeoCodingResponse> call = getYandexBackendAPIClient().geoCode(cityName + ",+" + address);
		Callback<GeoCodingResponse> callback = new Callback<GeoCodingResponse>() {
			@Override
			public void onResponse(Call<GeoCodingResponse> call, Response<GeoCodingResponse> response){
				if (response.isSuccessful()) {
					Logger.d(LOG_TAG, "geoCode retrofit response isSuccessful");
					view.showLocation(response.body().getLatLng(), response.body().getAddress());
				} else {
					handleError(response.code());
				}
			}

			@Override
			public void onFailure(Call<GeoCodingResponse> call, Throwable t){
				Logger.e(LOG_TAG, "geoCode retrofit failure: " + t.getLocalizedMessage());
				handleFailure(t.getLocalizedMessage());
			}
		};
		call.enqueue(callback);
	}

	@Override
	public void geoCode(String cityName, String address, GeoCodingListener geoCodeListener) {
		if (!AppUtils.checkConnection(view.getContext())) return;
		Call<GeoCodingResponse> call = getYandexBackendAPIClient().geoCode(cityName + ",+" + address);
		Callback<GeoCodingResponse> callback = new Callback<GeoCodingResponse>() {
			@Override
			public void onResponse(Call<GeoCodingResponse> call, Response<GeoCodingResponse> response){
				if (response.isSuccessful()) {
					Logger.d(LOG_TAG, "geoCode retrofit response isSuccessful");
					geoCodeListener.onGeoCodingCompleted(response.body().getLatLng());
				} else {
					handleError(response.code());
				}
			}

			@Override
			public void onFailure(Call<GeoCodingResponse> call, Throwable t){
				Logger.e(LOG_TAG, "geoCode retrofit failure: " + t.getLocalizedMessage());
				handleFailure(t.getLocalizedMessage());
			}
		};
		call.enqueue(callback);
	}

	private BackendAPI getYandexBackendAPIClient() {
		Gson gson = new GsonBuilder().setLenient().create();
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(BackendAPI.YANDEX_API_BASE_URL)
				.addConverterFactory(GsonConverterFactory.create(gson))
				.client(getClient())
				.build();
		return retrofit.create(BackendAPI.class);
	}

	@NonNull
	private static OkHttpClient getClient() {
		OkHttpClient client = BuildConfig.DEBUG ? httpClientLogging : httpClientNotLogging;
		if (client == null) {
			synchronized (RESTClientImpl.class) {
				client = BuildConfig.DEBUG ? httpClientLogging : httpClientNotLogging;
				if (client == null) {
					if (BuildConfig.DEBUG) {
						client = httpClientLogging = buildClient();
					} else {
						client = httpClientNotLogging = buildClient();
					}
				}
			}
		}
		return client;
	}

	@NonNull
	private static OkHttpClient buildClient() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(TIMEOUT_CONNECT, TimeUnit.SECONDS)
				.readTimeout(TIMEOUT_READ, TimeUnit.SECONDS)
				.writeTimeout(TIMEOUT_WRITE, TimeUnit.SECONDS);
		if (BuildConfig.DEBUG) {
			builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
		}
		return builder.build();
	}

	private void handleError(int errorCode) {
		String errorMsg = "retrofit response is not successful\nerrorCode: " + String.valueOf(errorCode);
		view.showToast(errorMsg);
	}

	private void handleFailure(String failureMessage) {
		view.showToast(failureMessage);
	}
}

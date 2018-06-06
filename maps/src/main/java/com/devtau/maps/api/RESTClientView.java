package com.devtau.maps.api;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

public interface RESTClientView {
	Resources getResources();
	Context getContext();
	void showToast(String msg);
	void showDialog(int msg);

	void showLocation(@Nullable float[] latLng, String address);
}

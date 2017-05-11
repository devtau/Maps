package com.devtau.maps;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Интерфейс хелпера для работы с разрешениями
 * Клиент переопределяет колбэк onRequestPermissionsResult и обрабатывает в нем реакцию пользователя
 */

public interface PermissionHelper {

	boolean checkGPSPermission(Context context);

	void requestGPSPermission(Fragment fragment);
	void requestGPSPermission(Activity activity);
}

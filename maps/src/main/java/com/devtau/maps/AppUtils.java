package com.devtau.maps;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import org.jetbrains.annotations.Contract;
import java.util.ArrayList;
import java.util.Map;

public class AppUtils {

    public static final int CLICKS_DEBOUNCE_RATE_MS = 300;

    public static boolean checkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) return false;
        return networkInfo.isConnectedOrConnecting();
    }

    @Contract(value = "null -> true", pure = true)
    public static boolean isEmpty(ArrayList list) {
        return !notEmpty(list);
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean notEmpty(ArrayList list) {
        return list != null && !list.isEmpty();
    }

    @Contract(value = "null -> false", pure = true)
    public static boolean notEmpty(Map map) {
        return map != null && !map.isEmpty();
    }
}

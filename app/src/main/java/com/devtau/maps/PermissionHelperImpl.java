package com.devtau.maps;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class PermissionHelperImpl implements PermissionHelper {

	private static final String LOG_TAG = "PermissionHelperImpl";
	public static final int GPS_REQUEST_CODE = 5748;


	@Override
	public boolean checkGPSPermission(Context context) {
		if (!isPermissionDynamic()) return true;
		return isPermissionGranted(context, ACCESS_COARSE_LOCATION) && isPermissionGranted(context, ACCESS_FINE_LOCATION);
	}

	@Override
	public void requestGPSPermission(Fragment fragment) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(fragment.getActivity(), ACCESS_FINE_LOCATION)) {
			String explanationText = fragment.getString(R.string.permission_explanation_gps);
			String declinedText = fragment.getString(R.string.permission_cancelled_msg_gps);
			showExplanationDialog(fragment, explanationText, declinedText, ACCESS_FINE_LOCATION, GPS_REQUEST_CODE);
		} else {
			fragment.requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, GPS_REQUEST_CODE);
		}
	}

	@Override
	public void requestGPSPermission(Activity activity) {
		if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)) {
			String explanationText = activity.getString(R.string.permission_explanation_gps);
			String declinedText = activity.getString(R.string.permission_cancelled_msg_gps);
			showExplanationDialog(activity, explanationText, declinedText, ACCESS_FINE_LOCATION, GPS_REQUEST_CODE);
		} else {
			activity.requestPermissions(new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION}, GPS_REQUEST_CODE);
		}
	}


	private boolean isPermissionDynamic() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
	}

	private boolean isPermissionGranted(Context context, String permission) {
		try {
			int selfPermission = PermissionChecker.checkSelfPermission(context, permission);
			if (selfPermission == PackageManager.PERMISSION_GRANTED) {
				Log.d(LOG_TAG, "Permission: " + permission + " is granted");
				return true;
			} else {
				Log.d(LOG_TAG, "Permission: " + permission + " is denied");
				return false;
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Failed to check permission", e);
			return false;
		}
	}


	private void showExplanationDialog(final Fragment fragment, String explanationText, final String declinedText,
												  final String permission, final int requestCode) {
		Log.d(LOG_TAG, "Showing explanation dialog for permission: " + permission);
		new AlertDialog.Builder(fragment.getContext())
				.setTitle(R.string.permission_needed)
				.setMessage(explanationText)
				.setPositiveButton(android.R.string.yes, (dialog, which) ->
						fragment.requestPermissions(new String[]{permission}, requestCode))
				.setNegativeButton(android.R.string.no, (dialog, which) ->
						Toast.makeText(fragment.getContext(), declinedText, Toast.LENGTH_LONG).show())
				.show();
	}

	private void showExplanationDialog(final Activity activity, String explanationText, final String declinedText,
												  final String permission, final int requestCode) {
		Log.d(LOG_TAG, "Showing explanation dialog for permission: " + permission);
		new AlertDialog.Builder(activity)
				.setTitle(R.string.permission_needed)
				.setMessage(explanationText)
				.setPositiveButton(android.R.string.yes, (dialog, which) ->
						activity.requestPermissions(new String[]{permission}, requestCode))
				.setNegativeButton(android.R.string.no, (dialog, which) ->
						Toast.makeText(activity, declinedText, Toast.LENGTH_LONG).show())
				.show();
	}
}

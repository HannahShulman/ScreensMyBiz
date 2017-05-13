package com.cliqdbase.app.async.device;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import com.cliqdbase.app.constants.AppConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;

/**
 * Created by Yuval on 20/05/2015.
 *
 * @author Yuval Siev
 */
public class RegistrationIdExtractor extends AsyncTask<Void, Void, Void> {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GoogleCloudMessaging gcm;
    private String regId;

    private Activity callingActivity;
    private RegIdAsyncResponse callback;

    public RegistrationIdExtractor(Activity callingActivity, RegIdAsyncResponse callback) {
        this.callingActivity = callingActivity;
        this.callback = callback;
    }


    @Override
    protected Void doInBackground(Void... params) {
        if (checkPlayServices()) {
            this.gcm = GoogleCloudMessaging.getInstance(this.callingActivity);
            this.regId = getRegistrationId();

            if (regId.isEmpty()) {
                System.out.println("empty");
                registerInBackground();

            }
        }

        return null;
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.callingActivity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this.callingActivity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
                Toast.makeText(this.callingActivity, "Google Play services are unavailable.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    private String getRegistrationId() {
        final SharedPreferences prefs = this.callingActivity.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String registrationId = prefs.getString(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_REG_ID, "");

        if (registrationId.isEmpty()) {
            System.out.println("Registration not found");
            return "";
        }

        int registeredVersion = prefs.getInt(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(this.callingActivity);

        if (currentVersion != registeredVersion) {  // Reg_id can change when app updates.
            System.out.println("App version changed");
            return "";
        }
        return registrationId;
    }



    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private boolean registerInBackground() {
        try {
            if (this.gcm == null)
                this.gcm = GoogleCloudMessaging.getInstance(this.callingActivity);

            this.regId = this.gcm.register(AppConstants.GOOGLE_CONSOLE_PROJECT_NUMBER); //TODO this may have been deprecated. Check this.

            storeRegistrationId(this.regId);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void storeRegistrationId(String regId) {
        SharedPreferences prefs = this.callingActivity.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        int appVersion = getAppVersion(this.callingActivity);

        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_REG_ID, regId);
        editor.putInt(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_APP_VERSION, appVersion);
        editor.apply();
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        if (this.callback != null)
            this.callback.processComplete(this.regId);
    }
}

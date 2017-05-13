package com.cliqdbase.app.general;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.cliqdbase.app._activities.LoginActivity;
import com.cliqdbase.app.constants.SharedPreferencesConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Yuval on 27/07/2015.
 *
 * @author Yuval Siev
 */
public class Common {

    public static String getSessionId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getString(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID, null);
    }


    public static void setLoggedInUserId(Context context, long userId) {
        SharedPreferences preferences = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        preferences.edit().putLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_USER_ID, userId).apply();
    }

    public static long getLoggedInUserId(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return preferences.getLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_USER_ID, -1);
    }


    public static String getTimeString(long time) {
        Date date = new Date(time);

        Calendar messageTime = Calendar.getInstance();
        messageTime.setTime(date);

        Calendar today = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat;

        if (messageTime.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                messageTime.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                messageTime.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {

            simpleDateFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());

        }// if the message was sent today, we will only display the time
        else        // If not, we will display the date as well
            simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy kk:mm", Locale.getDefault());


        return simpleDateFormat.format(date);
    }


    public static boolean isLoggedInUsingFacebook(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_FACEBOOK_LOGIN, false);
    }

    public static void setLoggedInUsingFacebook(Context context, boolean flag) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_FACEBOOK_LOGIN, flag).apply();
    }


    public static boolean isProfileDataEmpty(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_USER_DATA_EMPTY, true);
    }

    public static boolean isPreferredUnitsMetric(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_PREFERRED_UNITS_METRIC, false);
    }

    public static void setPreferredUnitMetric(Context context, boolean isMetric) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_PREFERRED_UNITS_METRIC, isMetric).apply();
    }

    public static void received401FromServer(Context context) {
        Logout.logout(context);
        Toast.makeText(context, "You were logged out. Please log in again.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static int[] convertCmToFeetAndInches(int centimeters) {
        int totalInches = (int) Math.ceil(centimeters * 0.39370);
        int feet = totalInches/12;
        int inches = totalInches%12;

        int[] arr = new int[2];
        arr[0] = feet;
        arr[1] = inches;

        return arr;
    }

    public static int convertFeetAndInchesToCm(int feet, int inches) {
        return (int) Math.ceil((feet/0.032808) + (inches/0.39370));
    }

    public static String getHeightString(Integer heightInCm, boolean isInMetric) {
        if (heightInCm == null)
            return null;
        if (isInMetric)
            return heightInCm + " cm.";
        int arr[] = convertCmToFeetAndInches(heightInCm);
        return arr[0] + " feet, " + arr[1] + " inches.";
    }

    public static Integer getHeightInCmFromString(String heightString) {
        String[] parts = heightString.split(" ");
        Integer heightInCm = null;
        if (parts.length == 2) {        // "xx cm."
            try {
                heightInCm = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        else if (parts.length == 4) {   // " xx feet, yy inches."
            try {
                int feet = Integer.parseInt(parts[0]);
                int inches = Integer.parseInt(parts[2]);

                heightInCm = convertFeetAndInchesToCm(feet, inches);

            } catch (NumberFormatException e) {
                return null;
            }
        }
        return heightInCm;
    }

    public static String getAndroidId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public static String getDeviceDesc() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    public static boolean isLoggedInAsGuest(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_AS_GUEST, false);
    }

    public static void setLoggedInAsGuest(Context context, boolean flag) {
        SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_AS_GUEST, flag).apply();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

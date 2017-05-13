package com.cliqdbase.app.general;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import com.cliqdbase.app._activities.WelcomeActivity;
import com.cliqdbase.app.chats_stuff.ChatsSQLiteHelper;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;

/**
 * Created by Yuval on 31/03/2015.
 *
 * @author Yuval Siev
 */
public class Logout {

    /**
     * Removes the cookies and other data of the login.
     * Closes the facebook and twitter sessions, if the user connected using one of them.
     * @param context    The context.
     */
    public static void logout(Context context) {
        if (Common.isLoggedInUsingFacebook(context))
            facebookLogout(context);

        cliqdbaseLogout(context);          // Do this anyway
    }

    /**
     * Logs the user out of our system.
     * @param context      The context
     */
    private static void cliqdbaseLogout(final Context context) {
        ConnectToServer logout = new ConnectToServer(context, true, 0);
        logout.delegate = new AsyncResponse_Server() {
            @Override
            public void onServerResponse(long taskCode, int httpRes, String output) {
                SharedPreferences prefs = context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // Deleting the unnecessary shared preferences values
                editor.remove(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID);
                editor.remove(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_FACEBOOK_LOGIN);
                editor.remove(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_USER_ID);
                editor.remove(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_AS_GUEST);
                editor.apply();


                // Deleting the stored messages, so nobody else can access them.
                ChatsSQLiteHelper helper = new ChatsSQLiteHelper(context);
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(ChatsSQLiteHelper.CHATS_TABLE, null, null);       // TODO maybe handle this better?
                db.delete(ChatsSQLiteHelper.CHAT_DRAFTS_TABLE, null, null);
                db.delete(ChatsSQLiteHelper.USERS_TABLE, null, null);

                db.close();
                helper.close();


                // Returning to the login context
                Intent intent = new Intent(context, WelcomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        };
        logout.execute(ServerUrlConstants.LOGOUT_URL, "GET");
    }

    /**
     * Logging out of facebook (if already connected). Closing the facebook Session.
     */
    public static void facebookLogout(Context context) {
        FacebookSdk.sdkInitialize(context);
        LoginManager.getInstance().logOut();
    }
}

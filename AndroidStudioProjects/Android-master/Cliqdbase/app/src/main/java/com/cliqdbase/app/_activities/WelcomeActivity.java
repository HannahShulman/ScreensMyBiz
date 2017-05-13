package com.cliqdbase.app._activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.device.RegIdAsyncResponse;
import com.cliqdbase.app.async.device.RegistrationIdExtractor;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;
import com.cliqdbase.app.general.Common;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;


public class WelcomeActivity extends Activity implements View.OnClickListener, AsyncResponse_Server, RegIdAsyncResponse {

    private ConnectToServer guestAsync;

    private static final int CTSC_REGISTER_GUEST = 0;

    private String regId;
    private RegistrationIdExtractor regIdAsync;

    private AlertDialog guestInfoDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking if the user is logged in.
        final SharedPreferences prefs = getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        String sessionId = prefs.getString(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID, null);
        if (sessionId != null) {
            startNextActivity();
            return;
        }

        setContentView(R.layout.activity_welcome);

        Button gotoLogin = (Button) findViewById(R.id.welcome_login);
        Button gotoSignUp = (Button) findViewById(R.id.welcome_signup);
        Button guestEnter = (Button) findViewById(R.id.welcome_guest_login);


        gotoLogin.setOnClickListener(this);
        gotoSignUp.setOnClickListener(this);
        guestEnter.setOnClickListener(this);

        guestInfoDialog = null;
        guestAsync = null;
        //deleteGuestData = null;


        // extracting the registration id.
        regId = null;
        regIdAsync = new RegistrationIdExtractor(this, this);
        regIdAsync.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.welcome_login:
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
                finish();
                break;
            case R.id.welcome_signup:
                startActivity(new Intent(WelcomeActivity.this, SignUpActivity.class));
                finish();
                break;
            case R.id.welcome_guest_login:
                if (guestInfoDialog == null) {
                    guestInfoDialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.guest_privacy_info_title)
                            .setMessage(R.string.guest_privacy_info_message)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // The dialog will be dismissed automatically here.
                                }
                            })
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendRequestToGuestManagerOnServer(1, null, null);
                                }
                            })
                            .create();
                }
                guestInfoDialog.show();

                break;
        }
    }

    /**
     * Connects to the guest manager in our server.
     *
     * Part 1 of the process is to check whether a guest account already exists on this device.
     * if so, the server will return the needed data, and ask whether the user want to re-connect to the guest user, or create a new one.
     * Part 2 of the process is the answer returned by the user.
     *
     * @param part                The part of the guest-registration process.
     * @param userId              The user id, if exists. This CANNOT be null in part 2.
     * @param useExistingGuest    true if the user would like to reconnect to the guest account, false otherwise. This can't be null in part 2.
     */
    private void sendRequestToGuestManagerOnServer(int part, Long userId, Boolean useExistingGuest) {
        String androidId = Common.getAndroidId(this);
        if (androidId == null || androidId.isEmpty()) {
            Toast.makeText(this, R.string.android_id_not_available, Toast.LENGTH_LONG).show();
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("part", part);
        json.addProperty("udid", androidId);
        json.addProperty("regId", regId);
        json.addProperty("deviceCode", 1);
        json.addProperty("deviceDesc", Common.getDeviceDesc());
        if (userId != null)
            json.addProperty("userId", userId);
        if (useExistingGuest != null)
            json.addProperty("useExistingGuestUser", useExistingGuest);


        if (guestAsync == null || guestAsync.getStatus().equals(AsyncTask.Status.FINISHED)) {
            guestAsync = new ConnectToServer(this, true, CTSC_REGISTER_GUEST);
            guestAsync.delegate = this;
        }
        guestAsync.execute(ServerUrlConstants.GUEST_USER_MANAGER, "POST", json.toString());

    }



    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        if (taskCode == CTSC_REGISTER_GUEST)
            switch (httpResultCode) {
                case 200:       // handle the server response
                    JsonReader reader = new JsonReader(new StringReader(data));
                    long userId = -1;
                    boolean guestUserExists = false;
                    boolean sessionCreated = false;

                    try {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String name = reader.nextName();
                            switch(name) {
                                case "guestUserExists":
                                    guestUserExists = reader.nextBoolean();
                                    break;
                                case "sessionCreated":
                                    sessionCreated = reader.nextBoolean();
                                    break;
                                case "userId":
                                    userId = reader.nextLong();
                                    break;
                                default:
                                    reader.skipValue();
                                    break;
                            }
                        }
                        reader.endObject();
                        reader.close();
                    } catch (IOException e) { /* Ignored */ }

                    if (userId != -1) {
                        final long userIdForAlert = userId;         // the alert needs final/static value
                        if (guestUserExists) {      // a guest user already exists. Displaying the reconnect/create dialog to the user
                            new AlertDialog.Builder(this)
                                    .setMessage(R.string.guest_user_exists_on_device)
                                    .setPositiveButton(R.string.guest_user_exists_create_new_guest, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // now we are at part 2 of the guest-registration process
                                            sendRequestToGuestManagerOnServer(2, userIdForAlert, false);
                                        }
                                    })
                                    .setNegativeButton(R.string.guest_user_exists_log_into_guest, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // now we are at part 2 of the guest-registration process
                                            sendRequestToGuestManagerOnServer(2, userIdForAlert, true);
                                        }
                                    })
                                    .setCancelable(true)
                                    .create()
                                    .show();
                        }
                    }

                    if (sessionCreated) {
                        Common.setLoggedInUserId(this, userId);
                        Common.setLoggedInAsGuest(this, true);
                        Toast.makeText(this, R.string.logged_in_as_guest, Toast.LENGTH_LONG).show();
                        startNextActivity();
                    }
                    break;
            }
        /*else {
            switch (httpResultCode) {
                case 200:
                    Toast.makeText(this, R.string.guest_info_removed, Toast.LENGTH_LONG).show();
                    break;
                default:        // response code will be 500 here
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.error)
                            .setMessage(R.string.server_error_500)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .create()
                            .show();
                    break;
            }
        }*/
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_remove_guest_data) {
            startRemoveGuestDataAsync();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startRemoveGuestDataAsync() {
        JsonObject json = new JsonObject();

        json.addProperty("udid", Common.getAndroidId(this));

        if (deleteGuestData == null || deleteGuestData.getStatus().equals(AsyncTask.Status.FINISHED)) {
            deleteGuestData = new ConnectToServer(this, true, CTSC_CLEAR_GUEST);
            deleteGuestData.delegate = this;
        }
        deleteGuestData.execute(ServerUrlConstants.REMOVE_GUEST_INFO_SERVLET, "POST", json.toString());
    }
    */


    @Override
    public void processComplete(String regId) {
        this.regId = regId;
    }

    @Override
    protected void onDestroy() {
        if (guestAsync != null)
            guestAsync.cancel(true);
        super.onDestroy();
    }


    private void startNextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

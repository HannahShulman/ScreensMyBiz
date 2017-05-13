package com.cliqdbase.app._activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cliqdbase.app.general.Common;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.device.RegIdAsyncResponse;
import com.cliqdbase.app.async.device.RegistrationIdExtractor;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;
import com.cliqdbase.app.general.Logout;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.general.Alert;
import com.cliqdbase.app.constants.AppConstants;
import com.cliqdbase.app.general.InputValidation;
import com.cliqdbase.app.general.PasswordHashing;
import com.google.gson.stream.JsonReader;


@SuppressWarnings("deprecation")
public class LoginActivity extends FragmentActivity implements AsyncResponse_Server, RegIdAsyncResponse, View.OnClickListener, View.OnFocusChangeListener {
    private Drawable error_icon;

    // Facebook login variables
    CallbackManager facebookCallbackManager;

    // Connect To Server Codes
    private final int CTSC_LOGIN = 0;
    private final int CTSC_LOGIN_FACEBOOK = 1;


    // CGM registration Id variable
    private String regId;
    private RegistrationIdExtractor regIdAsync;



    private EditText email_et;


    /**
     * Called upon creation of the LoginActivity.
     * Configures the necessary listeners and event handlers.
     * @param savedInstanceState The instance state this activity receives upon creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        regIdAsync = new RegistrationIdExtractor(LoginActivity.this, this);
        regIdAsync.execute();

        FacebookSdk.sdkInitialize(getApplicationContext());
        facebookCallbackManager = CallbackManager.Factory.create();
        Logout.facebookLogout(LoginActivity.this);

        setContentView(R.layout.activity_login);

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");

        LoginButton facebook_button = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook_button.setReadPermissions(permissions);
        facebook_button.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook", "onSuccess");
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                                Log.d("Facebook", "Response: " + graphResponse.toString());
                                try {
                                    String email = jsonObject.getString("email");
                                    String password = jsonObject.getString("id");
                                    //TODO check if facebook user is verified
                                    if (email == null || email.trim().length() == 0) {
                                        Toast.makeText(LoginActivity.this, getText(R.string.facebook_no_email_received) + " " + getText(R.string.facebook_login_failed), Toast.LENGTH_LONG).show();
                                        Logout.facebookLogout(LoginActivity.this);
                                    }

                                    do_login(email, password, CTSC_LOGIN_FACEBOOK);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, email");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "onCancel");
                Toast.makeText(LoginActivity.this, getText(R.string.facebook_no_data) + " " + getText(R.string.facebook_login_failed), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("Facebook", "onError " + e.toString());
                Toast.makeText(LoginActivity.this, getText(R.string.facebook_no_data) + " " + getText(R.string.facebook_login_failed), Toast.LENGTH_LONG).show();
            }
        });





        Intent intent = getIntent();        // Checking if when this activity was called, there are extras in the intent
        String email = intent.getStringExtra("email");      // If called from SignUpActivity alert, this extra will be here.


        // Edit texts decelerations
        EditText password_et = (EditText) findViewById(R.id.Login_Password);
        email_et = (EditText) findViewById(R.id.Login_Email_Address);
        if (email != null && email.length() > 0)
            email_et.setText(email);

        // Configuring the error icon.
        if (Build.VERSION.SDK_INT < 21)
            this.error_icon = getResources().getDrawable(R.drawable.input_wrong);
        else
            this.error_icon = getResources().getDrawable(R.drawable.input_wrong, null);

        if (this.error_icon != null)
            this.error_icon.setBounds(new Rect(0,0,error_icon.getIntrinsicWidth(),error_icon.getIntrinsicHeight()));

		/*
            Event handlers
		 */
        // Buttons' event handlers.
        Button login = (Button) findViewById(R.id.Sign_In_Button);
        login.setOnClickListener(this);

        ImageButton signUp = (ImageButton) findViewById(R.id.Sign_Up_Now_Button);       // The 'Sign Up Now' button
        signUp.setOnClickListener(this);

        ImageButton forgot_password = (ImageButton) findViewById(R.id.Forgot_Details);
        forgot_password.setOnClickListener(this);

        // EditTexts' event handlers.
        email_et.setOnFocusChangeListener(this);
        password_et.setOnFocusChangeListener(this);



        password_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {           // When the user clicks on "Done", we will try logging in
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (validate_login_input())
                        do_login();

                    handled = true;
                }
                return handled;
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Sign_In_Button:
                if (validate_login_input())
                    do_login();
                break;
            case R.id.Sign_Up_Now_Button:
                regIdAsync.cancel(true);
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();       // Closing this activity.
                break;
            case R.id.Forgot_Details:
                String email = email_et.getText().toString();
                View view = View.inflate(this, R.layout.dialog_forgot_password, null);

                final EditText reset_pass_email_et = (EditText) view.findViewById(R.id.reset_pass_email_et);
                reset_pass_email_et.setText(email);

                final AlertDialog alert = new AlertDialog.Builder(LoginActivity.this)
                        .setView(view)
                        .setPositiveButton(R.string.send, null)
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setTitle(R.string.reset_password_title)
                        .setCancelable(true)
                        .create();

                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button positive = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                        positive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String email = reset_pass_email_et.getText().toString();
                                JsonObject json = new JsonObject();
                                json.addProperty("email", email);

                                ConnectToServer async = new ConnectToServer(LoginActivity.this, true, 0);
                                async.delegate = new AsyncResponse_Server() {
                                    @Override
                                    public void onServerResponse(long taskCode, int httpResultCode, String data) {
                                        AlertDialog.Builder response_alert = new AlertDialog.Builder(LoginActivity.this);
                                        switch (httpResultCode) {
                                            case 200:
                                                response_alert.setTitle(R.string.success);
                                                response_alert.setMessage(R.string.reset_password_success);
                                                response_alert.setPositiveButton(R.string.continue_text, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        alert.dismiss();
                                                    }
                                                });
                                                break;
                                            case 404:
                                                response_alert.setTitle(R.string.error);
                                                response_alert.setMessage(R.string.reset_password_failure_404);
                                                response_alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) { }
                                                });
                                                break;
                                            case 500:
                                                response_alert.setTitle(R.string.error);
                                                response_alert.setMessage(R.string.server_error_500);
                                                response_alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        alert.dismiss();
                                                    }
                                                });
                                                break;
                                        }
                                        response_alert.create().show();
                                    }
                                };
                                async.execute(ServerUrlConstants.FORGOT_PASS_SERVLET, "POST", json.toString());
                            }
                        });
                    }
                });


                alert.show();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.Login_Email_Address:
                if (!hasFocus)
                    email_input_valid();
                break;
            case R.id.Login_Password:
                if (!hasFocus)
                    password_input_valid();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Validates all the input of the user. Must be called before logging in.
     * @return true if the input is valid. false otherwise.
     */
    public boolean validate_login_input() {
		/* We configure a variable to each of the fields instead of calling the functions in the return.
		 * This is because we want to set the error to all of the non-valid editTexts, and not just the first.
		 * If we had put the functions in the return, the return will call them until one of the functions will return false.
		 * Since the return is computed by AND it is enough for one of the functions to return false, in order for the whole AND statement to return false.
		 * Therefore, he will not execute the rest of the functions. and only the first non-valid editText will have the error.
		 */
        boolean email_valid = email_input_valid();
        boolean password_valid = password_input_valid();

        // The function will return true if and only if all the sub-validation-methods will return true
        return email_valid && password_valid;
    }


    /**
     * After the input had been validated,
     * this function configures the parameters we need to send to the login servlet in the server.
     * After the configuration had been completed, we will call an AsyncTask to connect to the server.
     *
     * This method is only for Cliqdbase login. NOT for facebook login
     *
     * This method calls {@link #do_login(String, String, int)} with the supplied email and password
     */
    public void do_login() {
        //Retrieving the email editText and the email itself.
        EditText email_et = (EditText) findViewById(R.id.Login_Email_Address);
        String email = email_et.getText().toString();

        //Retrieving the password editText and the password itself.
        EditText psw_et = (EditText) findViewById(R.id.Login_Password);
        String password = psw_et.getText().toString();

        do_login(email, password, CTSC_LOGIN);

    }

    /**
     * Logs the user into our system.
     * Used for both facebook and cliqdbase logins.
     * @param email       The user's email
     * @param password    The user's non-hashed password. (for facebook this is the user's id - also not hashed)
     */
    public void do_login(String email, String password, int requestCode) {
        String url;
        if (requestCode == CTSC_LOGIN_FACEBOOK)
            url = ServerUrlConstants.FACEBOOK_LOGIN_URL;
        else
            url = ServerUrlConstants.CLIQDBASE_LOGIN_URL;

        JsonObject jsonParams = new JsonObject();

        password = PasswordHashing.hashPass(password);
        String device_data = Common.getDeviceDesc();

        jsonParams.addProperty("email", email);
        jsonParams.addProperty("password", password);
        jsonParams.addProperty("regId", this.regId);
        jsonParams.addProperty("deviceCode", 1);
        jsonParams.addProperty("deviceDesc", device_data);

        ConnectToServer async = new ConnectToServer(LoginActivity.this, true, requestCode);
        async.delegate = this;
        async.execute(url, "POST", jsonParams.toString());

    }



    /**
     * Checks the email address input field and checks for input errors.
     * If there are errors, the function will add an error to the edit text and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     * @return true if there aren't any errors in the email field. false otherwise.
     */
    boolean email_input_valid() {
        EditText email_et = (EditText) findViewById(R.id.Login_Email_Address);
        String email = email_et.getText().toString();

        if (!InputValidation.validateEmail(email)) {
            email_et.setBackgroundResource(R.drawable.edit_text_box_error);     // Changing the edit text view to error view
            email_et.setError("Please enter a valid email address", error_icon);
            return false;
        }
        else {
            email_et.setBackgroundResource(R.drawable.edit_text_box);           // Setting the edit text box to the original non-error view
            email_et.setError(null);            // Removing the error message
            return true;
        }
    }


    /**
     * Checks the password input field and checks for input errors.
     * If there are errors, the function will add an error to the edit text and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     * @return true if there aren't any errors in the password field. false otherwise.
     */
    boolean password_input_valid() {
        EditText password_et = (EditText) findViewById(R.id.Login_Password);
        String password = password_et.getText().toString();

        boolean valid = InputValidation.validPassword(password);

        if (!valid) {        // Checking the validity of the password
            password_et.setBackgroundResource(R.drawable.edit_text_box_error);       // Changing the edit text view to error view
            password_et.setError("Password must contain at least " + AppConstants.PASSWORD_MINIMUM_LENGTH + " characters", error_icon);
            return false;
        }
        else {
            password_et.setBackgroundResource(R.drawable.edit_text_box);             // Setting the edit text box to the original non-error view
            password_et.setError(null);
            return true;
        }
    }


    @Override
    public void onServerResponse(long taskCode, int httpResCode, String output) {
        boolean logoutFromFacebook = false;
        if (taskCode == CTSC_LOGIN_FACEBOOK && httpResCode != 200)
            logoutFromFacebook = true;


        switch ((int)taskCode) {
            case CTSC_LOGIN:
            case CTSC_LOGIN_FACEBOOK:
                switch (httpResCode) {
                    case 200:             // Login completed successfully
                        Common.setLoggedInUsingFacebook(this, (taskCode != CTSC_LOGIN));
                        Common.setLoggedInAsGuest(this, false);

                        SharedPreferences prefs = LoginActivity.this.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        JsonReader reader = new JsonReader(new StringReader(output));
                        long userId = -1;
                        int badAttempts = -1,
                                maxBadAttempts = -1;
                        Boolean login = null;

                        try {
                            reader.beginObject();
                            while (reader.hasNext()) {
                                switch (reader.nextName()) {
                                    case "login":
                                        login = reader.nextBoolean();
                                        break;
                                    case "user_id":
                                        userId = reader.nextLong();
                                        break;
                                    case "badAttempts":
                                        badAttempts = reader.nextInt();
                                        break;
                                    case "maxBadAttempts":
                                        maxBadAttempts = reader.nextInt();
                                        break;
                                    default:
                                        reader.skipValue();
                                        break;
                                }
                            }
                        } catch (IOException e) { /* Ignored */ }
                        finally {
                            try { reader.close(); } catch (IOException e) { /* Ignored */ }
                        }

                        if (login == null) {
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.error)
                                    .setMessage(R.string.server_error_500)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setCancelable(true)
                                    .create()
                                    .show();
                        }
                        else if (login) {
                            editor.putLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_USER_ID, userId);
                            editor.apply();

                            startNextActivity();
                        }
                        else {
                            new Alert(this).showAlertDialog(getText(R.string.login_error_title).toString(), getText(R.string.login_error_text).toString());
                            if (badAttempts != -1 && maxBadAttempts != -1)
                                Toast.makeText(this, "Attempt " + badAttempts + "/" + maxBadAttempts, Toast.LENGTH_LONG).show();
                        }


                        break;
                    case 423:
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.login_banned_title)
                                .setMessage(R.string.login_banned_message)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Do nothing
                                    }
                                })
                                .setCancelable(true)
                                .create()
                                .show();
                        break;
                    default:
                        new Alert(LoginActivity.this).showAlertDialog(getText(R.string.unknown_server_error_title).toString() + " (" + httpResCode +")",
                                getText(R.string.unknown_server_error_text).toString());
                        break;
                }
                break;
        }

        if (logoutFromFacebook)
            Logout.facebookLogout(LoginActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Starts the next activity.
     */
    private void startNextActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    @Override
    public void processComplete(String regId) {
        this.regId = regId;
    }

    @Override
    public void onBackPressed() {
        if (this.regIdAsync != null)
            this.regIdAsync.cancel(true);
        startActivity(new Intent(this, WelcomeActivity.class));        // Starting the welcome Activity
        finish();
    }
}



/* Finding the keyHash for facebook
try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.cliqdbase.app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (Exception e) {}
*/
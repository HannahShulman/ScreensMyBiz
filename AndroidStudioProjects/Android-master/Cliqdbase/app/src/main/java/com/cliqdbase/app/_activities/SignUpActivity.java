package com.cliqdbase.app._activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.cliqdbase.app.general.Common;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.device.RegIdAsyncResponse;
import com.cliqdbase.app.async.device.RegistrationIdExtractor;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;
import com.cliqdbase.app.general.Alert;
import com.cliqdbase.app.constants.AppConstants;
import com.cliqdbase.app.general.Dialogs;
import com.cliqdbase.app.general.InputValidation;
import com.cliqdbase.app.general.Logout;
import com.cliqdbase.app.general.PasswordHashing;


import com.cliqdbase.app.server_model.City;

@SuppressWarnings("deprecation")
public class SignUpActivity extends FragmentActivity implements AsyncResponse_Server, RegIdAsyncResponse, View.OnClickListener, View.OnFocusChangeListener {

    // Facebook login variables
    private CallbackManager facebookCallbackManager;
    private boolean loggedInUsingFacebook;

    // Date picker variables
    private CalendarDatePickerDialog datePicker;
    private int onDateSetCounter;       // On android 4.x, when a user clicks on 'OK' in a DatePickerDialog, the function onDateSet is being called twice.
                                        // We will use this counter to make sure we are calling the function only once.
                                        // In android 5.0 and above this issue had been fixed.

    private Drawable error_icon;

    private ArrayList<City> cities;          // The list of cities to be displayed in the city AutoCompleteTextView
    private ArrayList<String> citiesList;    // The string to be displayed in the city AutoCompleteTextView

    private int guestCityCode;  // The city code that the guest picked as guest. We will display the correct city name with the given code.

    private String regId;

    // CTSC - Connect To Server Codes:
    private final int CTSC_DOWNLOAD_CITY_LIST = 0;
    private final int CTSC_REGISTER = 1;
    private final int CTSC_GET_GUEST_INFO = 2;

    private RegistrationIdExtractor regIdAsync;


    private ConnectToServer getCitiesAsync;
    private ConnectToServer guestInfoDownloader;

    private boolean guestSigningUp;


    private EditText firstName_et;
    private EditText lastName_et;
    private EditText birthday_et;
    private EditText email_et;
    private EditText password_et;
    private AutoCompleteTextView location_et;
    private RadioGroup gender_rg;
    private Button signUp;
    private ImageButton loginNowBtn;

    /**
     * This function is being called upon creation of the SignUp activity.
     * This function configures the datePickerDialog that will be shown upon touch of the date of birth input edit text.
     * This function also configures the necessary event handlers and listeners for this activity.
     *
     * @param savedInstanceState The instance state this activity receives upon creation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Facebook sdk bullshit.
        FacebookSdk.sdkInitialize(getApplicationContext());
        facebookCallbackManager = CallbackManager.Factory.create();

        regIdAsync = new RegistrationIdExtractor(SignUpActivity.this, this);
        regIdAsync.execute();

        setContentView(R.layout.activity_sign_up);

        findViewsById();
        setViewsEventHandlers();

        guestSigningUp = getIntent().getBooleanExtra(IntentConstants.INTENT_EXTRA_GUEST_WANT_TO_SIGN_UP, false);

        guestCityCode = -1;
        guestInfoDownloader = null;

        if (guestSigningUp) {
            JsonObject params = new JsonObject();
            params.addProperty("udid", Common.getAndroidId(this));

            guestInfoDownloader = new ConnectToServer(this, true, CTSC_GET_GUEST_INFO);
            guestInfoDownloader.delegate = this;
            guestInfoDownloader.execute(ServerUrlConstants.GET_GUEST_INFO_FOR_SIGN_UP, "POST", params.toString());
        }


        this.cities = null;
        this.citiesList = new ArrayList<>();

        // Configuring the DatePickerDialog
        this.datePicker = Dialogs.createBirthdayDatePickerDialog(birthday_et);


        // Sign up with facebook
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");

        LoginButton facebook_button = (LoginButton) findViewById(R.id.facebook_sign_up_button);
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
                                loggedInUsingFacebook = true;
                                try {
                                    final String email = jsonObject.getString("email");
                                    if (email == null || email.trim().length() == 0) {
                                        Toast.makeText(SignUpActivity.this, getText(R.string.facebook_no_email_received) + " " + getText(R.string.facebook_login_failed), Toast.LENGTH_LONG).show();
                                        Logout.facebookLogout(SignUpActivity.this);
                                    }
                                    final String password = jsonObject.getString("id");   // The id is unique for each user in each facebook application. Meaning: one user will have two different ids for two different facebook apps. This id is long enough for brute force attack to take forever.
                                    final String firstName = jsonObject.getString("first_name");
                                    final String lastName = jsonObject.getString("last_name");
                                    String gender = jsonObject.getString("gender");
                                    final char fGender;

                                    if (gender == null) {
                                        fGender = 'O';
                                    } else {
                                        fGender = gender.toUpperCase().charAt(0);
                                    }

                                    // Creating the facebook birthday date picker dialog

                                    onDateSetCounter = 0;       // Initializing the value of this counter.

                                    Calendar cal = Calendar.getInstance();

                                    CalendarDatePickerDialog datePickerDialog = CalendarDatePickerDialog.newInstance(
                                            new CalendarDatePickerDialog.OnDateSetListener() {
                                                @Override
                                                public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dateOfMonth) {
                                                    if (++onDateSetCounter > 1)         // Making sure that this function will be called only once.
                                                        return;

                                                    // Finding out the picked date.
                                                    monthOfYear++;         // Months are from 0 - 11, now they will be from 1 - 12. (the mod is because the date picker assigned January the number 12 instead of 0)
                                                    final GregorianCalendar datePicked = new GregorianCalendar(year, monthOfYear, dateOfMonth);

                                                    // Signing up using the facebook url.
                                                    do_signUp(ServerUrlConstants.FACEBOOK_SIGN_UP_URL, email, password, firstName, lastName, String.valueOf(fGender), datePicked.getTime().getTime(), null);
                                                }
                                            },
                                            cal.get(Calendar.YEAR) - 18,
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH));


                                    datePickerDialog.setYearRange(cal.get(Calendar.YEAR) - 100, cal.get(Calendar.YEAR));
                                    datePickerDialog.setCancelable(false);
                                    datePickerDialog.show(getSupportFragmentManager(), "Birthday Dialog");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, last_name, email, gender");     // TODO add "birthday_et" here after facebook enables it.
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "onCancel");
                Toast.makeText(SignUpActivity.this, getText(R.string.facebook_no_data) + " " + getText(R.string.facebook_sign_up_failed), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("Facebook", "onError " + e.toString());
                Toast.makeText(SignUpActivity.this, getText(R.string.facebook_no_data) + " " + getText(R.string.facebook_sign_up_failed), Toast.LENGTH_LONG).show();
            }
        });

        getCitiesAsync = null;

        downloadCitiesList();

        // Configures the error icon that will be shown when there is an error in one of the input fields.
        if (Build.VERSION.SDK_INT < 21)
            this.error_icon = getResources().getDrawable(R.drawable.input_wrong);
        else
            this.error_icon = getResources().getDrawable(R.drawable.input_wrong, null);

        if (this.error_icon != null)
            this.error_icon.setBounds(new Rect(0, 0, error_icon.getIntrinsicWidth(), error_icon.getIntrinsicHeight()));
    }

    private void findViewsById() {
        location_et = (AutoCompleteTextView) findViewById(R.id.sign_up_location);
        birthday_et = (EditText) findViewById(R.id.DateOfBirth);
        email_et = (EditText) findViewById(R.id.Sign_Up_Email_Address);
        password_et = (EditText) findViewById(R.id.Sign_Up_Password);
        firstName_et = (EditText) findViewById(R.id.First_Name);
        lastName_et = (EditText) findViewById(R.id.Last_Name);

        gender_rg = (RadioGroup) findViewById(R.id.genderRadioGroup);

        signUp = (Button) findViewById(R.id.Sign_Up_Button);
        loginNowBtn = (ImageButton) findViewById(R.id.login_now_button);
    }

    private void setViewsEventHandlers() {
        location_et.setOnFocusChangeListener(this);
        birthday_et.setOnClickListener(this);
        birthday_et.setOnFocusChangeListener(this);
        birthday_et.setKeyListener(null);

        email_et.setOnFocusChangeListener(this);
        password_et.setOnFocusChangeListener(this);
        firstName_et.setOnFocusChangeListener(this);
        lastName_et.setOnFocusChangeListener(this);

        signUp.setOnClickListener(this);
        loginNowBtn.setOnClickListener(this);

        gender_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton female_gender = (RadioButton) findViewById(R.id.female_radioButton);
                female_gender.setError(null);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.DateOfBirth:
                this.datePicker.show(getSupportFragmentManager(), "Birthday");
                break;
            case R.id.Sign_Up_Button:
                if (validate_input())
                    do_signUp();
                break;
            case R.id.login_now_button:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.sign_up_location:
                if (!hasFocus)
                    city_input_valid();
                break;
            case R.id.DateOfBirth:
                if (hasFocus)               // If the focus is set to this edit text (by pressing Enter on the editText above this) the dialog should open
                    datePicker.show(getSupportFragmentManager(), "Birthday");
                else
                    date_of_birth_input_valid();
                break;
            case R.id.Sign_Up_Email_Address:
                if (!hasFocus)
                    email_input_valid();
                break;
            case R.id.Sign_Up_Password:
                if (!hasFocus)
                    password_input_valid();
                break;
            case R.id.First_Name:
            case R.id.Last_Name:
                if (!hasFocus)
                    name_input_valid(v.getId());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (this.regIdAsync != null)
            this.regIdAsync.cancel(true);
        startActivity(new Intent(SignUpActivity.this, WelcomeActivity.class));        // Starting the welcome Activity
        finish();
    }


    /**
     * Validates all the input of the user. Must be called before signing up.
     *
     * @return true if the input is valid. false otherwise.
     */
    private boolean validate_input() {
        /* We configure a variable to each of the fields instead of calling the functions in the return.
         * This is because we want to set the error to all of the non-valid editTexts, and not just the first.
         * If we had put the functions in the return, the return will call them until one of the functions will return false.
         * Since the return is computed by AND it is enough for one of the functions to return false, in order for the whole AND statement to return false.
         * Therefore, he will not execute the rest of the functions. and only the first non-valid editText will have the error.
         */
        boolean email_valid = email_input_valid();
        boolean password_valid = password_input_valid();
        boolean first_name_valid = name_input_valid(R.id.First_Name);
        boolean last_name_valid = name_input_valid(R.id.Last_Name);
        boolean date_of_birth_valid = date_of_birth_input_valid();
        boolean gender_valid = gender_input_valid();
        boolean city_valid = city_input_valid();

        // The function will return true if and only if all the sub-validation-methods will return true
        return email_valid
                && password_valid
                && first_name_valid
                && last_name_valid
                && date_of_birth_valid
                && gender_valid
                && city_valid;
    }


    /**
     * After the input had been validated,
     * this function configures the parameters we need to send to the register servlet in the server.
     * After the configuration had been completed, we will call an AsyncTask to connect to the server.
     */
    private void do_signUp() {
        // Retrieving the necessary fields
        EditText email_et = (EditText) findViewById(R.id.Sign_Up_Email_Address);
        EditText psw_et = (EditText) findViewById(R.id.Sign_Up_Password);
        EditText firstName_et = (EditText) findViewById(R.id.First_Name);
        EditText lastName_et = (EditText) findViewById(R.id.Last_Name);
        EditText dateOfBirth_et = (EditText) findViewById(R.id.DateOfBirth);
        AutoCompleteTextView city_et = (AutoCompleteTextView) findViewById(R.id.sign_up_location);
        RadioGroup gender_rg = (RadioGroup) findViewById(R.id.genderRadioGroup);
        RadioButton genderSelected = (RadioButton) findViewById(gender_rg.getCheckedRadioButtonId());

        String email = email_et.getText().toString();
        String password = psw_et.getText().toString();
        String firstName = firstName_et.getText().toString();
        String lastName = lastName_et.getText().toString();
        String gender = genderSelected.getText().toString();
        if (gender.equals("Male"))
            gender = "M";
        else
            gender = "F";

        String dateOfBirth = dateOfBirth_et.getText().toString();
        String city = city_et.getText().toString();

        long dateOfBirth_milliseconds = getNumberOfMillisecondsSince(dateOfBirth);          // returns 0 if the date is not valid


        this.loggedInUsingFacebook = false;
        do_signUp(ServerUrlConstants.CLIQDBASE_SIGN_UP_URL, email, password, firstName, lastName, gender, dateOfBirth_milliseconds, city);
    }


    /**
     * Signs the user up to our system
     * @param url                        The registration url (different for regular/facebook/twitter/google)
     * @param email                      The email to register.
     * @param password                   The non-hashed password. We will hash it in this function.
     * @param firstName                  The user's first name.
     * @param lastName                   The user's last name.
     * @param gender                     The user's gender. Can be either "M" or "F".
     * @param dateOfBirthMilliSeconds    The milliseconds since the user's date of birth
     * @param city                       The main city of the user. Can be null.
     */
    private void do_signUp(String url, String email, String password, String firstName, String lastName, String gender, long dateOfBirthMilliSeconds, String city) {
        JsonObject jsonParams = new JsonObject();

        password = PasswordHashing.hashPass(password);          // Encoding the password using SHA512 algorithm.

        String device_data = Common.getDeviceDesc();

        // Configuring the parameters to send to the servlet.

        jsonParams.addProperty("email", email);
        jsonParams.addProperty("password", password);
        jsonParams.addProperty("firstName", firstName);
        jsonParams.addProperty("lastName", lastName);
        jsonParams.addProperty("gender", gender);
        jsonParams.addProperty("dateOfBirth", dateOfBirthMilliSeconds);
        jsonParams.addProperty("regId", this.regId);
        jsonParams.addProperty("deviceCode", 1);
        jsonParams.addProperty("deviceDesc", device_data);

        String androidId = Common.getAndroidId(this);
        if (androidId != null && !androidId.isEmpty())
            jsonParams.addProperty("udid", androidId);

        if (!(city == null || city.isEmpty()))
            jsonParams.addProperty("cityCode", getCityCode(city));


        ConnectToServer async = new ConnectToServer(SignUpActivity.this, true, CTSC_REGISTER);
        async.delegate = this;
        async.execute(url, "POST", jsonParams.toString());
    }



    /**
     * Checks the email address input field and checks for input errors.
     * If there are errors, the function will add an error to the edit text and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     *
     * @return true if there aren't any errors in the email field. false otherwise.
     */
    private boolean email_input_valid() {
        EditText email_et = (EditText) findViewById(R.id.Sign_Up_Email_Address);
        String email = email_et.getText().toString();
        if (!InputValidation.validateEmail(email)) {
            email_et.setBackgroundResource(R.drawable.edit_text_box_error);     // Changing the edit text view to error view
            email_et.setError(getText(R.string.email_address_error), error_icon);
            return false;
        } else {
            email_et.setBackgroundResource(R.drawable.edit_text_box);           // Setting the edit text box to the original non-error view
            email_et.setError(null);            // Removing the error message
            return true;
        }
    }


    /**
     * Checks the password input field and checks for input errors.
     * If there are errors, the function will add an error to the edit text and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     *
     * @return true if there aren't any errors in the password field. false otherwise.
     */
    private boolean password_input_valid() {
        String password = password_et.getText().toString();

        boolean valid = InputValidation.validPassword(password);

        if (!valid) {        // Checking the validity of the password
            password_et.setBackgroundResource(R.drawable.edit_text_box_error);       // Changing the edit text view to error view
            password_et.setError(getText(R.string.password_error) + Integer.toString(AppConstants.PASSWORD_MINIMUM_LENGTH), error_icon);
            return false;
        } else {
            password_et.setBackgroundResource(R.drawable.edit_text_box);             // Setting the edit text box to the original non-error view
            password_et.setError(null);
            return true;
        }
    }

    /**
     * Checks the name input field and checks for input errors.
     * If there are errors, the function will add an error to the edit text and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     *
     * @param name_editText_id The id of the EditText (there are two EditTexts for name - first and last)
     * @return true if there aren't any errors in the given EditText name field. false otherwise.
     */
    private boolean name_input_valid(int name_editText_id) {
        EditText name_et = (EditText) findViewById(name_editText_id);
        String name = name_et.getText().toString();

        boolean valid = InputValidation.validName(name);

        if (!valid) {
            name_et.setBackgroundResource(R.drawable.edit_text_box_error);       // Changing the edit text view to error view
            name_et.setError(getText(R.string.name_error), error_icon);
            return false;
        } else {
            name_et.setBackgroundResource(R.drawable.edit_text_box);             // Setting the edit text box to the original non-error view
            name_et.setError(null);
            return true;
        }
    }

    /**
     * Checks the date of birth input field and checks for input errors.
     * If there are errors, the function will add an error to the edit text and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     *
     * @return true if there aren't any errors in the date of birth field. false otherwise.
     */
    private boolean date_of_birth_input_valid() {
        String dateOfBirth = birthday_et.getText().toString();

        boolean valid = InputValidation.validateBirthday(dateOfBirth);

        if (valid) {
            birthday_et.setBackgroundResource(R.drawable.edit_text_box);             // Setting the edit text box to the original non-error view
            birthday_et.setError(null);
        }
        else {
            birthday_et.setBackgroundResource(R.drawable.edit_text_box_error);       // Changing the edit text view to error view
            birthday_et.setError(getText(R.string.date_error), error_icon);
        }
        return valid;
    }

    /**
     * Checks the gender radio button field and makes sure that a button was selected.
     * If there are errors, the function will add an error to the radio button and return false.
     * If there aren't any errors, the function will remove any existing errors and return true;
     *
     * @return true if one of the radio buttons was checked, false otherwise.
     */
    private boolean gender_input_valid() {
        RadioButton female_gender = (RadioButton) findViewById(R.id.female_radioButton);

        if (gender_rg.getCheckedRadioButtonId() == -1) {                    // No button was selected
            female_gender.setError(getText(R.string.gender_error), error_icon);
            return false;
        } else {
            female_gender.setError(null);
            return true;
        }
    }

    private boolean city_input_valid() {
        String cityText = location_et.getText().toString();

        if (cityText.equals(""))
            return true;

        if (this.citiesList.contains(cityText)) {
            location_et.setError(null);
            return true;
        }
        else {
            location_et.setError(getText(R.string.city_error), error_icon);
            return false;
        }
    }


    /**
     * @param date a string representing a date, using UTC timezone.
     * @return If the date string is valid, the number of milliseconds since that date. Else, 0.
     * @see java.util.Date#getTime();
     */
    private long getNumberOfMillisecondsSince(String date) {
        if (date == null || date.length() == 0)
            return 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date d = sdf.parse(date);       // Parsing the string as date
            return d.getTime();             // Returning the number of milliseconds

        } catch (ParseException e) {
            return 0;           // We won't actually get here, because before calling this function we validate the input
        }
    }


    @Override
    public void onServerResponse(long taskCode, int httpResCode, String data) {
        switch ((int)taskCode) {
            case CTSC_DOWNLOAD_CITY_LIST:
                if (httpResCode == 200) {
                    Gson gson = new Gson();
                    JsonReader reader = new JsonReader(new StringReader(data));
                    reader.setLenient(true);

                    Type citiesType = new TypeToken<ArrayList<City>>() {
                    }.getType();
                    cities = gson.fromJson(reader, citiesType);
                    if (cities != null) {
                        for (City c : cities)
                            citiesList.add(c.getDisplayName());
                    }
                    AutoCompleteTextView location_et = (AutoCompleteTextView) findViewById(R.id.sign_up_location);
                    ArrayAdapter<String> citiesAdapter = new ArrayAdapter<>(SignUpActivity.this, android.R.layout.simple_list_item_1, citiesList);
                    location_et.setAdapter(citiesAdapter);

                    setCityNameFromCodeInEditTextForGuest();
                }
                else {
                    Toast.makeText(this, "Couldn't download city list, Please try again later.", Toast.LENGTH_LONG).show();
                }
                break;
            case CTSC_REGISTER:
                boolean logoutFromFacebook = false;
                if (this.loggedInUsingFacebook && httpResCode != 200)
                    logoutFromFacebook = true;

                switch (httpResCode) {
                    case 200:                         // Registration completed successfully
                        SharedPreferences prefs = SignUpActivity.this.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_FACEBOOK_LOGIN, this.loggedInUsingFacebook);

                        JsonReader jsonReader = new JsonReader(new StringReader(data));
                        long userId = -1;
                        try {
                            boolean stopWhile = false;
                            jsonReader.beginObject();
                            while (!stopWhile && jsonReader.hasNext()) {
                                switch (jsonReader.nextName()) {
                                    case "user_id":
                                        stopWhile = true;
                                        userId = jsonReader.nextLong();
                                        break;
                                    default:
                                        jsonReader.skipValue();
                                        break;
                                }
                            }
                            jsonReader.close();
                        } catch (IOException e) { /* Ignored */ }



                        editor.putLong(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_LOGGED_IN_USER_ID, userId);
                        editor.apply();

                        AlertDialog dialog = new AlertDialog.Builder(SignUpActivity.this)
                                .setTitle("Confirmation email sent.")
                                .setMessage("A confirmation email has been sent to the email address you supplied.\n" +
                                        "To authorize your account, please follow the instruction in the mail")
                                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(SignUpActivity.this, ChangeProfilePictureActivity.class);
                                        intent.putExtra(IntentConstants.INTENT_EXTRA_SIGNUP, true);
                                        startActivity(intent);
                                        SignUpActivity.this.finish();
                                    }
                                })
                                .setCancelable(false)
                                .create();

                        dialog.show();

                        break;
                    case 400:                        // Missing fields
                        new Alert(SignUpActivity.this).showAlertDialog(getText(R.string.missing_fields_title).toString(), getText(R.string.missing_fields_text).toString());
                        break;
                    case 409:                      // User already exists
                        Alert alert = new Alert(SignUpActivity.this);
                        AlertDialog.Builder alertBuilder = alert.buildAlert(getText(R.string.user_exists_title).toString(), getText(R.string.user_exists_text).toString());
                        alertBuilder.setPositiveButton(R.string.user_exists_alert_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText email_et = (EditText) findViewById(R.id.Sign_Up_Email_Address);
                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                intent.putExtra("email", email_et.getText().toString());
                                startActivity(intent);
                                SignUpActivity.this.finish();
                            }
                        });
                        alertBuilder.show();
                        break;
                    default:
                        new Alert(SignUpActivity.this).showAlertDialog(getText(R.string.unknown_server_error_title).toString(), getText(R.string.unknown_server_error_text).toString());
                        break;
                }

                if (logoutFromFacebook)
                    Logout.facebookLogout(SignUpActivity.this);
                break;

            case CTSC_GET_GUEST_INFO:
                switch (httpResCode) {
                    case 200:
                        JsonReader jsonReader = new JsonReader(new StringReader(data));
                        try {
                            jsonReader.beginObject();
                            while (jsonReader.hasNext()) {
                                switch (jsonReader.nextName()) {
                                    case "firstName":
                                        firstName_et.setText(jsonReader.nextString());
                                        break;
                                    case "lastName":
                                        lastName_et.setText(jsonReader.nextString());
                                        break;
                                    case "dateOfBirth":
                                        long dateOfBirthTime = jsonReader.nextLong();
                                        Date birthDate = new Date(dateOfBirthTime);
                                        Dialogs.showPickedDateOnEditText(birthday_et, birthDate);
                                        break;
                                    case "cityCode":
                                        guestCityCode = jsonReader.nextInt();
                                        setCityNameFromCodeInEditTextForGuest();
                                        break;
                                    case "genderCode":
                                        String gender = jsonReader.nextString();
                                        char genderChar = gender.toLowerCase().charAt(0);
                                        switch (genderChar) {
                                            case 'm':
                                                gender_rg.check(R.id.filter_cliq_gender_male);
                                                break;
                                            case 'f':
                                                gender_rg.check(R.id.filter_cliq_gender_female);
                                                break;
                                            case 'o':
                                                gender_rg.check(R.id.filter_cliq_gender_other);
                                                break;
                                        }
                                        break;
                                    default:
                                        jsonReader.skipValue();
                                        break;
                                }
                            }
                            jsonReader.close();
                        } catch (IOException e) { /* Ignored */ }

                        break;
                    case 404:
                    case 500:
                        Toast.makeText(this, R.string.cant_download_guest_data, Toast.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    /**
     * This function will be called twice:
     *  1. After the getCitiesAsync is finished (if download is successful).
     *  2. After the guestInfoDownloader (if city code received from server).
     *
     * Only in the second time (once both of the async tasks have been completed), the function will extract the city code from the city code list, and put the city in the location edit text.
     */
    private void setCityNameFromCodeInEditTextForGuest() {
        if (guestInfoDownloader != null
                && guestInfoDownloader.getStatus().equals(AsyncTask.Status.FINISHED)
                && guestCityCode != -1
                && getCitiesAsync != null
                && getCitiesAsync.getStatus().equals(AsyncTask.Status.FINISHED)
                && cities != null) {
            for (City c : cities)
                if (c.getCityCode() == guestCityCode)
                    location_et.setText(c.getDisplayName());
        }

    }

    /**
     * Extracts the city code of the selected city in the location AutoCompleteTextView.
     * @param cityText    The displayed text in the location AutoCompleteTextView. (must be verified)
     * @return  The city code, or -1 if city not found.
     */
    private int getCityCode(String cityText) {
        for (City c : cities) {
            if (c.getDisplayName().equals(cityText))
                return c.getCityCode();
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void processComplete(String regId) {
        this.regId = regId;
    }

    @Override
    protected void onResume() {
        if ((getCitiesAsync != null && getCitiesAsync.getHttpResCode() == -1))
            downloadCitiesList();
        super.onResume();
    }

    private void downloadCitiesList() {
        if (getCitiesAsync == null || getCitiesAsync.getStatus().equals(AsyncTask.Status.FINISHED)) {
            getCitiesAsync = new ConnectToServer(SignUpActivity.this, false, CTSC_DOWNLOAD_CITY_LIST);     // No need to display a process dialog
            getCitiesAsync.delegate = this;
        }
        if (!getCitiesAsync.getStatus().equals(AsyncTask.Status.RUNNING))
            getCitiesAsync.execute(ServerUrlConstants.GET_CITIES_LIST, "GET");
    }
}

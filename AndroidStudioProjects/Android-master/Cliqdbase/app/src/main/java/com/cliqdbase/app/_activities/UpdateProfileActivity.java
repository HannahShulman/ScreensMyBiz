package com.cliqdbase.app._activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cliqdbase.app.general.Common;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.general.Dialogs;
import com.cliqdbase.app.general.InputValidation;
import com.cliqdbase.app.server_model.City;
import com.cliqdbase.app.server_model.UserProfile;

public class UpdateProfileActivity extends FragmentActivity implements View.OnClickListener, AsyncResponse_Server {

    private String firstName;
    private String lastName;
    private String city;
    private Integer cityCode;
    private String country;
    private long birthdayMillis;


    private EditText firstName_et;
    private EditText lastName_et;
    private EditText birthday_et;
    private AutoCompleteTextView location_et;


    private ArrayList<String> citiesTextList;
    private ArrayList<City> cities;

    private CalendarDatePickerDialog datePicker;


    // Codes for connect to server com.cliqdbase.app.async tasks
    private final int CTSC_CITY_LIST = 0;
    private final int CTSC_GET_PROFILE = 1;
    private final int CTSC_UPDATE_PROFILE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);


        // Downloading the city list and setting the list adapter.
        this.cities = null;
        this.citiesTextList = new ArrayList<>();
        downloadCityList();

        // Setting the date dialog for the birthday edit text
        this.datePicker = Dialogs.createBirthdayDatePickerDialog(birthday_et);


        Button updateButton = (Button) findViewById(R.id.edit_profile_update_profile_button);
        updateButton.setOnClickListener(this);


        Button changePass_button = (Button) findViewById(R.id.change_password_button);
        // Facebook users can't change their password
        if (Common.isLoggedInUsingFacebook(this) || Common.isLoggedInAsGuest(this))
            changePass_button.setVisibility(View.INVISIBLE);
        else {
            changePass_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(UpdateProfileActivity.this, ChangePasswordActivity.class));
                }
            });
        }

        this.firstName_et = (EditText) findViewById(R.id.edit_profile_first_name);
        this.lastName_et = (EditText) findViewById(R.id.edit_profile_last_name);
        this.birthday_et = (EditText) findViewById(R.id.edit_profile_date_of_birth);
        this.location_et = (AutoCompleteTextView) findViewById(R.id.edit_profile_location);

        this.birthday_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show(getSupportFragmentManager(), "Birthday");
            }
        });

        this.birthday_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    datePicker.show(getSupportFragmentManager(), "Birthday");
            }
        });


        Intent parent = getIntent();
        Bundle extras = parent.getExtras();
        if (extras == null) {
            downloadProfileInfo();
        }
        else {
            this.firstName = extras.getString("firstName", null);
            this.lastName = extras.getString("lastName", null);
            this.city = extras.getString("city", null);
            this.cityCode = extras.getInt("cityCode", -1);
            this.country = extras.getString("country", null);
            this.birthdayMillis = extras.getLong("birthday", -1);


            if (this.firstName == null || this.lastName == null || this.birthdayMillis == -1)
                downloadProfileInfo();
            else
                putDataOnEditTexts();
        }
    }



    private boolean validateFields() {
        String birthday = this.birthday_et.getText().toString();
        String fName = this.firstName_et.getText().toString();
        String lName = this.lastName_et.getText().toString();
        String location = this.location_et.getText().toString();

        boolean birthdayValid = InputValidation.validateBirthday(birthday);
        boolean fNameValid = InputValidation.validName(fName);
        boolean lNameValid = InputValidation.validName(lName);
        boolean cityValid = location.length() == 0 || this.citiesTextList.contains(location);


        this.birthday_et.setError((birthdayValid ? null : getText(R.string.date_error)));
        this.firstName_et.setError((fNameValid ? null : getText(R.string.name_error)));
        this.lastName_et.setError((lNameValid ? null : getText(R.string.name_error)));
        this.location_et.setError((cityValid ? null : getText(R.string.city_error)));


        return birthdayValid &&
                fNameValid &&
                lNameValid &&
                cityValid;
    }


    private void downloadCityList() {
        // Downloading the city list
        ConnectToServer downloadCityList = new ConnectToServer(UpdateProfileActivity.this, false, CTSC_CITY_LIST);     // No need to display a process dialog
        downloadCityList.delegate = this;
        downloadCityList.execute(ServerUrlConstants.GET_CITIES_LIST, "GET");
    }



    /**
     * Sets the value of the edit texts in this activity to the parameters of this class.
     * This function assumes that there are values for:
     * @link #firstName
     * @link #lastName
     * @link #cityCode
     * @link #city
     * @link #country
     * @link #birthdayMillis
     */
    private void putDataOnEditTexts() {
        this.firstName_et.setText(this.firstName);
        this.lastName_et.setText(this.lastName);

        if (this.city != null && this.country != null && this.cityCode != -1) {
            City city = new City(this.cityCode, this.city, this.country);
            this.location_et.setText(city.getDisplayName());
        }

        Date date = new Date(this.birthdayMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String birthday = sdf.format(date);
        this.birthday_et.setText(birthday);
    }



    /**
     * Downloads the user's profile.
     * This is called only if the ProfileActivity didn't complete downloading the profile information and sending it in the intent.
     * When done, calls {@link #putDataOnEditTexts()} to set the edit texts' value.
     */
    private void downloadProfileInfo() {
        ConnectToServer fetchProfileInfo = new ConnectToServer(this, true, CTSC_GET_PROFILE);
        fetchProfileInfo.delegate = this;
        fetchProfileInfo.execute(ServerUrlConstants.GET_MY_USER_PROFILE, "GET");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edit_profile_update_profile_button:
                if (validateFields()) {
                    String params = createServletParameters();
                    ConnectToServer updateProfile = new ConnectToServer(UpdateProfileActivity.this, true, CTSC_UPDATE_PROFILE);
                    updateProfile.delegate = this;
                    updateProfile.execute(ServerUrlConstants.UPDATE_USER_PROFILE, "POST", params);
                }
                break;
        }
    }

    /**
     * Creates the string to send to the servlet. This string is the parameters that the servlet will receive.
     *
     * If a user changes a value in this activity's edit text, we will send the new value.
     * If not, we will not add the parameter in the string.
     *
     * @return  The String of the servlet parameters
     */
    private String createServletParameters() {
        JsonObject jsonParams = new JsonObject();

        String firstName_new = this.firstName_et.getText().toString();
        String lastName_new = this.lastName_et.getText().toString();
        String location_new = this.location_et.getText().toString();
        String birthday_new = this.birthday_et.getText().toString();

        if (!firstName_new.equals(this.firstName))
            jsonParams.addProperty("firstName", firstName_new);

        if (!lastName_new.equals(this.lastName))
            jsonParams.addProperty("lastName", lastName_new);

        int cityCode_new = getCityCode(location_new);
        if (this.cityCode != cityCode_new)      // if we want to delete the user's city data, we will send -1.
            jsonParams.addProperty("cityCode", cityCode_new);

        long birthdayMillis_new = getNumberOfMillisecondsSince(birthday_new);
        if (birthdayMillis_new != this.birthdayMillis)
            jsonParams.addProperty("dateOfBirth", birthdayMillis_new);


        Log.d("yuval", "birthday millis = " + birthdayMillis_new);

        return jsonParams.toString();
    }


    /**
     * Extracts the city code of the selected city in the location AutoCompleteTextView.
     *
     * This function is called after validation of the city edit text value, and therefore the text must be empty or a valid city.
     *
     * @param cityText    The displayed text in the location AutoCompleteTextView. (must be verified)
     * @return  The city code, or -1 if city not found or empty.
     */
    private int getCityCode(String cityText) {
        if (cityText.trim().length() != 0) {
            for (City c : cities) {
                if (c.getDisplayName().equals(cityText))
                    return c.getCityCode();
            }
        }
        return -1;
    }


    /**
     * @param date a string representing a date.
     * @return If the date string is valid, the number of milliseconds since that date. Else, 0.
     * @see java.util.Date#getTime();
     */
    private long getNumberOfMillisecondsSince(String date) {
        if (date == null || date.isEmpty())
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
    public void onServerResponse(long taskCode, int httpResCode, String output) {
        switch((int)taskCode) {
            case CTSC_CITY_LIST:
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(output));
                reader.setLenient(true);

                Type citiesType = new TypeToken<ArrayList<City>>() {}.getType();
                cities = gson.fromJson(reader, citiesType);
                if (cities != null) {
                    for (City c : cities)
                        citiesTextList.add(c.getDisplayName());
                }
                ArrayAdapter<String> citiesAdapter = new ArrayAdapter<>(UpdateProfileActivity.this, android.R.layout.simple_list_item_1, citiesTextList);
                location_et.setAdapter(citiesAdapter);
                break;


            case CTSC_GET_PROFILE:
                UserProfile profile = UserProfile.getUserFromJson(output);
                if (profile == null) {
                    AlertDialog alert = new AlertDialog.Builder(UpdateProfileActivity.this)
                            .setMessage(R.string.cant_load_profile_info)
                            .setPositiveButton(R.string.close_alert, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    UpdateProfileActivity.this.finish();          // Closing this activity
                                }
                            })
                            .setCancelable(false)
                            .create();
                    alert.show();
                }
                else {
                    firstName = profile.getFirstName();
                    lastName = profile.getLastName();
                    city = profile.getCity();
                    country = profile.getCountry();
                    cityCode = profile.getCityCode();
                    birthdayMillis = profile.getBirthdayMillis();

                    putDataOnEditTexts();
                }
                break;


            case CTSC_UPDATE_PROFILE:
                if (httpResCode == 200) {
                    Intent intent = getIntent();
                    intent.putExtra(IntentConstants.INTENT_EXTRA_PROFILE_UPDATED, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else
                    Toast.makeText(this, R.string.update_profile_error, Toast.LENGTH_LONG).show();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        Intent intent = getIntent();
        intent.putExtra(IntentConstants.INTENT_EXTRA_PROFILE_UPDATED, false);
        setResult(RESULT_CANCELED, intent);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

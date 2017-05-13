package com.cliqdbase.app.server_model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;

/**
 * @author Yuval
 * This class will hold the data that a user's profile is needed.
 */
public class UserProfile {
    private String firstName;
    private String lastName;
    private Integer cityCode;
    private String city;
    private String country;
    private long birthdayMillis;
    private int age;


    /**
     * @param firstName The user's first name.
     * @param lastName	The user's last name.
     * @param cityCode 	The user's city code.
     * @param city		The user's city.
     * @param country	The user's country.
     * @param age		The user's age.
     * @param birthdayMillis The user's birthday represented as the number of milliseconds since Jan 1st 1970.
     */
    public UserProfile(String firstName, String lastName, Integer cityCode, String city, String country, int age, long birthdayMillis) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cityCode = cityCode;
        this.city = city;
        this.country = country;
        this.age = age;
        this.birthdayMillis = birthdayMillis;
    }

    public static UserProfile getUserFromJson(String jsonResponse) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(jsonResponse));
        reader.setLenient(true);

        Type userProfileType = new TypeToken<UserProfile>() {}.getType();

        return gson.fromJson(reader, userProfileType);
    }


    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getCity() {
        return this.city;
    }

    public int getAge() {
        return this.age;
    }

    public long getBirthdayMillis() {
        return this.birthdayMillis;
    }

    public String getCountry() {
        return this.country;
    }

    public Integer getCityCode() {
        return this.cityCode;
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}


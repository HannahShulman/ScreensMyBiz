package com.cliqdbase.app.general;

import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cliqdbase.app.constants.AppConstants;

/**
 * Created by Yuval on 06/03/2015.
 *
 * @author Yuval Siev
 */
public class InputValidation {


    /**
     * Checking if a given string represents an email
     * @param email    The given string. The function will check if this string represents an email address
     * @return True if the given string represents an email. False otherwise.
     */
    public static boolean validateEmail(String email) {
        String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";     // This regex is taken from http://howtodoinjava.com/2014/11/11/java-regex-validate-email-address/

        if (email == null)
            return false;

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);       // Making sure that the email address is consistent with the email regex
        return matcher.matches();
    }

    /**
     * Checks if the given birthday string represents a valid date.
     * @param birthday    The birthday string
     * @return  true if the string represents a valid date and false otherwise.
     */
    public static boolean validateBirthday(String birthday) {
        if (birthday != null && birthday.length() > 0) {      // Date of birth can't be null and of length zero!
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(birthday);                            // If we can't parse, the user didn't enter a valid date and we will move to the catch block, where the function will return false.
                return (date.getTime() < System.currentTimeMillis());
            } catch (ParseException e) {
                return false;
            }
        }
        return false;
    }

    public static boolean validName(String name) {
        return (name != null && name.length() != 0);
    }


    /**
     * Checks if the given password is valid.
     * @param password    The password string - not hashed.
     * @return  true if the given string is a valid password, false otherwise
     */
    public static boolean validPassword(String password) {
        return password.length() >= AppConstants.PASSWORD_MINIMUM_LENGTH;
    }
}

package com.cliqdbase.app.general;

import android.support.annotation.NonNull;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.cliqdbase.app.R;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialog;

/**
 * Created by Yuval on 07/05/2015.
 *
 * @author Yuval Siev
 */
public class Dialogs {
    public static CalendarDatePickerDialog createBirthdayDatePickerDialog(final EditText editText) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());

        CalendarDatePickerDialog datePicker = CalendarDatePickerDialog.newInstance(
                new CalendarDatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dateOfMonth) {
                        monthOfYear++;
                        String dateOfBirth = dateOfMonth + "/" + monthOfYear + "/" + year;
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        Date date;
                        try {
                            // Formatting the date based on the user's locale.
                            date = simpleDateFormat.parse(dateOfBirth);
                            showPickedDateOnEditText(editText, date);
                        } catch (ParseException e) { /* Ignore */ }
                    }
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        datePicker.setYearRange(1900, cal.get(Calendar.YEAR) - 1);

        return datePicker;
    }

    public static void showPickedDateOnEditText(@NonNull EditText editText, @NonNull Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateOfBirthToPresent = sdf.format(date);

        editText.setError(null);                                              // Making sure that all previous errors has been deleted.
        editText.setBackgroundResource(R.drawable.edit_text_box);             // Setting the edit text box to the original non-error view
        editText.setText(dateOfBirthToPresent);
    }

}

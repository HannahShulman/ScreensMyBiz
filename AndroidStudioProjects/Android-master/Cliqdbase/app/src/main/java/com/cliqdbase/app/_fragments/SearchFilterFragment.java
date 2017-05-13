package com.cliqdbase.app._fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app._activities.CliqSearchResultActivity;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.general.Common;
import com.cliqdbase.app.search_filter_stuff.ColorsArrayAdapter;
import com.cliqdbase.app.search_filter_stuff.MyColor;
import com.cliqdbase.app.search_filter_stuff.MyStringArrayAdapter;
import com.cliqdbase.app.server_model.SearchFilter;
import com.cliqdbase.app.widgets.MyColorEditText;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import yuku.ambilwarna.AmbilWarnaDialog;

public class SearchFilterFragment extends Fragment implements View.OnClickListener,
        View.OnFocusChangeListener,
        AsyncResponse_Server,
        CalendarDatePickerDialog.OnDateSetListener,
        RadialTimePickerDialog.OnTimeSetListener,
        DialogInterface.OnClickListener {

    private EditText cliq_clothes_et;
    private MyColorEditText cliq_clothes_color_et;
    private MyColorEditText cliq_hair_color_et;
    private AutoCompleteTextView cliq_hair_style_et;
    private MyColorEditText cliq_eye_color_et;
    private MyColorEditText cliq_skin_color_et;
    private AutoCompleteTextView cliq_body_type_et;
    private EditText cliq_height_et;

    private EditText my_clothes_et;
    private MyColorEditText my_clothes_color_et;
    private MyColorEditText my_hair_color_et;
    private AutoCompleteTextView my_hair_style_et;
    private MyColorEditText my_eye_color_et;
    private MyColorEditText my_skin_color_et;
    private AutoCompleteTextView my_body_type_et;
    private EditText my_height_et;

    private EditText cliq_date_et;
    private EditText cliq_time_et;

    private RadioGroup gender_group;

    private Button search_cliq_button;
    private Button load_filters_button;

    private CheckBox save_filter_checkbox;

    private boolean is24hour;

    private MyColorEditText color_et_last_touched;


    private static final int CTSC_SEARCH = 0;
    private static final int CTSC_GET_INITIAL_DATA = 1;

    private HashMap<String, SearchFilter.CliqSearchFilter> cliqFiltersMap;

    private AlertDialog saveFilterAlert;
    private AlertDialog loadFilterAlert;

    private AlertDialog myHeightDialog;
    private AlertDialog cliqHeightDialog;

    private AlertDialog myClothesPickerDialog;
    private AlertDialog cliqClothesPickerDialog;
    private ArrayList<String> myClothesPicked;
    private ArrayList<String> cliqClothesPicked;

    private SearchFilter userData;

    private long userId;
    private long filterIdUpdating;
    private String filterNameUpdating;

    private ScrollView view;

    private ConnectToServer getInitialDataAsync;
    private ConnectToServer searchCliqAsync;


    private static final String CLOTHES_SEPARATOR = ", ";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = (ScrollView) inflater.inflate(R.layout.fragment_search_filter, container, false);


        // Finding the preferred time format of the user.
        is24hour = DateFormat.is24HourFormat(getActivity());

        getInitialDataAsync = null;
        searchCliqAsync = null;

        findViewsById();
        setKeyListeners();
        setOnClickListeners();
        setOnFocusChangedListeners();
        setColorDialogs();

        saveFilterAlert = null;
        loadFilterAlert = null;

        myHeightDialog = null;
        cliqHeightDialog = null;

        filterIdUpdating = -1;
        filterNameUpdating = null;

        myClothesPicked = new ArrayList<>();
        cliqClothesPicked = new ArrayList<>();


        setInitialTimeInTimeEditText();

        downloadInitialData();       // Downloads the data about the user appearance

        return view;
    }

    private void findViewsById() {
        cliq_date_et = (EditText) view.findViewById(R.id.filter_cliq_date);
        cliq_time_et = (EditText) view.findViewById(R.id.filter_cliq_time);

        gender_group = (RadioGroup) view.findViewById(R.id.filter_cliq_gender_group);

        cliq_clothes_et = (EditText) view.findViewById(R.id.filter_cliq_clothes);
        cliq_clothes_color_et = (MyColorEditText) view.findViewById(R.id.filter_cliq_clothes_color);
        cliq_hair_color_et = (MyColorEditText) view.findViewById(R.id.filter_cliq_hair_color);
        cliq_hair_style_et = (AutoCompleteTextView) view.findViewById(R.id.filter_cliq_hair_style);
        cliq_eye_color_et = (MyColorEditText) view.findViewById(R.id.filter_cliq_eye_color);
        cliq_skin_color_et = (MyColorEditText) view.findViewById(R.id.filter_cliq_skin_color);
        cliq_body_type_et = (AutoCompleteTextView) view.findViewById(R.id.filter_cliq_body_type);
        cliq_height_et = (EditText) view.findViewById(R.id.filter_cliq_height);

        my_clothes_et = (EditText) view.findViewById(R.id.filter_my_clothes);
        my_clothes_color_et = (MyColorEditText) view.findViewById(R.id.filter_my_clothes_color);
        my_hair_color_et = (MyColorEditText) view.findViewById(R.id.filter_my_hair_color);
        my_hair_style_et = (AutoCompleteTextView) view.findViewById(R.id.filter_my_hair_style);
        my_eye_color_et = (MyColorEditText) view.findViewById(R.id.filter_my_eye_color);
        my_skin_color_et = (MyColorEditText) view.findViewById(R.id.filter_my_skin_color);
        my_body_type_et = (AutoCompleteTextView) view.findViewById(R.id.filter_my_body_type);
        my_height_et = (EditText) view.findViewById(R.id.filter_my_height);

        search_cliq_button = (Button) view.findViewById(R.id.filter_cliq_search_button);
        save_filter_checkbox = (CheckBox) view.findViewById(R.id.filter_cliq_save_filter);
        load_filters_button = (Button) view.findViewById(R.id.filter_cliq_load_filter);
    }

    private void setKeyListeners() {
        // Making sure that the user will not be able to enter a text manually in those edit texts - only using our dialogs.
        cliq_date_et.setKeyListener(null);
        cliq_time_et.setKeyListener(null);

        cliq_clothes_et.setKeyListener(null);
        cliq_clothes_color_et.setKeyListener(null);
        cliq_hair_color_et.setKeyListener(null);
        cliq_eye_color_et.setKeyListener(null);
        cliq_skin_color_et.setKeyListener(null);
        cliq_height_et.setKeyListener(null);

        my_clothes_et.setKeyListener(null);
        my_clothes_color_et.setKeyListener(null);
        my_hair_color_et.setKeyListener(null);
        my_eye_color_et.setKeyListener(null);
        my_skin_color_et.setKeyListener(null);
        my_height_et.setKeyListener(null);
    }

    private void setOnClickListeners() {
        cliq_clothes_et.setOnClickListener(this);
        cliq_clothes_color_et.setOnClickListener(this);
        cliq_hair_color_et.setOnClickListener(this);
        cliq_hair_style_et.setOnClickListener(this);
        cliq_eye_color_et.setOnClickListener(this);
        cliq_skin_color_et.setOnClickListener(this);
        cliq_body_type_et.setOnClickListener(this);
        cliq_height_et.setOnClickListener(this);

        cliq_date_et.setOnClickListener(this);
        cliq_time_et.setOnClickListener(this);

        my_clothes_et.setOnClickListener(this);
        my_clothes_color_et.setOnClickListener(this);
        my_hair_color_et.setOnClickListener(this);
        my_hair_style_et.setOnClickListener(this);
        my_eye_color_et.setOnClickListener(this);
        my_skin_color_et.setOnClickListener(this);
        my_body_type_et.setOnClickListener(this);
        my_height_et.setOnClickListener(this);

        search_cliq_button.setOnClickListener(this);
        load_filters_button.setOnClickListener(this);
    }

    private void setOnFocusChangedListeners() {
        cliq_clothes_et.setOnFocusChangeListener(this);
        cliq_clothes_color_et.setOnFocusChangeListener(this);
        cliq_hair_color_et.setOnFocusChangeListener(this);
        cliq_hair_style_et.setOnFocusChangeListener(this);
        cliq_eye_color_et.setOnFocusChangeListener(this);
        cliq_skin_color_et.setOnFocusChangeListener(this);
        cliq_body_type_et.setOnFocusChangeListener(this);
        cliq_height_et.setOnFocusChangeListener(this);

        cliq_date_et.setOnFocusChangeListener(this);
        cliq_time_et.setOnFocusChangeListener(this);

        search_cliq_button.setOnFocusChangeListener(this);

        my_clothes_et.setOnFocusChangeListener(this);
        my_clothes_color_et.setOnFocusChangeListener(this);
        my_hair_color_et.setOnFocusChangeListener(this);
        my_hair_style_et.setOnFocusChangeListener(this);
        my_eye_color_et.setOnFocusChangeListener(this);
        my_skin_color_et.setOnFocusChangeListener(this);
        my_body_type_et.setOnFocusChangeListener(this);
        my_height_et.setOnFocusChangeListener(this);
    }

    /**
     * Each color-edit-text has a dialog.
     * The dialog is a listView that contains a list of colors.
     * If a user clicks on the color 'other', a color-picker will appear and enable him to choose the exact color.
     */
    private void setColorDialogs() {
        ColorsArrayAdapter myHairColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getHairColorList());
        ColorsArrayAdapter cliqHairColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getHairColorList());
        ColorsArrayAdapter myEyeColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getEyeColorList());
        ColorsArrayAdapter cliqEyeColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getEyeColorList());
        ColorsArrayAdapter mySkinColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getSkinColorList());
        ColorsArrayAdapter cliqSkinColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getSkinColorList());
        ColorsArrayAdapter myClothesColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getClothesColorList());
        ColorsArrayAdapter cliqClothesColorsAdapter = new ColorsArrayAdapter(getActivity(), R.layout.item_color, MyColor.getClothesColorList());

        AlertDialog myClothesColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select clothes color")
                .setAdapter(myClothesColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();

        AlertDialog cliqClothesColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select clothes color")
                .setAdapter(cliqClothesColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();


        AlertDialog myHairColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select hair color")
                .setAdapter(myHairColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();

        AlertDialog cliqHairColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select hair color")
                .setAdapter(cliqHairColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();


        AlertDialog mySkinColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select skin color")
                .setAdapter(mySkinColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();

        AlertDialog cliqSkinColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select skin color")
                .setAdapter(cliqSkinColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();


        AlertDialog myEyeColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select eye color")
                .setAdapter(myEyeColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();

        AlertDialog cliqEyeColorDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Select eye color")
                .setAdapter(cliqEyeColorsAdapter, this)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.clear, this)
                .create();

        List<MyColor> clothesColorList = MyColor.getClothesColorList();
        my_clothes_color_et.setColorsDialog(myClothesColorDialog, clothesColorList);
        cliq_clothes_color_et.setColorsDialog(cliqClothesColorDialog, clothesColorList);

        List<MyColor> hairColorList = MyColor.getHairColorList();
        my_hair_color_et.setColorsDialog(myHairColorDialog, hairColorList);
        cliq_hair_color_et.setColorsDialog(cliqHairColorDialog, hairColorList);

        List<MyColor> skinColorList = MyColor.getSkinColorList();
        my_skin_color_et.setColorsDialog(mySkinColorDialog, skinColorList);
        cliq_skin_color_et.setColorsDialog(cliqSkinColorDialog, skinColorList);

        List<MyColor> eyeColorList = MyColor.getEyeColorList();
        my_eye_color_et.setColorsDialog(myEyeColorDialog, eyeColorList);
        cliq_eye_color_et.setColorsDialog(cliqEyeColorDialog, eyeColorList);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {        // On click of one of the color dialogs.
        if (which == Dialog.BUTTON_POSITIVE)
            dialog.dismiss();
        else if (which == Dialog.BUTTON_NEGATIVE) {
            color_et_last_touched.restoreOriginalDrawable();        // When the user clicks on 'clear' - we will restore the original drawable of the edittext.
            color_et_last_touched.setHint(R.string.blank_any);
        }
        else {
            MyColor color = (MyColor) ((AlertDialog) dialog).getListView().getAdapter().getItem(which);

            if (color == null)
                return;

            if (!color.isOther()) {     // If the color is in the list, change the edittext background to the selected color, and remove the hint.
                color_et_last_touched.setBackgroundColor(color.getExactColorCode());
                color_et_last_touched.setHint("");
            }
            else {          // The user selected 'other'. We will show him the color-picker dialog.
                AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(getActivity(), 0xFFFFFFFF, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onCancel(AmbilWarnaDialog ambilWarnaDialog) {
                    }

                    @Override
                    public void onOk(AmbilWarnaDialog ambilWarnaDialog, int i) {
                        color_et_last_touched.setBackgroundColor(MyColor.getInstance(i).getExactColorCode());
                        color_et_last_touched.setHint("");
                    }
                });
                colorPicker.show();
            }
        }
    }

    /**
     * Setting the autocomplete text views' adapters
     */
    private void setAutocompleteTextViewStringCodesAdapters() {
        MyStringArrayAdapter hairStyles_adapter1 = new MyStringArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, SearchFilter.getHairStyleHashMapValues());
        MyStringArrayAdapter hairStyles_adapter2 = new MyStringArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, SearchFilter.getHairStyleHashMapValues());
        MyStringArrayAdapter bodyTypes_adapter1 = new MyStringArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, SearchFilter.getBodyTypeHashMapValues());
        MyStringArrayAdapter bodyTypes_adapter2 = new MyStringArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, SearchFilter.getBodyTypeHashMapValues());

        cliq_hair_style_et.setAdapter(hairStyles_adapter1);
        cliq_hair_style_et.setDropDownBackgroundResource(R.color.dropdown_background);

        cliq_body_type_et.setAdapter(bodyTypes_adapter1);
        cliq_body_type_et.setDropDownBackgroundResource(R.color.dropdown_background);

        my_hair_style_et.setAdapter(hairStyles_adapter2);
        my_hair_style_et.setDropDownBackgroundResource(R.color.dropdown_background);

        my_body_type_et.setAdapter(bodyTypes_adapter2);
        my_body_type_et.setDropDownBackgroundResource(R.color.dropdown_background);
    }

    /**
     * Setting the clothes list in the clothes dialog.
     * Also, defining which item should be pre-checked, based on the data received from the server.
     */
    private void setClothesListInClothesDialog() {
        final String[] items = SearchFilter.getClothingHashMapValuesAsArray();      // receive the array of clothing pieces

        boolean[] mySelectedItems = new boolean[items.length];
        boolean[] cliqSelectedItems = new boolean[items.length];

        String[] myClothesItems = my_clothes_et.getText().toString().split(CLOTHES_SEPARATOR);
        String[] cliqClothesItems = cliq_clothes_et.getText().toString().split(CLOTHES_SEPARATOR);

        // Finding what pieces of clothes should be checked in the clothes dialog.
        for (int i = 0; i < items.length; i++) {
            boolean flag = false;
            for (String cloth : myClothesItems) {
                if (items[i].equalsIgnoreCase(cloth)) {
                    myClothesPicked.add(items[i]);
                    flag = true;
                    break;
                }
            }
            mySelectedItems[i] = flag;

            flag = false;
            for (String cloth : cliqClothesItems) {
                if (items[i].equalsIgnoreCase(cloth)) {
                    cliqClothesPicked.add(items[i]);
                    flag = true;
                    break;
                }
            }
            cliqSelectedItems[i] = flag;
        }

        myClothesPickerDialog = new AlertDialog.Builder(getActivity())
                .setMultiChoiceItems(items, mySelectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked && !myClothesPicked.contains(items[which]))
                            myClothesPicked.add(items[which]);
                        else if (!isChecked && myClothesPicked.contains(items[which]))
                            myClothesPicked.remove(items[which]);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String clothes = "";

                        for (String s : myClothesPicked) {
                            if (!clothes.isEmpty())
                                clothes += CLOTHES_SEPARATOR;
                            clothes += s;
                        }

                        my_clothes_et.setText(clothes);
                    }
                })
                .create();

        cliqClothesPickerDialog = new AlertDialog.Builder(getActivity())
                .setMultiChoiceItems(items, cliqSelectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked && !cliqClothesPicked.contains(items[which]))
                            cliqClothesPicked.add(items[which]);
                        else if (!isChecked && cliqClothesPicked.contains(items[which]))
                            cliqClothesPicked.remove(items[which]);
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String clothes = "";

                        for (String s : cliqClothesPicked) {
                            if (!clothes.isEmpty())
                                clothes += CLOTHES_SEPARATOR;
                            clothes += s;
                        }

                        cliq_clothes_et.setText(clothes);
                    }
                })
                .create();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_cliq_clothes_color:
            case R.id.filter_cliq_hair_color:
            case R.id.filter_cliq_eye_color:
            case R.id.filter_cliq_skin_color:
            case R.id.filter_my_clothes_color:
            case R.id.filter_my_hair_color:
            case R.id.filter_my_eye_color:
            case R.id.filter_my_skin_color:
                this.color_et_last_touched = (MyColorEditText) v;
                this.color_et_last_touched.showColorsDialog();
                break;
            case R.id.filter_cliq_hair_style:
            case R.id.filter_cliq_body_type:
            case R.id.filter_my_hair_style:
            case R.id.filter_my_body_type:
                ((AutoCompleteTextView)v).showDropDown();
                break;
            case R.id.filter_cliq_clothes:
                if (this.cliqClothesPickerDialog != null)
                    this.cliqClothesPickerDialog.show();
                else
                    Toast.makeText(getActivity(), "Loading, Please wait", Toast.LENGTH_SHORT).show();
                break;
            case R.id.filter_my_clothes:
                if (myClothesPickerDialog != null)
                    this.myClothesPickerDialog.show();
                else
                    Toast.makeText(getActivity(), "Loading, Please wait", Toast.LENGTH_SHORT).show();
                break;
            case R.id.filter_cliq_height:
                showHeightDialog(true);
                break;
            case R.id.filter_my_height:
                showHeightDialog(false);
                break;
            case R.id.filter_cliq_date:
                showDatePicker();
                break;
            case R.id.filter_cliq_time:
                showTimePicker();
                break;
            case R.id.filter_cliq_search_button:
                verifyInputAndSearch();
                break;
            case R.id.filter_cliq_load_filter:
                showFiltersDialog();
                break;
        }
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus)
            onClick(v);
        else {
            int vId = v.getId();
            if (vId == cliq_date_et.getId() || vId == cliq_time_et.getId())
                verifyDateAndTime();
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                .newInstance(this, year, month, day);

        calendarDatePickerDialog.setYearRange(1900, year);

        calendarDatePickerDialog.show(getFragmentManager(), "Date Picker Title");
    }


    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                .newInstance(this, hourOfDay, minute, this.is24hour);
        timePickerDialog.show(getFragmentManager(), "Time Picker Title");
    }

    /**
     * Response from the date picker dialog.
     * @param calendarDatePickerDialog      The dialog. Can be null.
     * @param year                          The selected year.
     * @param monthOfYear                   The selected month.
     * @param dateOfMonth                   The selected day.
     */
    @Override
    public void onDateSet(CalendarDatePickerDialog calendarDatePickerDialog, int year, int monthOfYear, int dateOfMonth) {
        onDateSetHelper(year, monthOfYear, dateOfMonth);
        verifyDateAndTime();
    }

    private void onDateSetHelper(int year, int monthOfYear, int dateOfMonth) {
        String dateStr = (monthOfYear + 1) + "/" + dateOfMonth + "/" + year;

        SimpleDateFormat sfdSource = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        SimpleDateFormat sdfDestination = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String correctDateStr;

        try {
            Date datePicked = sfdSource.parse(dateStr);
            correctDateStr = sdfDestination.format(datePicked);
        } catch (ParseException e) {
            correctDateStr = "";
            Toast.makeText(getActivity(), R.string.date_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        this.cliq_date_et.setText(correctDateStr);
    }

    /**
     * The response from the time picker dialog.
     * @param radialTimePickerDialog    The dialog. Can be null.
     * @param hourOfDay                 The selected hour.
     * @param minute                    The selected minute.
     */
    @Override
    public void onTimeSet(RadialTimePickerDialog radialTimePickerDialog, int hourOfDay, int minute) {
        onTimeSetHelper(hourOfDay, minute);
        verifyDateAndTime();
    }

    private void onTimeSetHelper(int hourOfDay, int minute) {
        hourOfDay %= 24;
        String timeStr = hourOfDay + ":" + minute;

        SimpleDateFormat sfdSource = new SimpleDateFormat("kk:mm", Locale.US);

        String pattern;
        if (this.is24hour)
            pattern = "HH:mm";
        else
            pattern = "hh:mm aa";

        SimpleDateFormat sdfDestination = new SimpleDateFormat(pattern, Locale.getDefault());

        String correctTimeStr;

        try {
            Date timePicked = sfdSource.parse(timeStr);
            correctTimeStr = sdfDestination.format(timePicked);
        } catch (ParseException e) {
            correctTimeStr = "";
            Toast.makeText(getActivity(), R.string.date_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        this.cliq_time_et.setText(correctTimeStr);
    }

    /**
     * Sets the default data in the date edit texts.
     * The default date: 30 minutes ago.
     */
    private void setInitialTimeInTimeEditText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis() - 1000*60*30));            // now minus 30 minutes.

        // Setting the text in the edit texts
        onDateSetHelper(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        onTimeSetHelper(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    /**
     * Shows the height dialog.
     * @param cliq    true if the result should be written on the cliq's height-edittext. false for the user's height-edittext.
     */
    private void showHeightDialog(final boolean cliq) {
        AlertDialog dialog;
        if ((cliq && cliqHeightDialog == null) || (!cliq && myHeightDialog == null)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            View dialog_view = View.inflate(getActivity(), R.layout.height_dialog, null);
            RadioGroup unitsRadioGroup = (RadioGroup) dialog_view.findViewById(R.id.units_radio_group);

            final NumberPicker feetNumberPicker = (NumberPicker) dialog_view.findViewById(R.id.units_foot_number_picker);
            final NumberPicker inchesNumberPicker = (NumberPicker) dialog_view.findViewById(R.id.units_inches_number_picker);
            final NumberPicker centimetersNumberPicker = (NumberPicker) dialog_view.findViewById(R.id.units_centimeters_number_picker);

            feetNumberPicker.setMinValue(0);
            feetNumberPicker.setMaxValue(8);
            feetNumberPicker.setValue(5);

            inchesNumberPicker.setMinValue(0);
            inchesNumberPicker.setMaxValue(11);
            inchesNumberPicker.setValue(0);

            centimetersNumberPicker.setMinValue(0);
            centimetersNumberPicker.setMaxValue(300);
            centimetersNumberPicker.setValue(150);

            if (Common.isPreferredUnitsMetric(getActivity())) {
                unitsRadioGroup.check(R.id.units_metric_radio_button);
                centimetersNumberPicker.setVisibility(View.VISIBLE);
                feetNumberPicker.setVisibility(View.INVISIBLE);
                inchesNumberPicker.setVisibility(View.INVISIBLE);
            }
            else {
                unitsRadioGroup.check(R.id.units_imperial_radio_button);
                centimetersNumberPicker.setVisibility(View.INVISIBLE);
                feetNumberPicker.setVisibility(View.VISIBLE);
                inchesNumberPicker.setVisibility(View.VISIBLE);
            }

            unitsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    boolean metric = (checkedId == R.id.units_metric_radio_button);
                    Common.setPreferredUnitMetric(getActivity(), metric);

                    if (metric) {
                        centimetersNumberPicker.setVisibility(View.VISIBLE);
                        feetNumberPicker.setVisibility(View.INVISIBLE);
                        inchesNumberPicker.setVisibility(View.INVISIBLE);
                    } else {
                        centimetersNumberPicker.setVisibility(View.INVISIBLE);
                        feetNumberPicker.setVisibility(View.VISIBLE);
                        inchesNumberPicker.setVisibility(View.VISIBLE);
                    }
                }
            });


            builder.setView(dialog_view);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String height;
                    if (Common.isPreferredUnitsMetric(getActivity()))
                        height = Common.getHeightString(centimetersNumberPicker.getValue(), true);
                    else
                        height = Common.getHeightString(Common.convertFeetAndInchesToCm(feetNumberPicker.getValue(), inchesNumberPicker.getValue()), false);

                    if (cliq)
                        cliq_height_et.setText(height);
                    else
                        my_height_et.setText(height);
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (cliq) {
                        if (cliq_height_et.length() > 0)
                            cliq_height_et.getText().clear();
                    } else {
                        if (my_height_et.length() > 0)
                            my_height_et.getText().clear();
                    }
                }
            });
            builder.setCancelable(true);
            dialog = builder.create();
        }
        else {
            if (cliq)
                dialog = cliqHeightDialog;
            else
                dialog = myHeightDialog;
        }

        dialog.show();
    }

    /**
     * Verifies the input and contacts the server to start searching using the given parameters
     */
    private void verifyInputAndSearch() {
        JsonObject parameters = new JsonObject();

        if (!verifyDateAndTime())
            return;

        Date date = getDate();
        Date time = getTimeFromTimeEditText();

        parameters.addProperty("cliq_date", date.getTime());
        parameters.addProperty("cliq_time", time.getTime());
        parameters.addProperty("userId", userId);

        String cliq_gender;
        switch (this.gender_group.getCheckedRadioButtonId()) {
            case R.id.filter_cliq_gender_male:
                cliq_gender = "male";
                break;
            case R.id.filter_cliq_gender_female:
                cliq_gender = "female";
                break;
            default:
                cliq_gender = "other";
                break;
        }

        parameters.addProperty("cliq_gender", cliq_gender);

        SearchFilter cliqFilter = getDataFromEditTexts(false);
        SearchFilter userFilter = getDataFromEditTexts(true);

        Gson gson = new Gson();
        Type type = new TypeToken<SearchFilter>() {}.getType();

        if (userFilter != null) {
            if (userProfileUpdated(userFilter) && userFilter.hasAnyData()) {
                JsonElement userFilterJson = gson.toJsonTree(userFilter, type);
                parameters.add("user_filter_json", userFilterJson);
            }
        }
        else
            return;

        // If the user want to save the cliq filter, he will check the save_filter_checkbox box.
        if (cliqFilter != null) {
            if (cliqFilter.hasAnyData()) {
                if (save_filter_checkbox.isChecked())     // if the user want to save the filter, we will display the save-filter-dialog dialog, and from there we will contact the server.
                    showSaveFilterDialog(parameters, cliqFilter);
                else {      // if not, we will just contact the server from here.
                    JsonElement cliqFilterJson = gson.toJsonTree(cliqFilter, type);
                    parameters.add("cliq_filter_json", cliqFilterJson);
                    sendSearchDataToServer(parameters);
                }
            }
        }
    }

    /**
     * Sending the search parameters to the server.
    * @param parameters     The parameters.
     */
    private void sendSearchDataToServer(JsonObject parameters) {
        Log.d("yuval", parameters.toString());

        searchCliqAsync = new ConnectToServer(getActivity(), true, CTSC_SEARCH);
        searchCliqAsync.delegate = this;
        searchCliqAsync.execute(ServerUrlConstants.SEARCH_CLIQ_SERVLET, "POST", parameters.toString());
    }


    /**
     * Verifies the date and time.
     * The date and time are valid only if:
     * 1. They are not empty.
     * 2. They are not in the future.
     *
     * @return  true if verified and valid, false otherwise.
     */
    private boolean verifyDateAndTime() {
        String cliq_date = cliq_date_et.getText().toString().trim();
        String cliq_time = cliq_time_et.getText().toString().trim();

        if (cliq_date.isEmpty() && cliq_time.isEmpty()) {
            this.cliq_date_et.setError("Date and time cannot be empty!");
            return false;
        }

        Date date = getDate();
        Date time = getTimeFromTimeEditText();
        if (date == null) {
            this.cliq_date_et.setError("Date is not valid");
            return false;
        }
        if (time == null) {
            this.cliq_time_et.setError("Time is not valid");
            return false;
        }

        TimeZone tz = TimeZone.getDefault();
        
        /*Calendar datePickedCal = Calendar.getInstance();
        datePickedCal.setTimeInMillis(date.getTime());
        datePickedCal.setTimeZone(tz);

        Calendar dateAndTimePickedCal = Calendar.getInstance();
        dateAndTimePickedCal.setTimeInMillis(date.getTime() + time.getTime());
        dateAndTimePickedCal.setTimeZone(tz);

        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(System.currentTimeMillis());
        nowCal.setTimeZone(tz);

        if (datePickedCal.compareTo(nowCal) > 0) {      // The date is in the future
            this.cliq_date_et.setError("Date cannot be in the future.");
            return false;
        }
        else if (dateAndTimePickedCal.compareTo(nowCal) > 0) {
            this.cliq_date_et.setError(null);       // The date here is ok.
            this.cliq_time_et.setError("Time cannot be in the future.");
            return false;
        }*/

        if (date.getTime() + tz.getOffset(date.getTime()) > System.currentTimeMillis()) {          // If only the date is in the future
            this.cliq_date_et.setError("Date cannot be in the future.");
            return false;
        }
        else if (date.getTime() + time.getTime() + tz.getOffset(date.getTime())> System.currentTimeMillis()) {      // If the date is not in the future, but the date + time is in the future. This can only happen when the day is today, and the hour is in the future.
            this.cliq_date_et.setError(null);       // The date here is ok.
            this.cliq_time_et.setError("Time cannot be in the future.");
            return false;
        }

        this.cliq_time_et.setError(null);
        this.cliq_date_et.setError(null);

        return true;
    }

    /**
     * @return a Date object based on the data on the time-edittext.
     */
    private Date getTimeFromTimeEditText() {
        String cliq_time = cliq_time_et.getText().toString().trim();

        String timePattern;
        if (this.is24hour)
            timePattern = "kk:mm";
        else
            timePattern = "hh:mm";

        SimpleDateFormat timeSimpleDateFormat = new SimpleDateFormat(timePattern, Locale.getDefault());
        Date time = null;
        try {
            time = timeSimpleDateFormat.parse(cliq_time);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return time;
    }

    /**
     * @return a Date object based on the data on the date-edittext.
     */
    private Date getDate() {
        String cliq_date = cliq_date_et.getText().toString().trim();

        SimpleDateFormat timeSimpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date = null;
        try {
            date = timeSimpleDateFormat.parse(cliq_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    /**
     * Downloads the initial data.
     */
    private void downloadInitialData() {
        getInitialDataAsync = new ConnectToServer(getActivity(), false, CTSC_GET_INITIAL_DATA);
        getInitialDataAsync.delegate = this;
        getInitialDataAsync.execute(ServerUrlConstants.GET_SEARCH_FILTER_INITIAL_DATA, "GET");
    }

    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        switch ((int)taskCode) {
            case CTSC_SEARCH:
                if (httpResultCode == 200) {        // Response from the search servlet.
                    JsonReader reader = new JsonReader(new StringReader(data));
                    reader.setLenient(true);

                    JsonParser parser = new JsonParser();
                    JsonElement jsonTree = parser.parse(reader);
                    JsonObject object = jsonTree.getAsJsonObject();

                    Log.d("yuval", data);

                    // a boolean data whether to show the user a message for saving/updating the cliq's filter.
                    JsonElement display = object.get("cliqFilterDisplayMessageToUser");
                    boolean showMessageToUser = false;
                    if (display != null)
                        showMessageToUser = display.getAsBoolean();

                    if (showMessageToUser) {
                        String text;
                        JsonElement saved = object.get("cliqFilterSaved");
                        if (saved != null && saved.getAsBoolean()) {
                            JsonElement updated = object.get("cliqFilterUpdated");
                            if (updated != null && updated.getAsBoolean())
                                text = "Cliq filter updated successfully!";
                            else
                                text = "Cliq filter saved successfully!";
                        }
                        else
                            text = "There was an error saving the cliq filter.";
                        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
                    }


                    // starting the search result activity.
                    startActivity(new Intent(getActivity(), CliqSearchResultActivity.class));
                }
                else
                    Toast.makeText(getActivity(), "There was an error in the server. Error code " + httpResultCode, Toast.LENGTH_LONG).show();
                break;
            case CTSC_GET_INITIAL_DATA:
                switch (httpResultCode) {
                    case 200:       // receiving the initial data.
                        /*
                        The initial data contains:
                            The symbol tables of the hair style, clothing type, body type,.
                            The last filter the user have used about himself.
                            The saved filter of cliqs.
                            The user id of the user.
                         */
                        try {
                            Log.d("yuval", data);

                            JsonReader reader = new JsonReader(new StringReader(data));
                            reader.setLenient(true);

                            JsonParser parser = new JsonParser();
                            JsonElement jsonTree = parser.parse(reader);

                            reader.close();

                            JsonObject object = jsonTree.getAsJsonObject();

                            JsonElement hairStyleJson = object.get("hairStyleCodes");
                            JsonElement bodyTypeJson = object.get("bodyTypeCodes");
                            JsonElement clothingJson = object.get("clothingCodes");
                            JsonElement userFilterJson = object.get("userFilter");
                            JsonElement cliqFiltersJson = object.get("cliqFilters");
                            JsonElement userIdJson = object.get("userId");
                            userId = userIdJson.getAsLong();

                            Gson gson = new Gson();
                            Type dualHashBidiMapType = new TypeToken<DualHashBidiMap<Integer, String>>(){}.getType();
                            Type searchFilterType = new TypeToken<SearchFilter>(){}.getType();
                            Type cliqSearchFilterHashMapType = new TypeToken<HashMap<String, SearchFilter.CliqSearchFilter>>() {}.getType();

                            DualHashBidiMap<Integer, String> hairStyleMap = gson.fromJson(hairStyleJson, dualHashBidiMapType);
                            DualHashBidiMap<Integer, String> bodyTypeMap = gson.fromJson(bodyTypeJson, dualHashBidiMapType);
                            DualHashBidiMap<Integer, String> clothingMap = gson.fromJson(clothingJson, dualHashBidiMapType);
                            HashMap<String, SearchFilter.CliqSearchFilter> filtersHashMap = gson.fromJson(cliqFiltersJson, cliqSearchFilterHashMapType);

                            SearchFilter.setHairStyleCodes(hairStyleMap);
                            SearchFilter.setBodyTypeCodes(bodyTypeMap);
                            SearchFilter.setClothingCodes(clothingMap);

                            this.cliqFiltersMap = new HashMap<>();
                            boolean first = true;
                            for (Map.Entry<String, SearchFilter.CliqSearchFilter> entry : filtersHashMap.entrySet()) {
                                if (first) {
                                    setDataInEditTexts(entry.getValue(), false);        // Setting the received data about the last used cliq filter in the edit texts.
                                    first = false;
                                }
                                if (!entry.getKey().isEmpty())
                                    this.cliqFiltersMap.put(entry.getKey(), entry.getValue());
                            }

                            setAutocompleteTextViewStringCodesAdapters();

                            this.userData = gson.fromJson(userFilterJson, searchFilterType);
                            setDataInEditTexts(this.userData, true);            // Setting the received data of the user filter in the user's edit texts.

                            setClothesListInClothesDialog();

                        } catch (IOException e) {
                            downloadInitialData();
                            return;
                        }
                        break;
                }
                break;
        }
    }

    /**
     * Creating (if needed) and showing the save-filter-dialog.
     * This function is called right before contacting the server and starting the search process.
     * @param parameters    The parameters to call the search servlet with. We should only add items to this parameters, and not remove any.
     * @param cliqFilter    The filter to be saved.
     */
    private void showSaveFilterDialog(final JsonObject parameters, final SearchFilter cliqFilter) {
        if (saveFilterAlert == null) {
            View view = View.inflate(getActivity(), R.layout.dialog_save_filter, null);
            final EditText filterName_et = (EditText) view.findViewById(R.id.filter_cliq_save_filter_name_et);
            if (filterNameUpdating == null) {
                if (filterName_et.length() > 0)
                    filterName_et.getText().clear();
            }
            else
                filterName_et.setText(filterNameUpdating);          // Delete previous text if existed

            saveFilterAlert = new AlertDialog.Builder(getActivity())
                    .setView(view)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String filterName = filterName_et.getText().toString().trim();
                            if (filterName.isEmpty()) {
                                filterName = getFilterTempName();
                                Toast.makeText(getActivity(), "This filter will be named " + filterName, Toast.LENGTH_LONG).show();
                            }

                            if (cliqFiltersMap.containsKey(filterName) && filterIdUpdating == -1) {
                                Toast.makeText(getActivity(), R.string.filter_save_filter_name_exists, Toast.LENGTH_LONG).show();
                                return;
                            }

                            SearchFilter.CliqSearchFilter newFilter = new SearchFilter.CliqSearchFilter(cliqFilter, filterName, userId);

                            cliqFiltersMap.put(filterName, newFilter);


                            Gson gson = new Gson();
                            Type type = new TypeToken<SearchFilter>() {}.getType();

                            JsonElement cliqFilterJson = gson.toJsonTree(cliqFilter, type);
                            parameters.add("cliq_filter_json", cliqFilterJson);
                            parameters.addProperty("cliq_filter_name", filterName);

                            boolean updating = isUpdatingExistingCliqFilter();
                            parameters.addProperty("cliq_filter_updating", updating);

                            if (updating)
                                parameters.addProperty("cliq_filter_id", filterIdUpdating);

                            sendSearchDataToServer(parameters);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .create();
        }
        saveFilterAlert.show();
    }

    /**
     * Checks whether the user chose an existing filter - and is updating it, or if the user created a new filter.
     * @return  True if updating, false otherwise
     */
    private boolean isUpdatingExistingCliqFilter(){
        return save_filter_checkbox.getText().toString().equalsIgnoreCase(getResources().getString(R.string.filter_search_update_cliq_filter));
    }

    /**
     * If the user didn't want to name the filter, we will create a temp name.
     * The temp name will be "filter" + a number.
     * @return  The temp name.
     */
    private String getFilterTempName() {
        if (cliqFiltersMap == null || cliqFiltersMap.isEmpty())
            return "filter1";

        int maxFilterNumber = 0;

        for (Map.Entry<String, SearchFilter.CliqSearchFilter> entry : cliqFiltersMap.entrySet()) {
            String name = entry.getKey();
            if (name.startsWith("filter")) {
                int index = name.indexOf("filter");
                String suffix = name.substring(index);

                if (suffix.charAt(0) > '9' || suffix.charAt(0) < '0')       // Making sure that the next character is a number
                    continue;

                try {
                    int i = Integer.parseInt(suffix);
                    if (i > maxFilterNumber)
                        maxFilterNumber = i;
                } catch (NumberFormatException e) { /* Ignore */ }
            }
        }
        return "filter" + ++maxFilterNumber;
    }

    /**
     * Creating (if needed) the filters dialog and showing it to the user.
     * Once a filter is selected in the dialog, we will load the filter to the cliq's edit texts.
     */
    private void showFiltersDialog() {
        if (loadFilterAlert == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final String[] items;

            if (cliqFiltersMap == null || cliqFiltersMap.isEmpty()) {
                if (cliqFiltersMap == null)
                    cliqFiltersMap = new HashMap<>();

                items = new String[1];
                items[0] = "New Filter";
            }
            else {
                items = new String[cliqFiltersMap.size() + 1];
                items[0] = "New Filter";
                int position = 1;
                for (Map.Entry<String, SearchFilter.CliqSearchFilter> e : cliqFiltersMap.entrySet())
                    items[position++] = e.getKey();
            }

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which != 0) {
                        save_filter_checkbox.setText(R.string.filter_search_update_cliq_filter);

                        SearchFilter.CliqSearchFilter selectedFilter = cliqFiltersMap.get(items[which]);

                        filterIdUpdating = selectedFilter.getFilterNumber();
                        filterNameUpdating = selectedFilter.getFilterName();
                        setDataInEditTexts(selectedFilter, false);
                    }
                    else {
                        filterIdUpdating = -1;
                        filterNameUpdating = null;
                        save_filter_checkbox.setText(R.string.filter_search_save_cliq_filter);
                        clearCliqEditTexts();
                    }
                }
            });
            loadFilterAlert = builder.create();
        }
        loadFilterAlert.show();
    }

    private void clearCliqEditTexts() {
        if (cliq_clothes_et.length() > 0)
            cliq_clothes_et.getText().clear();
        if (cliq_clothes_color_et.length() > 0)
            cliq_clothes_color_et.getText().clear();
        if (cliq_hair_color_et.length() > 0)
            cliq_hair_color_et.getText().clear();
        if (cliq_hair_style_et.length() > 0)
            cliq_hair_style_et.getText().clear();
        if (cliq_eye_color_et.length() > 0)
            cliq_eye_color_et.getText().clear();
        if (cliq_skin_color_et.length() > 0)
            cliq_skin_color_et.getText().clear();
        if (cliq_body_type_et.length() > 0)
            cliq_body_type_et.getText().clear();
        if (cliq_height_et.length() > 0)
            cliq_height_et.getText().clear();
    }

    /**
     * Sets the edit texts to the data received in the filter parameter.
     * If the user boolean is true, we will change the edit texts of the user.
     * Otherwise, we will change the edit texts of the cliq.
     * @param filter    The filter
     * @param user      true if the filter is of the user. false of of the cliq.
     */
    private void setDataInEditTexts(SearchFilter filter, boolean user) {
        if (filter == null)
            return;

        byte[] clothesCode = filter.getClothingCode();
        Integer hairStyleCode = filter.getHairStyleCode();
        Integer bodyTypeCode = filter.getBodyTypeCode();

        String clothes = SearchFilter.getClothingFromCode(clothesCode);
        String hairStyle = SearchFilter.getHairStyleFromCode(hairStyleCode);
        String bodyType = SearchFilter.getBodyTypeFromCode(bodyTypeCode);

        MyColor color;

        String height = Common.getHeightString(filter.getHeightInCm(), filter.isUnitsInMetric());

        if (user) {
            color = MyColor.getInstance(filter.getClothingColorAccurate());
            if (!color.isOther()) {
                my_clothes_color_et.setHint("");
                my_clothes_color_et.setBackgroundColor(color.getExactColorCode());
            }

            color = MyColor.getInstance(filter.getHairColorAccurate());
            if (!color.isOther()) {
                my_hair_color_et.setHint("");
                my_hair_color_et.setBackgroundColor(color.getExactColorCode());
            }

            color = MyColor.getInstance(filter.getEyesColorAccurate());
            if (!color.isOther()) {
                my_eye_color_et.setHint("");
                my_eye_color_et.setBackgroundColor(color.getExactColorCode());
            }

            color = MyColor.getInstance(filter.getSkinColorAccurate());
            if (!color.isOther()) {
                my_skin_color_et.setHint("");
                my_skin_color_et.setBackgroundColor(color.getExactColorCode());
            }

            my_clothes_et.setText(clothes);
            my_hair_style_et.setText(hairStyle);
            my_body_type_et.setText(bodyType);
            my_height_et.setText(height);
        }
        else {
            color = MyColor.getInstance(filter.getClothingColorAccurate());
            if (!color.isOther()) {
                cliq_clothes_color_et.setHint("");
                cliq_clothes_color_et.setBackgroundColor(color.getExactColorCode());
            }

            color = MyColor.getInstance(filter.getHairColorAccurate());
            if (!color.isOther()) {
                cliq_hair_color_et.setHint("");
                cliq_hair_color_et.setBackgroundColor(color.getExactColorCode());
            }

            color = MyColor.getInstance(filter.getEyesColorAccurate());
            if (!color.isOther()) {
                cliq_eye_color_et.setHint("");
                cliq_eye_color_et.setBackgroundColor(color.getExactColorCode());
            }

            color = MyColor.getInstance(filter.getSkinColorAccurate());
            if (!color.isOther()) {
                cliq_skin_color_et.setHint("");
                cliq_skin_color_et.setBackgroundColor(color.getExactColorCode());
            }

            cliq_clothes_et.setText(clothes);
            cliq_hair_style_et.setText(hairStyle);
            cliq_body_type_et.setText(bodyType);
            cliq_height_et.setText(height);
        }
    }

    private SearchFilter getDataFromEditTexts(boolean user) {
        String clothes, hairStyle, bodyType;

        Integer clothesColorAccurate = null,
                clothesColorList = null,
                hairColorAccurate = null,
                hairColorList = null,
                skinColorAccurate = null,
                skinColorList = null,
                eyeColorAccurate = null,
                eyeColorList = null;

        Integer heightInCm;
        boolean unitsInMetric = Common.isPreferredUnitsMetric(getActivity());

        String colorHintString;
        String heightString;

        boolean error = false;

        if (user) {
            clothes = my_clothes_et.getText().toString().trim();
            hairStyle = my_hair_style_et.getText().toString().trim();
            bodyType = my_body_type_et.getText().toString().trim();


            if (!hairStyle.isEmpty()) {
                if (!((MyStringArrayAdapter)my_hair_style_et.getAdapter()).containing(hairStyle)) {
                    error = true;
                    my_hair_style_et.setError("You must choose an item from the list");
                }
            }
            if (!bodyType.isEmpty()) {
                if (!((MyStringArrayAdapter) my_body_type_et.getAdapter()).containing(bodyType)) {
                    error = true;
                    my_body_type_et.setError("You must choose an item from the list");
                }
            }

            colorHintString = my_clothes_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {        // If empty, this means that the user selected a color and we removed the hint.
                clothesColorAccurate = my_clothes_color_et.getBackgroundColor();
                clothesColorList = MyColor.findClosest(clothesColorAccurate, my_clothes_color_et.getColorsFromColorsDialog());
            }

            colorHintString = my_hair_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {
                hairColorAccurate = my_hair_color_et.getBackgroundColor();
                hairColorList = MyColor.findClosest(hairColorAccurate, my_hair_color_et.getColorsFromColorsDialog());
            }

            colorHintString = my_eye_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {
                eyeColorAccurate = my_eye_color_et.getBackgroundColor();
                eyeColorList = MyColor.findClosest(eyeColorAccurate, my_eye_color_et.getColorsFromColorsDialog());
            }

            colorHintString = my_skin_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {
                skinColorAccurate = my_skin_color_et.getBackgroundColor();
                skinColorList = MyColor.findClosest(skinColorAccurate, my_skin_color_et.getColorsFromColorsDialog());
            }

            heightString = my_height_et.getText().toString().trim();
            heightInCm = Common.getHeightInCmFromString(heightString);

        }
        else {
            clothes = cliq_clothes_et.getText().toString().trim();
            hairStyle = cliq_hair_style_et.getText().toString().trim();
            bodyType = cliq_body_type_et.getText().toString().trim();


            if (!hairStyle.isEmpty()) {
                if (!((MyStringArrayAdapter)cliq_hair_style_et.getAdapter()).containing(hairStyle)) {
                    error = true;
                    cliq_hair_style_et.setError("You must choose an item from the list");
                }
            }
            if (!bodyType.isEmpty()) {
                if (!((MyStringArrayAdapter) cliq_body_type_et.getAdapter()).containing(bodyType)) {
                    error = true;
                    cliq_body_type_et.setError("You must choose an item from the list");
                }
            }


            colorHintString = cliq_clothes_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {        // If empty, this means that the user selected a color and we removed the hint.
                clothesColorAccurate = cliq_clothes_color_et.getBackgroundColor();
                clothesColorList = MyColor.findClosest(clothesColorAccurate, my_clothes_color_et.getColorsFromColorsDialog());
            }

            colorHintString = cliq_hair_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {
                hairColorAccurate = cliq_hair_color_et.getBackgroundColor();
                hairColorList = MyColor.findClosest(hairColorAccurate, cliq_hair_color_et.getColorsFromColorsDialog());
            }

            colorHintString = cliq_eye_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {
                eyeColorAccurate = cliq_eye_color_et.getBackgroundColor();
                eyeColorList = MyColor.findClosest(eyeColorAccurate, cliq_eye_color_et.getColorsFromColorsDialog());
            }

            colorHintString = cliq_skin_color_et.getHint().toString();
            if (colorHintString.isEmpty()) {
                skinColorAccurate = cliq_skin_color_et.getBackgroundColor();
                skinColorList = MyColor.findClosest(skinColorAccurate, cliq_skin_color_et.getColorsFromColorsDialog());
            }

            heightString = cliq_height_et.getText().toString().trim();
            heightInCm = Common.getHeightInCmFromString(heightString);
        }

        if (error)
            return null;

        return new SearchFilter(SearchFilter.getHairStyleCodeFromString(hairStyle),
                SearchFilter.getBodyTypeCodeFromString(bodyType),
                SearchFilter.getClothingCodeFromString(clothes),
                clothesColorList, clothesColorAccurate,
                hairColorList, hairColorAccurate,
                eyeColorList, eyeColorAccurate,
                skinColorList, skinColorAccurate,
                heightInCm, unitsInMetric);
    }


    /**
     * Checks if the user had changed anything about his appearance.
     * @param newUserData    The new data about the user.
     * @return  True if the new data equals the original data, false otherwise.
     */
    private boolean userProfileUpdated(SearchFilter newUserData) {
        if (this.userData == null)
            return newUserData.hasAnyData();
        return !newUserData.equals(this.userData);
    }

    @Override
    public void onDestroyView() {
        if (getInitialDataAsync != null)
            getInitialDataAsync.cancel(true);
        if (searchCliqAsync != null)
            searchCliqAsync.cancel(true);
        super.onDestroyView();
    }
}

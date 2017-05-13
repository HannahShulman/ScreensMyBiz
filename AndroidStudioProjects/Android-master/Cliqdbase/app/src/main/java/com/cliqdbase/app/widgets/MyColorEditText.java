package com.cliqdbase.app.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;

import com.cliqdbase.app.search_filter_stuff.MyColor;

import java.util.List;

/**
 * Created by Yuval on 07/09/2015.
 *
 * @author Yuval Siev
 */
public class MyColorEditText extends EditText {
    private int color;
    private AlertDialog colorsDialog;
    private Drawable originalDrawable;

    private List<MyColor> colorsInDialog;

    public MyColorEditText(Context context) {
        super(context);
        originalDrawable = getBackground();
        colorsInDialog = null;
    }

    public MyColorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        originalDrawable = getBackground();
        colorsInDialog = null;
    }

    public MyColorEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        originalDrawable = getBackground();
        colorsInDialog = null;
    }

    @Override
    public void setBackgroundColor(int color) {
        this.color = color;
        super.setBackgroundColor(color);
    }

    public int getBackgroundColor() {
        return color;
    }

    @SuppressWarnings("deprecation")
    public void restoreOriginalDrawable() {
        if (Build.VERSION.SDK_INT >= 16)
            setBackground(originalDrawable);
        else
            setBackgroundDrawable(originalDrawable);
    }

    public void setColorsDialog(AlertDialog colorsDialog, List<MyColor> colorsInDialog) {
        this.colorsDialog = colorsDialog;
        this.colorsInDialog = colorsInDialog;
    }

    public void showColorsDialog() {
        if (colorsDialog != null)
            colorsDialog.show();
    }

    public List<MyColor> getColorsFromColorsDialog() {
        /*if (colorsDialog == null)
            return null;

        Log.d("yuval", String.valueOf(colorsDialog.getListView() == null));
        Log.d("yuval", String.valueOf(colorsDialog.getListView().getAdapter() == null));

        return ((ColorsArrayAdapter)colorsDialog.getListView().getAdapter()).getItemsList();*/
        return colorsInDialog;
    }

}

package com.cliqdbase.app.general;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.cliqdbase.app.R;

/**
 * Created by Yuval on 11/03/2015.
 *
 * @author Yuval Siev
 */
public class Alert {

    private Activity calling_activity;

    /**
     * Constructs the alert class.
     * @param activity    The activity who created this alert object. (The context)
     */
    public Alert (Activity activity) {
        this.calling_activity = activity;
    }

    /**
     * Displays an alertDialog on top of the calling (current) activity, with the given data.
     * @param title              The Title of the alertDialog.
     * @param message            The message in the alertDialog.
     */
    public void showAlertDialog(String title, String message) {
        buildAlert(title,message).show();
    }

    /**
     * Builds an alert dialog.
     * @param title      The Title of the alertDialog.
     * @param message    The message in the alertDialog.
     * @return The alert dialog built.
     */
    public AlertDialog.Builder buildAlert(String title, String message) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this.calling_activity);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(message);
        alertBuilder.setNegativeButton(R.string.close_alert, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return alertBuilder;
    }
}

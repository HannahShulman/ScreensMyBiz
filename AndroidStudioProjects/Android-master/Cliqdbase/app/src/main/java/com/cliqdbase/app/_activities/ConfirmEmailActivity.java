package com.cliqdbase.app._activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.ServerUrlConstants;

import java.util.List;

public class ConfirmEmailActivity extends Activity implements AsyncResponse_Server{

    private static final int CTSC_CONFIRM = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_email);


        // This activity must receive a confirmation code.
        String confirmationCode = extractConfirmationCodeFromIntent(getIntent());
        if (confirmationCode == null || confirmationCode.isEmpty()) {
            Toast.makeText(ConfirmEmailActivity.this, "Could not find the confirmation code", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Sending the confirmation code to the server
        ConnectToServer async = new ConnectToServer(ConfirmEmailActivity.this, true, CTSC_CONFIRM);
        async.delegate = this;
        async.execute(ServerUrlConstants.CONFIRM_EMAIL + confirmationCode, "GET");
    }


    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        if (taskCode != CTSC_CONFIRM)
            return;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ConfirmEmailActivity.this);
        dialogBuilder.setCancelable(false);
        switch (httpResultCode) {               // server responses handler
            case 200:
                dialogBuilder.setTitle("Account confirmed!")
                        .setMessage("Your account has been confirmed.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(ConfirmEmailActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                ConfirmEmailActivity.this.finish();
                            }
                        });
                break;
            case 400:
                dialogBuilder.setTitle("Account not confirmed!")
                        .setMessage("Your account hasn't been confirmed.\nThere is an error in your confirmation code.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConfirmEmailActivity.this.finish();
                            }
                        });
                break;
            default:
                dialogBuilder.setTitle("Internal server error")
                        .setMessage("Your account hasn't been confirmed due to an internal server error.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ConfirmEmailActivity.this.finish();
                            }
                        });
                break;
        }

        dialogBuilder.create().show();
    }


    /**
     * Extracting the confirmation code from the intent
     * @return the confirmation code
     **/
    private String extractConfirmationCodeFromIntent(Intent intent) {
        Uri uri = intent.getData();

        if (uri != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            List<String> segments = uri.getPathSegments();
            if (segments.size() == 2)
                return segments.get(1);
        }

        return null;
    }

}

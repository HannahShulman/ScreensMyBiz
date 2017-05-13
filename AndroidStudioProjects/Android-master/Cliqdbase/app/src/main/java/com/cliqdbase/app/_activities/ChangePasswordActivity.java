package com.cliqdbase.app._activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.constants.AppConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.general.Common;
import com.cliqdbase.app.general.InputValidation;
import com.cliqdbase.app.general.PasswordHashing;
import com.google.gson.JsonObject;

public class ChangePasswordActivity extends Activity implements View.OnClickListener, AsyncResponse_Server{

    private EditText oldPass_et;
    private EditText newPass_et;
    private EditText repeatNewPass_et;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Common.isLoggedInAsGuest(this) || Common.isLoggedInUsingFacebook(this))
            finish();

        setContentView(R.layout.activity_change_password);

        findViewsById();

        this.sendButton.setOnClickListener(this);
    }

    private void findViewsById() {
        this.oldPass_et = (EditText) findViewById(R.id.change_password_old_pass);
        this.newPass_et = (EditText) findViewById(R.id.change_password_new_pass);
        this.repeatNewPass_et = (EditText) findViewById(R.id.change_password_new_pass_repeat);
        this.sendButton = (Button) findViewById(R.id.change_password_send_button);
    }


    /**
     * Checks that the input of the user is valid: the passwords match, and the password complexity is acceptable.
     *
     * If the input is invalid, a message will be shown to the user.
     *
     * @return true if the input is valid, false otherwise.
     */
    private boolean validateInput() {
        String oldPass = this.oldPass_et.getText().toString();
        String newPass = this.newPass_et.getText().toString();
        String repeatNewPass = this.repeatNewPass_et.getText().toString();

        boolean flag1 = newPass.equals(repeatNewPass);

        if (!flag1)
            this.repeatNewPass_et.setError("Passwords must match");
        else
            this.repeatNewPass_et.setError(null);


        boolean flag2 = InputValidation.validPassword(newPass);

        if (!flag2)
            this.newPass_et.setError(R.string.password_error + Integer.toString(AppConstants.PASSWORD_MINIMUM_LENGTH));
        else
            this.newPass_et.setError(null);


        boolean flag3 = oldPass.isEmpty();

        if (flag3)
            this.oldPass_et.setError("Old password can't be empty");
        else
            this.oldPass_et.setError(null);


        return flag1 && flag2 && !flag3;
    }


    @Override
    public void onClick(View v) {
        // If the user had clicked on the 'change password' button and the input is valid: start the password-changing process
        if (v.getId() == R.id.change_password_send_button && validateInput()) {
            if (!Common.isLoggedInUsingFacebook(this)) {        // a user can change his password only if he didn't sign up using facebook.
                String oldPass = oldPass_et.getText().toString();
                String newPass = newPass_et.getText().toString();

                String oldPassHash = PasswordHashing.hashPass(oldPass);     // Hashing the passwords: we don't want to send un-hashed password over the internet.
                String newPassHash = PasswordHashing.hashPass(newPass);

                JsonObject json = new JsonObject();
                json.addProperty("old_password", oldPassHash);
                json.addProperty("new_password", newPassHash);

                ConnectToServer async = new ConnectToServer(ChangePasswordActivity.this, true, 0);
                async.delegate = this;
                async.execute(ServerUrlConstants.CHANGE_PASS_SERVLET, "POST", json.toString());
            }
            else {
                Toast.makeText(this, "Since you used Facebook to login, You don't have a password to change.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        switch (httpResultCode) {
            case 200:
                Toast.makeText(ChangePasswordActivity.this, "Password Changes Successfully", Toast.LENGTH_LONG).show();
                finish();
                break;
            case 401:
                new AlertDialog.Builder(ChangePasswordActivity.this)
                        .setTitle(R.string.old_password_incorrect_title)
                        .setMessage(R.string.old_password_incorrect_message)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
            default:
                new AlertDialog.Builder(ChangePasswordActivity.this)
                        .setTitle(R.string.error)
                        .setMessage(R.string.server_error_500)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
        }
    }

}

package com.cliqdbase.app._activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import com.cliqdbase.app.general.InputValidation;
import com.cliqdbase.app.general.PasswordHashing;
import com.google.gson.JsonObject;

import java.util.List;

public class ResetPassActivity extends Activity implements View.OnClickListener, AsyncResponse_Server{

    private EditText password_et;
    private EditText repeat_password_et;
    private Button send_button;

    private String confirmationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pass);

        findViewsById();

        this.confirmationCode = extractConfirmationCode();
        if (this.confirmationCode == null) {
            Toast.makeText(ResetPassActivity.this, "Could not find the confirmation code", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        this.send_button.setOnClickListener(this);
    }

    private void findViewsById() {
        this.password_et = (EditText) findViewById(R.id.reset_pass_password_et);
        this.repeat_password_et = (EditText) findViewById(R.id.reset_pass_repeat_password_et);
        this.send_button = (Button) findViewById(R.id.reset_pass_send_button);
    }


    /**
     * Extracting the reset-passwird confirmation code from the intent.
     * @return  The confirmation code.
     */
    private String extractConfirmationCode() {
        Intent intent = getIntent();
        Uri uri = intent.getData();

        if (uri != null && intent.getAction().equals(Intent.ACTION_VIEW)) {
            List<String> segments = uri.getPathSegments();
            if (segments.size() == 2)
                return segments.get(1);
        }

        return null;
    }


    private boolean validateInput() {
        String password = this.password_et.getText().toString();
        String repeatPassword = this.repeat_password_et.getText().toString();


        boolean flag1 = password.equals(repeatPassword);

        if (!flag1)
            this.repeat_password_et.setError("Passwords must match");
        else
            this.repeat_password_et.setError(null);


        boolean flag2 = InputValidation.validPassword(password);

        if (!flag2)
            this.password_et.setError(R.string.password_error + Integer.toString(AppConstants.PASSWORD_MINIMUM_LENGTH));
        else
            this.password_et.setError(null);

        return flag1 && flag2;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reset_pass_send_button) {
            if (validateInput()) {
                String password = this.password_et.getText().toString();
                String hash = PasswordHashing.hashPass(password);

                JsonObject json = new JsonObject();
                json.addProperty("confirmation-code", this.confirmationCode);
                json.addProperty("password", hash);

                ConnectToServer async = new ConnectToServer(ResetPassActivity.this, true, 0);
                async.delegate = this;
                async.execute(ServerUrlConstants.RESET_PASS_SERVLET, "POST", json.toString());
            }
        }
    }


    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ResetPassActivity.this);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(ResetPassActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        switch (httpResultCode) {
            case 200:
                builder.setTitle(R.string.success)
                        .setMessage(R.string.password_changed_successfully);
                break;
            case 400:
                builder.setTitle(R.string.error)
                        .setMessage(R.string.confirmation_code_error);
                break;
            case 500:
                builder.setTitle(R.string.error)
                        .setMessage(R.string.server_error_500);
                break;
        }

        builder.create().show();
    }
}

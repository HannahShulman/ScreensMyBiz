package com.cliqdbase.app.async.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cliqdbase.app.R;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.constants.SharedPreferencesConstants;
import com.cliqdbase.app.general.Common;


/**
 * Created by Yuval on 06/03/2015.
 *
 * @author Yuval Siev
 */
public class ConnectToServer extends AsyncTask<String, Void, String> {

    private ProgressDialog dialog;
    private Context context;
    public AsyncResponse_Server delegate = null;

    private long taskCode;
    private int httpResCode;


    /**
     * Creates a new AsyncTask object.
     * If showDialog is true, a progress dialog will be displayed
     * @param activity      The activity calling this AsyncTask
     * @param showDialog    True if you want to display a progress dialog and false otherwise
     */
    public ConnectToServer(Context activity, boolean showDialog, long taskCode) {
        this.context = activity;
        this.taskCode = taskCode;
        if (showDialog)
            this.dialog = new ProgressDialog(this.context);     // Creating the progressDialog in the current activity
        else
            this.dialog = null;

        httpResCode = 0;
    }

    @Override
    protected void onPreExecute() {
        if (this.dialog != null) {
            this.dialog.setCancelable(false);
            this.dialog.setMessage(context.getText(R.string.please_wait));
            this.dialog.show();
        }
        System.out.println("Async Started");
    }

    @Override
    protected String doInBackground(String... connectionParams) {
        if (connectionParams.length < 2)
            return "Not enough input parameters";
        String servletUrl = connectionParams[0];                   // The servlet URL is in the first cell of the array
        String url = ServerUrlConstants.SERVER_HOST + servletUrl;

        String method = connectionParams[1].toUpperCase();                // The method (GET/POST) will be in the second cell.

        String urlParam = null;
        if (connectionParams.length > 2)
            urlParam = connectionParams[2];          // The parameters to send to the servlet will be in the third cell.

        if (!method.equals("GET") && !method.equals("POST"))        // Input checks.
            return "Wrong Method";

        return executeServletOnServer(url, method, urlParam, true);
    }


    @Override
    protected void onPostExecute(String result) {
        System.out.println("Async Finished");
        if (this.dialog != null)
            this.dialog.dismiss();

        if (this.httpResCode == -1) {    // This means that there was no internet connection available
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return;
        }
        else if (this.httpResCode == 401) {
            Common.received401FromServer(context);
            return;
        }

        if (delegate != null)
            delegate.onServerResponse(this.taskCode, this.httpResCode, result);     // Running the onServerResponse function of the activity who called this Async task
    }


    /**
     * Connects to the server and runs the servlet if there is an internet connection. If there is no internet connection, Displays a toast to the user.
     * @param url           The url of the servlet.
     * @param method        The method of the servlet (get or post)
     * @param parameters    The parameters to send to the servlet, can be null.
     * @param fromAsync     True if this function is called from the doInBackground() function, or false if called from outside.    (needed to know whether to call on post execute manually or not.
     * @return  The response from the server. If there was none, an empty string. never null.
     */
    public @NonNull String executeServletOnServer(@NonNull String url,@NonNull String method,@Nullable String parameters, boolean fromAsync) {

        if (!Common.isNetworkAvailable(context)) {
            this.httpResCode = -1;
            return "";
        }


        StringBuilder response = new StringBuilder("");

        SharedPreferences prefs = this.context.getSharedPreferences(SharedPreferencesConstants.GLOBAL_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE);
        try {
            URL login_url = new URL(url);

            HttpURLConnection server_conn = (HttpURLConnection) login_url.openConnection();  // Opening a connection to the login servlet
            server_conn.setConnectTimeout(2*60*1000);        // Timeout = 2 Minutes
            server_conn.setRequestMethod(method);

            String cookie = prefs.getString(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID, null);          // Extract the cookie of there is one
            if (cookie != null)
                server_conn.setRequestProperty("Cookie", cookie);

            server_conn.setUseCaches(false);
            server_conn.setDoInput(true);

            if (parameters != null) {
                server_conn.setDoOutput(true);
                server_conn.setRequestProperty("Content-Length", Integer.toString(parameters.getBytes().length));
                DataOutputStream writer = new DataOutputStream(server_conn.getOutputStream());
                writer.write(parameters.getBytes("UTF-8"));
                writer.flush();
                writer.close();
            }

            if (cookie == null) {           // If there is not a cookie in the shared preferences, we must set the cookie if the server returns one.
                String header;
                for (int i = 1; (header = server_conn.getHeaderFieldKey(i)) != null; i++) {
                    if (header.equals("Set-Cookie")) {              // Finding the cookie sent from the server
                        cookie = server_conn.getHeaderField(i);

                        // If this is indeed the cookie that contain the session id.  THIS COOKIE WILL BE SENT IN LOGIN/REGISTER ONLY!
                        if (cookie.substring(0, cookie.indexOf("=")).equals(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID))
                            prefs.edit().putString(SharedPreferencesConstants.SHARED_PREFERENCES_KEY_COOKIE_SESSION_ID, cookie).apply();                     // this will put the whole cookie in the shared preferences. "sessionId=*"

                    }
                }
            }

            this.httpResCode = server_conn.getResponseCode();

            if (this.httpResCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(server_conn.getInputStream()));
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }

            server_conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = response.toString();

        Log.d("ConnectToServer", "Result Code: " + this.httpResCode);
        if (!result.isEmpty())
            Log.d("ConnectToServer", "Message: " + result);

        if (!fromAsync)
            onPostExecute(result);

        return result;
    }

    public int getHttpResCode() {
        return httpResCode;
    }
}

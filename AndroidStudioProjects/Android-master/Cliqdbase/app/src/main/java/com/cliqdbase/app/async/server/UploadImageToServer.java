package com.cliqdbase.app.async.server;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cliqdbase.app.R;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.general.Common;

/**
 * Created by Yuval on 08/04/2015.
 *
 * @author Yuval Siev
 */
public class UploadImageToServer extends AsyncTask<Void, Integer, Integer> {

    private ProgressDialog dialog;
    private Context context;
    private Bitmap imageBitmap;

    private AsyncResponse_Server callback;

    public UploadImageToServer(Context context,@NonNull Bitmap bitmap, AsyncResponse_Server callback) {
        this.context = context;
        this.imageBitmap = bitmap;
        this.callback = callback;

        this.dialog = new ProgressDialog(this.context);     // Creating the progressDialog in the current activity
        this.dialog.setCancelable(false);
        this.dialog.setMessage(context.getString(R.string.please_wait));
    }

    @Override
    protected void onPreExecute() {
        this.dialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this.dialog.setProgress(values[0]);
    }

    @Override
    protected Integer doInBackground(Void... connectionParams) {
        if (!Common.isNetworkAvailable(context))
            return null;

        int response = -1;

        String url = ServerUrlConstants.SERVER_HOST + ServerUrlConstants.UPLOAD_PROFILE_IMAGE;                 // The servlet URL
        
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;


        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapData = bos.toByteArray();
            bos.close();
            ByteArrayInputStream imageInputStream = new ByteArrayInputStream(bitmapData);

            URL login_url = new URL(url);

            HttpURLConnection server_conn = (HttpURLConnection) login_url.openConnection();  // Opening a connection to the login servlet
            server_conn.setDoOutput(true);
            server_conn.setRequestMethod("POST");
            server_conn.setRequestProperty("Content-Length", Long.toString(bitmapData.length));


            String cookie = Common.getSessionId(context);          // Extract the cookie of there is one
            if (cookie != null)
                server_conn.setRequestProperty("Cookie", cookie);
            else
                return 401;


            DataOutputStream writer = new DataOutputStream(server_conn.getOutputStream());

            bytesAvailable = imageInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            bytesRead = imageInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {         // Writing the image file's bytes to the output stream writer
                writer.write(buffer, 0, bufferSize);
                bytesAvailable = imageInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = imageInputStream.read(buffer, 0, bufferSize);
            }
            imageInputStream.close();


            writer.flush();
            writer.close();

            response = server_conn.getResponseCode();

            server_conn.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }


    @Override
    protected void onPostExecute(Integer result) {
        System.out.println(result);
        this.dialog.cancel();

        String message = "";

        if (result == null) {
            Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            return;
        }
        switch (result) {
            case 200:
                message = context.getString(R.string.image_upload_successful);
                break;
            case 401:
                Common.received401FromServer(context);
                break;
            default:
                message = context.getString(R.string.image_upload_fail);
                break;
        }


        if (!message.isEmpty())
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        if (callback != null)
            callback.onServerResponse(-1, result, null);
    }

}

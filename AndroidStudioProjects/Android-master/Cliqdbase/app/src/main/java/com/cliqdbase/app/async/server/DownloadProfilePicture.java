package com.cliqdbase.app.async.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.general.Common;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Yuval on 03/05/2015.
 *
 * @author Yuval Siev
 */
public class DownloadProfilePicture extends AsyncTask<Void, Void, Bitmap> {

    private Context context;
    private long userId;
    private ImageDownloaded callback;


    public DownloadProfilePicture(@NonNull Context context, long userId, @NonNull ImageDownloaded callback) {
        this.context = context;
        this.userId = userId;
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        Log.d("yuval", "Started downloading image");
        String urlDisplay = ServerUrlConstants.SERVER_HOST + ServerUrlConstants.GET_USER_PROFILE_PICTURE + (userId != -1 ? "/user/" + userId : ""); // If downloading the user's image, no need to add userId.

        Bitmap imageBitmap = null;

        if (!Common.isNetworkAvailable(context))
            return null;

        try {
            URL url = new URL(urlDisplay);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setConnectTimeout(2*60*1000);        // Timeout = 2 Minutes
            conn.setRequestMethod("GET");

            String cookie = Common.getSessionId(context);          // Extract the cookie of there is one
            if (cookie != null)
                conn.setRequestProperty("Cookie", cookie);
            else
                return null;        // The user must have a session id (must be logged in) in order to view profile images.

            InputStream inputStream = conn.getInputStream();
            imageBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        Log.d("yuval", "Finished downloading image");
        callback.onImageDownloaded(bitmap);
    }
}

package com.cliqdbase.app.async.google_maps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Yuval on 13/07/2015.
 *
 * @author Yuval Siev
 */
public class GetLocationListAsync extends AsyncTask<Void, Integer, List<String>> {
    private static final int GEOCODER_MAX_RESULTS = 15;

    private GetLocationListAsyncCallback callback;
    private String query;
    private Context context;

    public GetLocationListAsync(Context context, String query, GetLocationListAsyncCallback callback) {
        this.context = context;
        this.callback = callback;
        this.query = query;
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        List<String> addressesToDisplay;

        if (query == null || query.isEmpty())
            addressesToDisplay = getPlacesFromCurrentLocation();
        else
            addressesToDisplay = getPlacesFromStringQuery(query);

        return addressesToDisplay;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        switch (values[0]) {
            case 1:
                Toast.makeText(context, "Couldn't receive location from GPS, using network instead. This may result in bad results.", Toast.LENGTH_LONG).show();
                break;
            case 2:
                Toast.makeText(context, "Couldn't receive last known location. Make sure that your location services are on.", Toast.LENGTH_LONG).show();
                break;
            case 3:
                Toast.makeText(context, "Couldn't receive data from google. Make sure that you are connected to the internet or try again.", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    protected void onPostExecute(List<String> addresses) {
        callback.callback(addresses);
    }

    private List<String> getPlacesFromStringQuery(String query) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(query, GEOCODER_MAX_RESULTS);
        } catch (IOException e) {
            publishProgress(3);
            e.printStackTrace();
            return null;
        }

        return extractStringsFromAddressList(addressList);
    }


    private List<String> getPlacesFromCurrentLocation() {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addressList;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    public void requestPermissions(@NonNull String[] permissions, int requestCode)
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return null;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null)
                publishProgress(1);
        }

        if (location == null) {
            publishProgress(2);
            return null;
        }


        try {
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), GEOCODER_MAX_RESULTS);
        } catch (IOException e) {
            publishProgress(3);
            e.printStackTrace();
            return null;
        }

        return extractStringsFromAddressList(addressList);
    }


    private List<String> extractStringsFromAddressList(List<Address> addressList) {
        if (addressList == null)
            return null;

        List<String> addressesToDisplay = new ArrayList<>();
        for (Address address : addressList) {
            //String name = address.getFeatureName();

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                builder.append(address.getAddressLine(i));
                builder.append(' ');
            }
            String name = builder.toString();

            if (!addressesToDisplay.contains(name))
                addressesToDisplay.add(name);
        }

        return addressesToDisplay;
    }


}

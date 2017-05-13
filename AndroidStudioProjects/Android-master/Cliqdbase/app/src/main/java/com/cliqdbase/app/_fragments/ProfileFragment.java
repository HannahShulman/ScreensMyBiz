package com.cliqdbase.app._fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cliqdbase.app.R;
import com.cliqdbase.app._activities.ChangeProfilePictureActivity;
import com.cliqdbase.app._activities.UpdateProfileActivity;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.async.server.DownloadProfilePicture;
import com.cliqdbase.app.async.server.ImageDownloaded;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;
import com.cliqdbase.app.general.Logout;
import com.cliqdbase.app.server_model.UserProfile;

public class ProfileFragment extends ListFragment implements AsyncResponse_Server, ImageDownloaded {

    private long userId;
    private String firstName;
    private String lastName;
    private Integer cityCode;
    private String city;
    private String country;
    private long birthdayMillis;

    private View listHeader;

    ProgressBar imageDownloadingProgressBar;
    ImageView profilePictureImageView;

    private ConnectToServer getProfile;
    private DownloadProfilePicture getProfilePicture;

    private final int UPDATE_PROFILE_RS = 1;        // The code for the UpdateProfileActivity result

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.fragment_profile, container, false);

        Bundle intentExtras = getActivity().getIntent().getExtras();
        this.userId = -1;
        if (intentExtras != null)
            this.userId = intentExtras.getLong(IntentConstants.INTENT_EXTRA_USER_ID, -1);

        getProfile = null;
        getProfilePicture = null;

        downloadProfileInfo();

        return view;
    }


    /**
     * Downloads the profile info.
     * if the user id is -1, we will download the data of the user's profile.
     * else, we will download the data of the user with the given id.
     */
    private void downloadProfileInfo() {
        String url;
        if (this.userId == -1)
            url = ServerUrlConstants.GET_MY_USER_PROFILE;            // Downloading the data of my profile
        else
            url = ServerUrlConstants.GET_USER_PROFILE + this.userId;      // Downloading the data of the given user's profile


        getProfile = new ConnectToServer(getActivity(), false, 0);      // We don't want to display the loading window.
        getProfile.delegate = this;
        getProfile.execute(url, "GET");
    }

    private void downloadProfileImage() {
        getProfilePicture = new DownloadProfilePicture(getActivity(), userId, this);
        getProfilePicture.execute();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onImageDownloaded(@Nullable Bitmap imageBitMap) {
        if (profilePictureImageView == null)
            return;

        if (imageDownloadingProgressBar != null)
            imageDownloadingProgressBar.setVisibility(View.GONE);

        if (imageBitMap != null) {
            profilePictureImageView.setImageBitmap(scaleBitmap(imageBitMap, 360));
        }
        else {
            if (Build.VERSION.SDK_INT < 21)
                profilePictureImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_image));
            else
                profilePictureImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_image, null));
        }
    }

    /**
     * Scales a bitmap to fit a square with the given height.
     * This method keeps the aspect ratio of the bitmap.
     * @param source          The source bitmap.
     * @param scaledHeight    The height the returned bitmap should have.
     * @return  A scaled bitmap to fit the given height, with the same aspect ratio.
     */
    private Bitmap scaleBitmap(Bitmap source, int scaledHeight) {
        if (source == null)
            return null;
        int scaledWidth = (int) (source.getWidth() * scaledHeight/((double)source.getHeight()));
        return Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile, menu);

        if (this.userId != -1) {
            menu.removeItem(R.id.action_edit_profile);
            menu.removeItem(R.id.action_change_picture);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_picture:
                Intent intent = new Intent(getActivity(), ChangeProfilePictureActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_edit_profile:
                Intent editProfileIntent = new Intent(getActivity(), UpdateProfileActivity.class);
                editProfileIntent.putExtra("firstName", this.firstName);
                editProfileIntent.putExtra("lastName", this.lastName);
                editProfileIntent.putExtra("cityCode", this.cityCode);
                editProfileIntent.putExtra("city", this.city);
                editProfileIntent.putExtra("country", this.country);
                editProfileIntent.putExtra("birthday", this.birthdayMillis);
                startActivityForResult(editProfileIntent, UPDATE_PROFILE_RS);
                return true;
            case R.id.action_logout:
                Logout.logout(getActivity());
                return true;
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UPDATE_PROFILE_RS) {
            boolean updated = data.getBooleanExtra(IntentConstants.INTENT_EXTRA_PROFILE_UPDATED, true);
            if (resultCode == Activity.RESULT_OK && updated) {
                getListView().setAdapter(null);
                getListView().removeHeaderView(this.listHeader);
                downloadProfileInfo();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        // Interpreting the result
        boolean showErrorAlertMessage = false;

        UserProfile profile = null;

        if (httpResultCode != 200)
            showErrorAlertMessage = true;
        else {
            profile = UserProfile.getUserFromJson(data);        // Extracting the given profile from the data.

            if (profile == null)
                showErrorAlertMessage = true;
        }
        if (showErrorAlertMessage) {
            AlertDialog alert = new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.cant_load_profile_info)
                    .setPositiveButton(R.string.close_alert, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().getSupportFragmentManager().beginTransaction().remove(ProfileFragment.this).commit(); // Closing the fragment
                        }
                    })
                    .setCancelable(false)
                    .create();
            alert.show();
            return;
        }

        //Configuring the header.
        this.listHeader = View.inflate(getActivity(), R.layout.profile_list_view_header, null);
        profilePictureImageView = (ImageView) listHeader.findViewById(R.id.user_profile_image_view);
        imageDownloadingProgressBar = (ProgressBar) listHeader.findViewById(R.id.profile_image_progressBar);

        TextView profileName = (TextView) listHeader.findViewById(R.id.user_profile_name);
        TextView profileAge = (TextView) listHeader.findViewById(R.id.user_profile_age);
        TextView profileCity = (TextView) listHeader.findViewById(R.id.user_profile_location);


        this.firstName = profile.getFirstName();
        this.lastName = profile.getLastName();
        this.city = profile.getCity();
        this.country = profile.getCountry();
        this.birthdayMillis = profile.getBirthdayMillis();
        this.cityCode = profile.getCityCode();

        profileName.setText(profile.getFullName());
        // TODO fix that
        profileAge.setText(Integer.toString(profile.getAge()));
        String city = profile.getCity();
        if (city != null)
            profileCity.setText(city);

        getListView().addHeaderView(listHeader);


        downloadProfileImage();     // Downloading the profile picture of the user.


        // List of cliqs. This is only a temp list.
        String[] items = {"a", "b", "c", "42", "test1", "test2", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3", "test3"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);

        getListView().setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        if (getProfile != null && !getProfile.getStatus().equals(AsyncTask.Status.FINISHED))
            getProfile.cancel(true);
        if (getProfilePicture != null && !getProfilePicture.getStatus().equals(AsyncTask.Status.FINISHED))
            getProfilePicture.cancel(true);
        super.onDestroyView();
    }
}

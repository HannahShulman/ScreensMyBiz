package com.cliqdbase.app._activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.cliqdbase.app.R;
import com.cliqdbase.app.async.server.AsyncResponse_Server;
import com.cliqdbase.app.async.server.ConnectToServer;
import com.cliqdbase.app.async.server.DownloadProfilePicture;
import com.cliqdbase.app.async.server.ImageDownloaded;
import com.cliqdbase.app.async.server.UploadImageToServer;
import com.cliqdbase.app.constants.IntentConstants;
import com.cliqdbase.app.constants.ServerUrlConstants;

@SuppressWarnings("deprecation")
public class ChangeProfilePictureActivity extends Activity implements AsyncResponse_Server, View.OnClickListener, ImageDownloaded {

    // Intent request codes
    private static final int IMAGE_GALLERY_REQUEST_CODE = 42;
    private static final int IMAGE_CAMERA_REQUEST_CODE = 41;

    // Connect to server task codes
    private static final long CTS_REMOVE_IMAGE = 3;

    private Uri capturedImageUri;
    private Bitmap selectedImageBitmap;

    private ImageView profileImageView;
    private ProgressBar profileImageDownloading;

    private DownloadProfilePicture getProfilePicture;

    private boolean signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile_pic);

        Intent callingIntent = getIntent();
        Bundle intentExtras = callingIntent.getExtras();

        this.signUp = intentExtras != null && intentExtras.getBoolean(IntentConstants.INTENT_EXTRA_SIGNUP, false);

        TextView title_tv = (TextView) findViewById(R.id.change_pic_title);
        Button cancel_button = (Button) findViewById(R.id.change_picture_cancel_button);
        Button upload_image_button = (Button) findViewById(R.id.upload_image_button);
        ImageView profile_image_change_button = (ImageView) findViewById(R.id.profile_image_edit_button);
        profileImageDownloading = (ProgressBar) findViewById(R.id.current_profile_image_downloading);

        cancel_button.setOnClickListener(this);
        upload_image_button.setOnClickListener(this);
        profile_image_change_button.setOnClickListener(this);

        profileImageView = (ImageView) findViewById(R.id.profile_image_selected);

        if (this.signUp) {
            title_tv.setText(R.string.change_picture_title_after_sign_up);
            cancel_button.setText(R.string.skip);

            setDefaultProfilePicture();             // Setting the default profile image to the image view.
        }
        else {
            title_tv.setText(R.string.change_picture_title_regular);
            cancel_button.setText(R.string.cancel);

            downloadCurrentProfilePicture();        // Downloading the current profile picture. There is no profile image if the user had just sign up.
        }

        // Setting the pencil mark on the edit-image image view.
        if (Build.VERSION.SDK_INT < 21)
            profile_image_change_button.setImageDrawable(getResources().getDrawable(R.drawable.pencil_edit));
        else
            profile_image_change_button.setImageDrawable(getResources().getDrawable(R.drawable.pencil_edit, null));
    }

    private void downloadCurrentProfilePicture() {
        getProfilePicture = new DownloadProfilePicture(this, -1, this);
        getProfilePicture.execute();
    }

    @Override
    public void onImageDownloaded(@Nullable Bitmap imageBitMap) {
        if (imageBitMap == null)
            setDefaultProfilePicture();
        else {
            profileImageDownloading.setVisibility(View.GONE);
            profileImageView.setImageBitmap(scaleBitmap(imageBitMap, 360));
        }
    }

    private void setDefaultProfilePicture() {
        profileImageDownloading.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT < 21)
            profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_image));
        else
            profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_image, null));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_picture_cancel_button:
                startNextActivity();
                break;
            case R.id.upload_image_button:                  // After image selection, upload the image to the server
                System.out.println("Starting image upload");
                if (selectedImageBitmap != null) {
                    UploadImageToServer uploadImageToServer = new UploadImageToServer(this, selectedImageBitmap, new AsyncResponse_Server() {
                        @Override
                        public void onServerResponse(long taskCode, int httpResultCode, String data) {
                            startNextActivity();
                        }
                    });
                    uploadImageToServer.execute();
                }
                else
                    startNextActivity();        // If the bitmap is null, no new image selected and therefore there is no need to contact the server. We will simply continue to the next activity.
                break;

            case R.id.profile_image_edit_button:                // Choose image - select from gallery or capture
                // The options:
                String[] options = {"Take a picture", "Choose from gallery", "Remove image"};

                AlertDialog imageOption = new AlertDialog.Builder(ChangeProfilePictureActivity.this)
                        .setTitle("Choose a profile photo")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:         // Capture image
                                        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        if (takePicture.resolveActivity(getPackageManager()) != null) {
                                            File photoFile = createImageFile();
                                            if (photoFile != null) {
                                                capturedImageUri = Uri.fromFile(photoFile);
                                                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                                                startActivityForResult(takePicture, IMAGE_CAMERA_REQUEST_CODE);
                                            }
                                        }
                                        else
                                            Toast.makeText(ChangeProfilePictureActivity.this, R.string.cant_capture_image_from_camera, Toast.LENGTH_LONG).show();
                                        break;

                                    case 1:         // Select from gallery
                                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                        pickPhoto.setType("image/*");
                                        startActivityForResult(pickPhoto, IMAGE_GALLERY_REQUEST_CODE);
                                        break;

                                    case 2:
                                        ConnectToServer removeImageAsync = new ConnectToServer(ChangeProfilePictureActivity.this, true, CTS_REMOVE_IMAGE);
                                        removeImageAsync.delegate = ChangeProfilePictureActivity.this;
                                        removeImageAsync.execute(ServerUrlConstants.REMOVE_PROFILE_IMAGE, "GET");
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();

                imageOption.show();
                break;
        }
    }

    @Override
    public void onServerResponse(long taskCode, int httpResultCode, String data) {
        if (taskCode == CTS_REMOVE_IMAGE) {
            if (httpResultCode == 200) {
                if (Build.VERSION.SDK_INT < 21)
                    profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_image));
                else
                    profileImageView.setImageDrawable(getResources().getDrawable(R.drawable.default_profile_image, null));
                Toast.makeText(ChangeProfilePictureActivity.this, R.string.image_removal_success, Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(ChangeProfilePictureActivity.this, R.string.image_removal_failure, Toast.LENGTH_LONG).show();
            startNextActivity();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case IMAGE_CAMERA_REQUEST_CODE:
                    setSelectedBitmapToImageOnDevice(capturedImageUri);

                    Bitmap scaledBitmap = scaleBitmap(selectedImageBitmap, 360);
                    profileImageView.setImageBitmap(scaledBitmap);
                    break;

                case IMAGE_GALLERY_REQUEST_CODE:
                    handleGalleryResult(data);
                    break;
            }
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


    private void setSelectedBitmapToImageOnDevice(Uri imageUri) {
        selectedImageBitmap = getBitmapFromUriOfGalleryImageOnDevice(imageUri);
    }

    private Bitmap getBitmapFromUriOfGalleryImageOnDevice(Uri imageUri) {
        try {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void handleGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();

        String galleryPicPath = getGalleryImagePath(selectedImageUri);

        if (galleryPicPath != null) {        // Image is on device
            Bitmap galleryImage = getBitmapFromUriOfGalleryImageOnDevice(selectedImageUri);
            setSelectedBitmapToImageOnDevice(selectedImageUri);

            if (galleryImage != null) {
                Bitmap scaledBitmap = scaleBitmap(galleryImage, 360);
                profileImageView.setImageBitmap(scaledBitmap);
            }
        }
        else {                              // Image is backed up to google servers
            Log.d("yuval", "on google servers");
            Log.d("yuval", String.valueOf(selectedImageUri == null));

            selectedImageBitmap = getBitmapFromImageUri(selectedImageUri);

            Log.d("yuval", String.valueOf(selectedImageBitmap == null));

            if (selectedImageBitmap != null) {
                profileImageView.setImageBitmap(scaleBitmap(selectedImageBitmap, 360));
            }
        }
    }

    private Bitmap getBitmapFromImageUri(Uri uri) {
        Bitmap bitmap = null;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            if (is != null) {
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private String getGalleryImagePath(Uri uri) {
        if (uri == null)
            return null;

        String path = null;

        String projection[] = {MediaStore.Images.Media.DATA};

        Cursor cursor;

        if (Build.VERSION.SDK_INT <= 19) {
            cursor = getContentResolver().query(uri, projection, null, null, null);
        } else {
            String wholeId = DocumentsContract.getDocumentId(uri);      // Will return "image:x*"
            String id = wholeId.split(":")[1];                          // Get the string after the colon
            String select = MediaStore.Images.Media._ID + "=?";         // Select statement for query

            cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, select, new String[]{id}, null);
        }
        if (cursor != null) {
            try {
                int column_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                path = cursor.getString(column_index);
            } catch (NullPointerException e) {
                path = null;
            }
            cursor.close();
        }
        return path;
    }


    private File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "Cliqdbase_profile_" + timeStamp;

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Cliqdbase");
        //noinspection ResultOfMethodCallIgnored
        storageDir.mkdir();         // Creating the Cliqdbase directory in the default Pictures Directory.

        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
            //mCurrentPhotoUri = Uri.fromFile(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startNextActivity();
    }

    /**
     * Starts the next activity.
     * This function is called after image upload, after cancellation, or after the back key is pressed
     *
     * If this activity is called after sign up, the next activity should be the main activity.
     * If another activity called this one, we should return to the calling activity.
     */
    public void startNextActivity() {
        if (this.signUp) {
            Intent intent = new Intent(ChangeProfilePictureActivity.this, MainActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        if (getProfilePicture != null && !getProfilePicture.getStatus().equals(AsyncTask.Status.FINISHED))
            getProfilePicture.cancel(true);
        super.onDestroy();
    }
}

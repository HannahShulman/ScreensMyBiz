package com.cliqdbase.app.async.server;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

/**
 * Created by Yuval on 26/09/2015.
 *
 * @author Yuval Siev
 */
public interface ImageDownloaded {
    void onImageDownloaded(@Nullable Bitmap imageBitMap);
}

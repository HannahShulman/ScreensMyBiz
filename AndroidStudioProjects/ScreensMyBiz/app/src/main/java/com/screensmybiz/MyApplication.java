package com.screensmybiz;

import android.app.Application;
import  com.firebase.client.Firebase;
/**
 * Created by hannashulmah on 16/11/2016.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}

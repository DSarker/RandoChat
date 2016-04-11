package com.industries.sarker.randochat;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by David on 3/28/16.
 */
public class RandoChat extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}

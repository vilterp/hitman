package com.hitman.client.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import com.hitman.client.activity.Startup;

public class GCMIntentService extends GCMBaseIntentService {

    public static final String REG_RECEIVED_ACTION = "org.androidsofdeath.client.action.REG_RECEIVED";

    public GCMIntentService() {
        super(Startup.SENDER_ID);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "onMessage: " + intent.toString());
    }

    @Override
    protected void onError(Context context, String errorId) {
        Log.e(TAG, "onError: " + errorId);
    }

    @Override
    protected void onRegistered(Context context, final String registrationId) {
        Log.i(TAG, "onRegistered");
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "onUnregistered");
    }

}

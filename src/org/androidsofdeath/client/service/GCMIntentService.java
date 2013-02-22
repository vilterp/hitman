package org.androidsofdeath.client.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;
import org.androidsofdeath.client.model.GameSession;

import java.io.Serializable;

public class GCMIntentService extends GCMBaseIntentService {

    public static final String REGISTER_TAG = "REGISTER";
    public static final String REG_RECEIVED_ACTION = "org.androidsofdeath.client.action.REG_RECEIVED";

    public GCMIntentService() {
        super(GameSession.SENDER_ID);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onError(Context context, String errorId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void onRegistered(Context context, final String registrationId) {
        Intent broadcast = new Intent();
        broadcast.putExtra("registration", registrationId);
        broadcast.setAction(REG_RECEIVED_ACTION);
        sendBroadcast(broadcast);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(REGISTER_TAG, "unregister");
    }

}

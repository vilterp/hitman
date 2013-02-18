package org.androidsofdeath.client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.android.gcm.GCMBaseIntentService;

import java.io.Serializable;

public class GCMIntentService extends GCMBaseIntentService {

    public static final String REGISTER_TAG = "REGISTER";
    public static final String REG_RECEIVED_ACTION = "org.androidsofdeath.client.action.REG_RECEIVED";

    public GCMIntentService() {
        super(Util.SENDER_ID);
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
        GCMRegistration registration = new GCMRegistration(registrationId);
        Intent broadcast = new Intent();
        broadcast.putExtra("registration", registration);
        broadcast.setAction(REG_RECEIVED_ACTION);
        sendBroadcast(broadcast);
    }

    public class GCMRegistration implements Serializable {

        private String regId;

        public GCMRegistration(String regId) {
            this.regId = regId;
        }

        public String getRegId() {
            return regId;
        }

    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(REGISTER_TAG, "unregister");
    }

}

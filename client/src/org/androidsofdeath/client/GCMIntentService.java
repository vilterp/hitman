package org.androidsofdeath.client;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

    private static final String SENDER_ID = "791109992959";

    public GCMIntentService() {
        super(SENDER_ID);
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
    protected void onRegistered(Context context, String registrationId) {
        Toast.makeText(context, String.format("onRegistered: %s", registrationId), 100).show();
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}

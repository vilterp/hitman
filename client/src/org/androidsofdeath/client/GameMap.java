package org.androidsofdeath.client;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;

public class GameMap extends Activity {

    private static final String TAG = "GameMap";
    private static final String SENDER_ID = "791109992959";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        Log.d(GCMIntentService.REGISTER_TAG, "checking registration");
        if (regId.equals("")) {
          Log.d(GCMIntentService.REGISTER_TAG, "registering");
          GCMRegistrar.register(this, SENDER_ID);
        } else {
          Log.d(GCMIntentService.REGISTER_TAG, "Already registered");
        }
    }

}

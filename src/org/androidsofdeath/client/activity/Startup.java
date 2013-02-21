package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.GameSession;

public class Startup extends Activity {

    private static final String TAG = "STARTUP";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting);
        this.registerReceiver(new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
            GCMIntentService.GCMRegistration registration =
                    (GCMIntentService.GCMRegistration) intent.getSerializableExtra("registration");
                        // send HTTP POST to log in
                        new AsyncTask<LoginCredentials, Void, Void>() {

                            @Override
                            protected Void doInBackground(LoginCredentials... params) {
                                assert params.length == 0;
                                org.androidsofdeath.client.activity.GameSession session = login(params[0]);
                                // finish this activity, sending the session back...
                                Intent res = new Intent();
                                res.putExtra("session", session);
                                LoggingIn.this.setResult(Activity.RESULT_OK, res);
                                LoggingIn.this.finish();
                                return null;
                            }

                        }.execute(new LoginCredentials(registration.getRegId(), nameEntered));
                    }

                }, new IntentFilter(GCMIntentService.REG_RECEIVED_ACTION));

        // register with GCM
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        Log.d(TAG, "checking registration");
        if (regId.equals("")) {
            Log.d(TAG, "registering");
            GCMRegistrar.register(this, GameSession.SENDER_ID);
        } else {
            Log.d(TAG, "Already registered");
            // launch login activity
            Intent intent = new Intent(Login.class)
        }
    }

}



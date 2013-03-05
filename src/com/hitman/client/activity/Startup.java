package com.hitman.client.activity;

import android.app.Activity;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import com.hitman.client.R;
import com.hitman.client.Util;
import com.hitman.client.http.Either;
import com.hitman.client.http.WrongSideException;
import com.hitman.client.model.*;
import com.hitman.client.GCMIntentService;
import com.hitman.client.service.LocationService;

public class Startup extends Activity {

    private static final String TAG = "HITMAN_STARTUP";

    public static final String SENDER_ID = "791109992959";


    private static final int GET_LOGIN_CREDENTIALS = 1;

    private LoggedOutContext loggedOutContext;
    private BroadcastReceiver receiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting);
        loggedOutContext = new LoggedOutContext(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        getGcmIdAndContinue();
    }

    private void getGcmIdAndContinue() {
        // register with GCM
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String gcmId = GCMRegistrar.getRegistrationId(this);
        Log.d(TAG, "checking registration");
        if (gcmId.equals("")) {
            Log.d(TAG, "registering");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String gcmId = intent.getStringExtra("registration");
                    // send HTTP POST to log in
                    getLicAndContinue(gcmId);
                    unregisterReceiver(receiver);
                    receiver = null;
                }
            };
            this.registerReceiver(receiver, new IntentFilter(GCMIntentService.REG_RECEIVED_ACTION));
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            Log.d(TAG, "Already registered");
            // launch login activity
            getLicAndContinue(gcmId);
        }
    }

    private void getLicAndContinue(String gcmId) {
        try {
            LoggedInContext loggedInContext = LoggedInContext.readFromStorage(loggedOutContext);
            enterGameOrAskForGame(loggedInContext);
        } catch (SessionStorage.NoCredentialsException e) {
            promptForCredentials(gcmId);
        }
    }

    private void promptForCredentials(String gcmId) {
        Intent launchLogin = new Intent(this, Login.class);
        launchLogin.putExtra("gcmId", gcmId);
        startActivityForResult(launchLogin, GET_LOGIN_CREDENTIALS);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        assert requestCode == GET_LOGIN_CREDENTIALS;
        assert resultCode == RESULT_OK;
        LoginCredentials credentials = (LoginCredentials) data.getSerializableExtra("credentials");
        doLogin(credentials);
    }

    private void doLogin(final LoginCredentials credentials) {
        new AsyncTask<LoginCredentials, Void, Either<Object,LoggedInContext>>() {
            @Override
            protected Either<Object, LoggedInContext> doInBackground(LoginCredentials... params) {
                assert params.length == 1;
                return loggedOutContext.doLogin(params[0]);
            }
            protected void onPostExecute(Either<Object,LoggedInContext> result) {
                try {
                    enterGameOrAskForGame(result.getRight());
                } catch (WrongSideException e) {
                    Util.handleError(Startup.this, e);
                    promptForCredentials(credentials.getGcmId());
                }
            }
        }.execute(credentials);
    }

    private void enterGameOrAskForGame(final LoggedInContext context) {
        try {
            PlayingContext playingContext = PlayingContext.readFromStorage(context);
            Startup.this.startService(new Intent(Startup.this, LocationService.class));
            Intent launchShowGame = new Intent(Startup.this, ShowGame.class);
            startActivity(launchShowGame);
        } catch (GameStorage.NoGameException e) {
            Intent launchGameList = new Intent(this, GameList.class);
            startActivity(launchGameList);
        }
    }

}

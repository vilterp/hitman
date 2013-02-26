package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.GameSession;
import org.androidsofdeath.client.model.LoginCredentials;
import org.androidsofdeath.client.service.GCMIntentService;

public class Startup extends Activity {

    private static final String TAG = "HITMAN_STARTUP";

    private static final String PREFS_NAME = "HitmanPrefs";
    private static final int GET_LOGIN_CREDENTIALS = 1;
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GAME_ID = "gameId";

    private SharedPreferences prefs;
    private BroadcastReceiver receiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String gcmId = intent.getStringExtra("registration");
                // send HTTP POST to log in
                getCredentialsAndLogin(gcmId);
                unregisterReceiver(receiver);
                receiver = null;
            }

        };
        this.registerReceiver(receiver, new IntentFilter(GCMIntentService.REG_RECEIVED_ACTION));
        // register with GCM
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String gcmId = GCMRegistrar.getRegistrationId(this);
        Log.d(TAG, "checking registration");
        if (gcmId.equals("")) {
            Log.d(TAG, "registering");
            GCMRegistrar.register(this, GameSession.SENDER_ID);
        } else {
            Log.d(TAG, "Already registered");
            // launch login activity
            getCredentialsAndLogin(gcmId);
        }
    }

    private void getCredentialsAndLogin(String gcmId) {
        String username = prefs.getString(USERNAME, null);
        // TODO: probably should store this more securely :P
        String password = prefs.getString(PASSWORD, null);
        if(username == null || password == null) {
            getCredentials(gcmId);
        } else {
            doLogin(new LoginCredentials(gcmId, username, password));
        }
    }

    private void getCredentials(String gcmId) {
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
        new AsyncTask<LoginCredentials, Void, GameSession>() {
            @Override
            protected GameSession doInBackground(LoginCredentials... params) {
                assert params.length == 1;
                return GameSession.doLogin(params[0]);
            }
            protected void onPostExecute(GameSession result) {
                if(result == null) {
                    // TODO: some kind of message ("wrong password", etc)
                    getCredentials(credentials.getGcmId());
                } else {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(USERNAME, credentials.getUsername());
                    editor.putString(PASSWORD, credentials.getPassword());
                    editor.commit();
                    enterGameOrAskForGame(result);
                }
            }
        }.execute(credentials);
    }

    private void enterGameOrAskForGame(GameSession session) {
        int gameId = prefs.getInt(GAME_ID, -1);
        if(gameId == -1) {
            Intent launchGameList = new Intent(this, GameList.class);
            launchGameList.putExtra("session", session);
            startActivity(launchGameList);
        } else {
            Intent launchGameDetail = new Intent(this, GameDetail.class);
            launchGameDetail.putExtra("session", session);
            startActivity(launchGameDetail);
        }
    }

}

package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMRegistrar;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.Util;
import org.androidsofdeath.client.http.Either;
import org.androidsofdeath.client.http.WrongSideException;
import org.androidsofdeath.client.model.*;
import org.androidsofdeath.client.service.GCMIntentService;

public class Startup extends Activity {

    private static final String TAG = "HITMAN_STARTUP";

    private static final String SENDER_ID = "791109992959";


    private static final int GET_LOGIN_CREDENTIALS = 1;
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PASSWORD = "password";

    private LoggedOutContext loggedOutContext;
    private BroadcastReceiver receiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting);
        loggedOutContext = new LoggedOutContext();
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
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            Log.d(TAG, "Already registered");
            // launch login activity
            getCredentialsAndLogin(gcmId);
        }
    }

    private void getCredentialsAndLogin(String gcmId) {
        LoginCredentials credentials = loggedOutContext.readCredentials(this, gcmId);
        if(credentials == null) {
            getCredentials(gcmId);
        } else {
            doLogin(credentials);
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
        new AsyncTask<LoginCredentials, Void, Either<Object,LoggedInContext>>() {
            @Override
            protected Either<Object, LoggedInContext> doInBackground(LoginCredentials... params) {
                assert params.length == 1;
                return loggedOutContext.doLogin(Startup.this, params[0]);
            }
            protected void onPostExecute(Either<Object,LoggedInContext> result) {
                try {
                    enterGameOrAskForGame(result.getRight());
                } catch (WrongSideException e) {
                    Util.handleError(Startup.this, e);
                    getCredentials(credentials.getGcmId());
                }
            }
        }.execute(credentials);
    }

    private void enterGameOrAskForGame(final LoggedInContext context) {
        int gameId = context.readGameId(this);
        if(gameId == -1) {
            Intent launchGameList = new Intent(this, GameList.class);
            launchGameList.putExtra("credentials", context.getCredentials());
            startActivity(launchGameList);
        } else {
            // get game
            new AsyncTask<Integer, Void, Either<Object,Game>>() {
                @Override
                protected Either<Object,Game> doInBackground(Integer... params) {
                    assert params.length == 1;
                    int theGameId = params[0];
                    return context.getGame(theGameId);
                }
                @Override
                public void onPostExecute(Either<Object,Game> result) {
                    try {
                        Game game = result.getRight();
                        Intent launchShowGame = new Intent(Startup.this, ShowGame.class);
                        launchShowGame.putExtra("credentials", context.getCredentials());
                        launchShowGame.putExtra("game", game);
                        startActivity(launchShowGame);
                    } catch (WrongSideException e) {
                        Util.handleError(Startup.this, e);
                    }
                }
            }.execute(gameId);
        }
    }

}

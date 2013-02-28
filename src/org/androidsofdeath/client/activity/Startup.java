package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gcm.GCMRegistrar;
import org.androidsofdeath.client.R;
import org.androidsofdeath.client.model.Game;
import org.androidsofdeath.client.model.UnexpectedResponseStatusException;
import org.androidsofdeath.client.model.GameSession;
import org.androidsofdeath.client.model.LoginCredentials;
import org.androidsofdeath.client.service.GCMIntentService;
import org.json.JSONException;

import java.io.IOException;

public class Startup extends Activity {

    private static final String TAG = "HITMAN_STARTUP";

    public static final String PREFS_NAME = "HitmanPrefs";
    private static final int GET_LOGIN_CREDENTIALS = 1;
    public static final String PREF_USERNAME = "username";
    public static final String PREF_PASSWORD = "password";
    public static final String PREF_CURRENT_GAME_ID = "gameId";

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
        String username = prefs.getString(PREF_USERNAME, null);
        // TODO: probably should store this more securely :P
        String password = prefs.getString(PREF_PASSWORD, null);
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
                try {
                    return GameSession.doLogin(params[0]);
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                    throw new RuntimeException(e);
                } catch (UnexpectedResponseStatusException e) {
                    Log.e(TAG, e.toString());
                    throw new RuntimeException(e);
                }
            }
            protected void onPostExecute(GameSession result) {
                if(result == null) {
                    // TODO: some kind of message ("wrong password", etc)
                    getCredentials(credentials.getGcmId());
                } else {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PREF_USERNAME, credentials.getUsername());
                    editor.putString(PREF_PASSWORD, credentials.getPassword());
                    editor.commit();
                    enterGameOrAskForGame(result);
                }
            }
        }.execute(credentials);
    }

    private void enterGameOrAskForGame(final GameSession session) {
        int gameId = prefs.getInt(PREF_CURRENT_GAME_ID, -1);
        if(gameId == -1) {
            Intent launchGameList = new Intent(this, GameList.class);
            launchGameList.putExtra("session", session);
            startActivity(launchGameList);
        } else {
            // get game
            new AsyncTask<Integer, Void, Game>() {
                @Override
                protected Game doInBackground(Integer... params) {
                    assert params.length == 1;
                    int theGameId = params[0];
                    try {
                        return session.getGame(theGameId);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    } catch (JSONException e) {
                        Log.e(TAG, e.toString());
                    } catch (UnexpectedResponseStatusException e) {
                        Log.e(TAG, e.toString());
                    }
                    return null;
                }
                @Override
                public void onPostExecute(Game game) {
                    if(game == null) {
                        Toast.makeText(Startup.this, "Network error. Try again?", Toast.LENGTH_LONG).show();
                    } else {
                        Intent launchShowGame = new Intent(Startup.this, ShowGame.class);
                        session.setCurrentGame(game);
                        launchShowGame.putExtra("session", session);
                        launchShowGame.putExtra("game", game);
                        startActivity(launchShowGame);
                    }
                }
            }.execute(gameId);
        }
    }

}

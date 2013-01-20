package org.androidsofdeath.client;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;
import java.util.Map;

public class SigningIn extends Activity {

    public static final String TAG = "SigningIn";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signingin);
        final String nameEntered = getIntent().getStringExtra("name");
        // set up BroadcastReceiver to listen for "registered" broadcast from GCMIntentService
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
                        GameSession session = login(params[0]);
                        // finish this activity, sending the session back...
                        Intent res = new Intent();
                        res.putExtra("session", session);
                        SigningIn.this.setResult(Activity.RESULT_OK, res);
                        SigningIn.this.finish();
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
            GCMRegistrar.register(this, Util.SENDER_ID);
        } else {
            Log.d(TAG, "Already registered");
            // log in; device already registered
            new AsyncTask<LoginCredentials, Void, Void>() {
                @Override
                protected Void doInBackground(LoginCredentials... params) {
                    GameSession session = login(params[0]);
                    Intent intent = new Intent();
                    intent.putExtra("session", session);
                    SigningIn.this.setResult(Activity.RESULT_OK, intent);
                    SigningIn.this.finish();
                    return null;
                }
            }.execute(new LoginCredentials(regId, nameEntered));
        }
    }

    /**
     * Do this in the background!
     * @param credentials
     * @return newly constructed session
     */
    public GameSession login(LoginCredentials credentials) {
        Map<String, String> loginPostParams = new HashMap<String, String>();
        loginPostParams.put("regid", credentials.getRegId());
        loginPostParams.put("user", credentials.getUsername());
        // execute HTTP request
        CookieStore cookieStore = new BasicCookieStore();
        HttpResponse response = Util.sendHTTPPost(loginPostParams, "login", cookieStore);
        int resCode = response.getStatusLine().getStatusCode();
        String sessionIdCookie = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if(cookie.getName().equals("sessionid"))
                sessionIdCookie = cookie.getValue();
        }
        assert sessionIdCookie != null;
        assert resCode == 200;
        // construct session object
        GameSession session = new GameSession(credentials.getRegId(),
                credentials.getUsername(), sessionIdCookie);
        return session;
    }

    public class LoginCredentials {

        private String regId;
        private String username;

        public LoginCredentials(String regId, String username) {
            this.regId = regId;
            this.username = username;
        }

        public String getRegId() {
            return regId;
        }

        public String getUsername() {
            return username;
        }

    }

}

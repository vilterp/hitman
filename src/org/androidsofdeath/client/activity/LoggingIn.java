package org.androidsofdeath.client.activity;

import android.app.Activity;
import android.os.Bundle;
import org.androidsofdeath.client.R;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;
import java.util.Map;

public class LoggingIn extends Activity {

    public static final String TAG = "LoggingIn";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waiting);
        final String nameEntered = getIntent().getStringExtra("name");
        // set up BroadcastReceiver to listen for "registered" broadcast from GCMIntentService

        }
    }

    /**
     * Do this in the background!
     * @param credentials
     * @return newly constructed session
     */
    public org.androidsofdeath.client.activity.GameSession login(LoginCredentials credentials) {
        Map<String, String> loginPostParams = new HashMap<String, String>();
        loginPostParams.put("regid", credentials.getRegId());
        loginPostParams.put("user", credentials.getUsername());
        // execute HTTP request
        CookieStore cookieStore = new BasicCookieStore();
        HttpResponse response = org.androidsofdeath.client.model.GameSession.sendHTTPPost(loginPostParams, "login", cookieStore);
        int resCode = response.getStatusLine().getStatusCode();
        String sessionIdCookie = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if(cookie.getName().equals("sessionid"))
                sessionIdCookie = cookie.getValue();
        }
        assert sessionIdCookie != null;
        assert resCode == 200;
        // construct session object
        org.androidsofdeath.client.activity.GameSession session = new org.androidsofdeath.client.activity.GameSession(credentials.getRegId(),
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

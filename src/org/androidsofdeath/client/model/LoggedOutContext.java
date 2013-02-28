package org.androidsofdeath.client.model;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.common.base.Function;
import org.androidsofdeath.client.http.Either;
import org.androidsofdeath.client.http.HTTPMethod;
import org.androidsofdeath.client.http.Left;
import org.apache.http.Header;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggedOutContext extends HitmanContext {

    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    public LoggedOutContext(Context androidContext) {
        super(androidContext);
    }

    @Override
    public List<Header> getHeaders() {
        return NO_HEADERS;
    }

    public LoginCredentials readCredentials(String gcmId) {
        String username = getPrefs().getString(PREF_USERNAME, null);
        // TODO: probably should store this more securely :P
        String password = getPrefs().getString(PREF_PASSWORD, null);
        if(username == null || password == null) {
            // TODO: maybe
            return null;
        } else {
            return new LoginCredentials(gcmId, username, password);
        }
    }

    public Either<Object,LoggedInContext> doSignup(final LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        params.put("gcm_regid", credentials.getGcmId());
        return getJsonObjectExpectCodes("/users/signup", params, HTTPMethod.POST, 200)
                .bindRight(new Function<JSONObject, LoggedInContext>() {
                    public LoggedInContext apply(JSONObject jsonObject) {
                        writePrefs(credentials);
                        return new LoggedInContext(getAndroidContext(), credentials);
                    }
                });
    }

    public Either<Object,LoggedInContext> doLogin(final LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("username", credentials.getUsername());
        params.put("password", credentials.getPassword());
        return getJsonObjectExpectCodes("/users/login", params, HTTPMethod.POST, 200)
                .bindRight(new Function<JSONObject, LoggedInContext>() {
                    public LoggedInContext apply(JSONObject jsonObject) {
                        writePrefs(credentials);
                        return new LoggedInContext(getAndroidContext(), credentials);
                    }
                });
    }

    private void writePrefs(LoginCredentials credentials) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(PREF_USERNAME, credentials.getUsername());
        editor.putString(PREF_PASSWORD, credentials.getPassword());
        editor.commit();
    }

}

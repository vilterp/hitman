package com.hitman.client.model;

import com.google.common.base.Function;
import com.hitman.client.http.Either;
import com.hitman.client.http.HTTPMethod;
import org.apache.http.Header;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggedOutContext extends HitmanContext {

    private static final String PREF_USERNAME = "username";
    private static final String PREF_PASSWORD = "password";

    @Override
    public List<Header> getHeaders() {
        return NO_HEADERS;
    }

    public Either<Object,LoggedInContext> doSignup(final SessionStorage storage, final LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        params.put("gcm_regid", credentials.getGcmId());
        return getJsonObjectExpectCodes("/users/signup", params, HTTPMethod.POST, 200)
                .bindRight(new Function<JSONObject, LoggedInContext>() {
                    public LoggedInContext apply(JSONObject jsonObject) {
                        storage.saveLoginCredentials(credentials);
                        return new LoggedInContext(credentials);
                    }
                });
    }

    public Either<Object,LoggedInContext> doLogin(final SessionStorage storage, final LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", credentials.getGcmId());
        params.put("username", credentials.getUsername());
        params.put("password", credentials.getPassword());
        return getJsonObjectExpectCodes("/users/login", params, HTTPMethod.POST, 200)
                .bindRight(new Function<JSONObject, LoggedInContext>() {
                    public LoggedInContext apply(JSONObject jsonObject) {
                        storage.saveLoginCredentials(credentials);
                        return new LoggedInContext(credentials);
                    }
                });
    }

}

package com.hitman.client.model;

import android.content.Context;
import com.google.common.base.Function;
import com.hitman.client.http.Either;
import com.hitman.client.http.HTTPMethod;
import org.apache.http.Header;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoggedOutContext extends HitmanContext {

    public LoggedOutContext(Context androidContext) {
        super(androidContext);
    }

    @Override
    public List<Header> getHeaders() {
        return NO_HEADERS;
    }

    public Either<Object,LoggedInContext> doSignup(final LoginCredentials credentials) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", credentials.getUsername());
        params.put("password", credentials.getPassword());
        params.put("gcm_regid", credentials.getGcmId());
        return getJsonObjectExpectCodes("/users/signup", params, HTTPMethod.POST, 200)
                .bindRight(new Function<JSONObject, LoggedInContext>() {
                    public LoggedInContext apply(JSONObject jsonObject) {
                        getSessionStorage().saveLoginCredentials(credentials);
                        return LoggedInContext.createFromLogin(LoggedOutContext.this, credentials);
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
                        getSessionStorage().saveLoginCredentials(credentials);
                        return LoggedInContext.createFromLogin(LoggedOutContext.this, credentials);
                    }
                });
    }

}

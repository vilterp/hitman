package org.androidsofdeath.client.model;

import android.location.Location;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The static methods in here are blocking. Call them
 * within an AsyncTask.
 */
public class GameSession {

    public static final String TAG = "GameSession";

    public static final String SENDER_ID = "791109992959";
    private static final String SERVER_HOST = "vm1.kevinzhang.org";
    private static final int SERVER_PORT = 9000;

    private String username;
    private String gcmId;

    public GameSession(String username, String gcmId) {
        this.username = username;
        this.gcmId = gcmId;
    }

    public static GameSession doSignup(String username, String password, String gcmId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", username);
        params.put("password", password);
        params.put("gcm_regid", gcmId);
        String path = "/users/signup";
        // TODO: flow when signup doesn't work (e.g. username already taken)
        execAppPost(path, params);
        return new GameSession(username, gcmId);
    }

    public static GameSession doLogin(String username, String password, String gcmId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", gcmId);
        params.put("user", username);
        params.put("password", password);
        execAppPost("/users/login", params);
        // TODO: flow when login doesn't work
        return new GameSession(username, gcmId);
    }

    public void updateLocation(Location location) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("gcm_regid", gcmId);
        params.put("lat", Double.toString(location.getLatitude()));
        params.put("lon", Double.toString(location.getLongitude()));
        execAppPost("/locations/update", params);
    }

    private static HttpResponse execAppPost(String path, Map<String, String> params) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        String postUrl = String.format("http://%s:%d/%s/", SERVER_HOST, SERVER_PORT, path);
        HttpPost httpPost = new HttpPost(postUrl);

        List<NameValuePair> args = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            args.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }

        try {
            httpPost.setEntity(new UrlEncodedFormEntity(args));
            HttpResponse response = httpClient.execute(httpPost);
            return response;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null; // TODO: probably not the best way to handle errors
        }

    }

}
